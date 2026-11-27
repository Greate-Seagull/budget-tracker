package com.lichsu.budget_tracker.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichsu.budget_tracker.ui.components.text.NormalText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppBottomSheet(
    title: String,

    trigger: @Composable (open: () -> Unit) -> Unit,

    content: @Composable ColumnScope.(
        close: (afterClose: () -> Unit) -> Unit,
        expand: () -> Unit,
    ) -> Unit,
) {
    var show by rememberSaveable { mutableStateOf(false) }

    trigger { show = true }

    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    // Chạy animation đóng xong mới báo ra ngoài
    val close: (() -> Unit) -> Unit = { afterClose ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                afterClose()
                show = false
            }
        }
    }
    val expand: () -> Unit = { scope.launch { sheetState.expand() } }

    ModalBottomSheet(
        onDismissRequest = { show = false },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            BottomSheetDefaults.DragHandle(modifier = Modifier.padding(0.dp))
        },
    ) {
        val imeVisible = WindowInsets.isImeVisible
        val extraSpace = with(LocalDensity.current) {
            (LocalWindowInfo.current.containerSize.height / 3).toDp()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SheetHeader(title = title)

            content(close, expand)

            // Chừa chỗ cuộn khi bàn phím mở
            if (imeVisible) {
                Spacer(Modifier.height(extraSpace))
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        NormalText(
            label = title,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 24.sp),
        )
    }
}