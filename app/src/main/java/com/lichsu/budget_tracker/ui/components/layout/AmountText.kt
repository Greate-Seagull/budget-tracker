package com.lichsu.budget_tracker.ui.components.layout

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.TextStyle
import com.lichsu.budget_tracker.ui.components.text.NormalText
import com.lichsu.budget_tracker.ui.theme.LittleWarning
import com.lichsu.budget_tracker.util.formatAmount
import java.text.NumberFormat

@Composable
fun rememberNumberFormat(): NumberFormat {
    val locale = LocalLocale.current.platformLocale
    return remember(locale) { NumberFormat.getInstance(locale) }
}

@Composable
fun amountColor(amount: Long): Color = when {
    amount == 0L -> MaterialTheme.colorScheme.tertiary
    amount > 0L -> MaterialTheme.colorScheme.primary
    else -> LittleWarning
}

@Composable
fun AmountText(amount: Long, style: TextStyle, numberFormat: NumberFormat) {
    NormalText(
        label = formatAmount(amount, numberFormat),
        style = style,
        color = amountColor(amount),
    )
}
