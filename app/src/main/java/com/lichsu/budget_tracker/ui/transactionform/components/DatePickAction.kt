package com.lichsu.budget_tracker.ui.transactionform.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lichsu.budget_tracker.ui.theme.BudgetTrackerTheme
import com.lichsu.budget_tracker.ui.transactionform.components.labels.NormalText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun DatePickerPreview() {
    BudgetTrackerTheme {
        DatePicker(
            state = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickAction(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable (open: () -> Unit) -> Unit,
) {
    var show by rememberSaveable { mutableStateOf(false) }

    trigger { show = true }
    val onDismiss = { show = false }

    if (!show)
        return

    // DatePicker của Material 3 làm việc với millis theo UTC
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    millis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateSelected(date)
                    }
                    onDismiss()
                }
            ) { NormalText("Chọn") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { NormalText("Hủy") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}