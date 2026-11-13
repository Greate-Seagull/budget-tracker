package com.lichsu.budget_tracker.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateFormats {
    val display: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val displayShort: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/yyyy")
}

fun LocalDate.toDisplayString(): String = format(DateFormats.display)