package com.lichsu.budget_tracker.ui.transactionform.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lichsu.budget_tracker.ui.transactionform.components.labels.NormalText

@Composable
fun ConfirmAction(
    title: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable (open: () -> Unit) -> Unit,
) {
    var show by rememberSaveable { mutableStateOf(false) }

    trigger { show = true }

    if (show) {
        AlertDialog(
            modifier = modifier,
            title = { NormalText(title) },
            confirmButton = {
                TextButton(onClick = {
                    show = false
                    onConfirmed()
                }) { NormalText("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { show = false })
                { NormalText("Hủy") }
            },
            onDismissRequest = { show = false },
        )
    }
}