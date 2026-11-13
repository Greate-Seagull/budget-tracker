package com.lichsu.budget_tracker.ui.transactionform.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    maxLength: Int = 60
) {
    val bringIntoView = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }

    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)

    LaunchedEffect(focused, imeBottom) {
        if (focused && imeBottom > 0) {
            delay(80)                      // đợi chiều cao bàn phím ngừng đổi
            bringIntoView.bringIntoView()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .bringIntoViewRequester(bringIntoView)   // phải đứng TRƯỚC padding
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        CircleIcon(R.drawable.ic_note)

        Spacer(Modifier.width(12.dp))

        BasicTextField(
            value = note,
            onValueChange = { if (it.length <= maxLength) onNoteChange(it) },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                if (note.isEmpty()) Text(
                    "Ghi chú",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.titleSmall
                )
                inner()
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "${note.length}/$maxLength",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleSmall
        )
    }
}