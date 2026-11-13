package com.lichsu.budget_tracker.ui.transactionform.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.transactionform.components.icons.NormalIcon
import com.lichsu.budget_tracker.ui.transactionform.components.labels.NormalText
import com.lichsu.budget_tracker.ui.transactionform.components.labels.Title
import kotlinx.coroutines.launch

data class CategoryUi(val id: Long, val name: String, @DrawableRes val iconRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(
    categories: List<CategoryUi>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    trigger: @Composable (open: () -> Unit) -> Unit,
) {
    var show by rememberSaveable { mutableStateOf(false) }

    trigger { show = true }
    val onDismiss = { show = false }

    if (!show)
        return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Chạy animation đóng xong mới báo ra ngoài
    fun close(afterClose: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                afterClose()
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        SheetHeader(
            title = "Chọn danh mục",
            onClose = { close() }
        )
        CategoryPickerContent(
            categories = categories,
            selectedId = selectedId,
            onSelected = { close { onSelected(it.id) } },
        )
    }
}

@Composable
fun CategoryPickerContent(
    categories: List<CategoryUi>,
    selectedId: Long?,
    onSelected: (CategoryUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val showSearch = categories.size > 8

    val filtered = remember(categories, query) {
        if (query.isBlank()) categories
        else categories.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    Column(modifier.fillMaxWidth().imePadding()) {
        NormalText("Chọn danh mục", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        if (showSearch) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Tìm danh mục") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered, key = { it.id }) { category ->
                val selected = category.id == selectedId
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(category) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    CircleIcon(category.iconRes)
                    Spacer(Modifier.width(14.dp))
                    NormalText(category.name, Modifier.weight(1f))
                    if (selected) {
                        Icon(
                            Icons.Default.Check, contentDescription = "Đã chọn",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            if (filtered.isEmpty()) {
                item { NormalText("Không tìm thấy", Modifier.padding(16.dp)) }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
    ) {
        Title(
            label = title,
            modifier = Modifier.weight(1f)
        )

        FilledTonalIconButton(
            onClick = onClose,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
//            NormalIcon(Icons.Default.Close)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}