package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.text.NormalText
import java.time.YearMonth

object MonthLabelDefaults {
    /** Chiều cao cố định của nhãn tháng: dùng cho cả UI lẫn tính toán cuộn. */
    val Height: Dp = 48.dp
}

@Composable
fun MonthLabel(month: YearMonth) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MonthLabelDefaults.Height)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        NormalText(
            label = stringResource(R.string.month_year, month.monthValue, month.year),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
