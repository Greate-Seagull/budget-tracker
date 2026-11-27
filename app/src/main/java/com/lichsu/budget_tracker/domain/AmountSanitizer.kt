package com.lichsu.budget_tracker.domain

/** Chỉ giữ chữ số, tối đa [maxDigits] chữ số, bỏ số 0 đứng đầu ("007" -> "7", "000" -> "0"). */
fun sanitizeAmountDigits(raw: String, maxDigits: Int = 12): String {
    val digits = raw.filter { it in '0'..'9' }.take(maxDigits)
    return digits.trimStart('0').ifEmpty { if (digits.isEmpty()) "" else "0" }
}
