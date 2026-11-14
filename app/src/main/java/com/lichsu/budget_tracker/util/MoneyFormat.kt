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
