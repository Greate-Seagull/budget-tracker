package com.lichsu.budget_tracker.ui.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Mẫu chung: [trigger] nhận hàm mở; khi mở thì hiện [dialog] nhận hàm đóng. */
@Composable
fun TriggeredDialog(
    trigger: @Composable (open: () -> Unit) -> Unit,
    dialog: @Composable (dismiss: () -> Unit) -> Unit,
) {
    var show by rememberSaveable { mutableStateOf(false) }

    trigger { show = true }
    if (show) dialog { show = false }
}
