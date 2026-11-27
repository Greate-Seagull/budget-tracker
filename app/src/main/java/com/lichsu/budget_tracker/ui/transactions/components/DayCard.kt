package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.layout.AmountText
import com.lichsu.budget_tracker.ui.components.buttons.NormalRow
import com.lichsu.budget_tracker.ui.components.text.NormalText
import com.lichsu.budget_tracker.ui.transactions.TransactionDay
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

@Composable
fun DayCard(
    day: TransactionDay,
    today: LocalDate,
    locale: Locale,
    numberFormat: NumberFormat,
    onRowClicked: (Long) -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(4.dp),
        ) {
            DayHeader(day, today, locale, numberFormat)
            day.items.forEach { tx ->
                AmountRow(
                    label = tx.category.name,
                    amount = tx.signedAmount,
                    style = MaterialTheme.typography.bodySmall,
                    numberFormat = numberFormat,
                    onClick = { onRowClicked(tx.id) },
                )
            }
        }
    }
}

@Composable
private fun DayHeader(
    day: TransactionDay,
    today: LocalDate,
    locale: Locale,
    numberFormat: NumberFormat,
) {
    val colors = MaterialTheme.colorScheme
    val isToday = day.date == today
    val tileColor = if (isToday) colors.primary else colors.secondary
    val onTile = if (isToday) colors.onPrimary else colors.onSecondary
    val style = MaterialTheme.typography.titleSmall

    NormalRow(
        leading = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(tileColor, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                NormalText(label = day.date.dayOfMonth.toString(), color = onTile)
            }
        },
        trailing = { AmountText(day.total, style, numberFormat) },
    ) {
        NormalText(label = dayTitle(day.date, today, locale), style = style)
    }
}

@Composable
private fun AmountRow(
    label: String,
    amount: Long,
    style: TextStyle,
    numberFormat: NumberFormat,
    onClick: () -> Unit = {},
) {
    NormalRow(
        leading = { NormalText(label = label, style = style) },
        trailing = { AmountText(amount, style, numberFormat) },
        onClick = onClick,
    ) {}
}

@Composable
private fun dayTitle(date: LocalDate, today: LocalDate, locale: Locale): String =
    when (ChronoUnit.DAYS.between(today, date)) {
        0L -> stringResource(R.string.day_today)
        -1L -> stringResource(R.string.day_yesterday)
        1L -> stringResource(R.string.day_tomorrow)
        else -> date.dayOfWeek.getDisplayName(JavaTextStyle.FULL, locale) // "Thứ Tư"
    }
