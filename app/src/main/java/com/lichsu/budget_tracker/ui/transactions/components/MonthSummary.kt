package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.layout.AmountText
import com.lichsu.budget_tracker.ui.components.layout.rememberNumberFormat
import com.lichsu.budget_tracker.ui.components.text.NormalText

@Composable
fun MonthSummary(total: Long, modifier: Modifier = Modifier) {
    val numberFormat = rememberNumberFormat()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NormalText(stringResource(R.string.month_total))
        AmountText(total, MaterialTheme.typography.titleMedium, numberFormat)
    }
}
