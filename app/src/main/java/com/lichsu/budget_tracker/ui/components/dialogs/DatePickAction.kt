package com.lichsu.budget_tracker.ui.components.dialogs

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.text.NormalText
import com.lichsu.budget_tracker.util.toUtcDate
import com.lichsu.budget_tracker.util.toUtcMillis
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickAction(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable (open: () -> Unit) -> Unit,
) {
    TriggeredDialog(trigger) { dismiss ->
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.toUtcMillis()
        )

        DatePickerDialog(
            modifier = modifier,
            onDismissRequest = dismiss,
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDateSelected(it.toUtcDate()) }
                    dismiss()
                }) { NormalText(stringResource(R.string.action_confirm)) }
            },
            dismissButton = { TextButton(onClick = dismiss) { NormalText(stringResource(R.string.action_cancel)) } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
