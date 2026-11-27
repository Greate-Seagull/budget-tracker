package com.lichsu.budget_tracker.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.text.NormalText

@Composable
fun ConfirmDialog(
    title: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable (open: () -> Unit) -> Unit,
) {
    TriggeredDialog(trigger) { dismiss ->
        AlertDialog(
            modifier = modifier,
            title = { NormalText(title) },
            confirmButton = {
                TextButton(onClick = {
                    dismiss()
                    onConfirmed()
                }) { NormalText(stringResource(R.string.action_confirm)) }
            },
            dismissButton = { TextButton(onClick = dismiss) { NormalText(stringResource(R.string.action_cancel)) } },
            onDismissRequest = dismiss,
        )
    }
}
