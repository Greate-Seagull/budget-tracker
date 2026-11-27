package com.lichsu.budget_tracker.ui.components.dialogs

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.domain.model.DateRange
import com.lichsu.budget_tracker.util.toUtcDate
import com.lichsu.budget_tracker.util.toUtcMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    initial: DateRange,
    onDismiss: () -> Unit,
    onConfirm: (DateRange) -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initial.start.toUtcMillis(),
        initialSelectedEndDateMillis = initial.end.toUtcMillis(),
        initialDisplayedMonthMillis = initial.start.toUtcMillis(),
    )

    val start = state.selectedStartDateMillis?.toUtcDate()
    val end = state.selectedEndDateMillis?.toUtcDate() ?: start

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { start?.let { onConfirm(DateRange(it, end ?: it)) } },
                enabled = start != null,
            ) { Text(stringResource(R.string.action_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DateRangePicker(
            state = state,
            title = null,
            headline = null,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
            modifier = Modifier.height(480.dp), // bắt buộc có chiều cao cố định
        )
    }
}
