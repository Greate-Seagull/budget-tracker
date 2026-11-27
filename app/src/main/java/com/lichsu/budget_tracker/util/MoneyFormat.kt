package com.lichsu.budget_tracker.util

import java.text.NumberFormat
import java.util.Locale

object MoneyFormat {
    private val vietnamese = Locale.forLanguageTag("vi-VN")

    fun vnd(amount: Long): String = "${number(amount)} đ"

    fun number(amount: Long): String = NumberFormat.getIntegerInstance(vietnamese).format(amount)

    fun percent(ratio: Double): String = NumberFormat.getPercentInstance(vietnamese).apply {
        maximumFractionDigits = 1
    }.format(ratio)
}

/** "+1,000" / "-1,000" / "0". */
fun formatAmount(amount: Long, format: NumberFormat): String {
    val sign = when {
        amount == 0L -> ""
        amount > 0L -> "+"
        else -> "-"
    }
    return sign + format.format(abs(amount))
}
