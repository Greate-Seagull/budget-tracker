package com.lichsu.budget_tracker.domain

enum class AmountError {
    EMPTY,
    INVALID,
    TOO_LARGE,
    NOT_POSITIVE

}

sealed interface AmountResult {
    data class Invalid(val error: AmountError): AmountResult
    data class Valid(val value: Long): AmountResult
}

object AmountValidator {
    const val MAX_AMOUNT = 100_000_000_000L

    fun validate(raw: String): AmountResult {
        val text = raw.trim()
        if (text.isEmpty())
            return AmountResult.Invalid(AmountError.EMPTY)

        val digits = text.replace(".", "")
        if (digits.isEmpty() || !digits.all { it in '0'..'9' })
            return AmountResult.Invalid(AmountError.INVALID)

        val value = digits.toLongOrNull() ?: return AmountResult.Invalid(AmountError.TOO_LARGE)

        return when {
            value <= 0 -> AmountResult.Invalid(AmountError.NOT_POSITIVE)
            value > MAX_AMOUNT -> AmountResult.Invalid(AmountError.TOO_LARGE)
            else -> AmountResult.Valid(value)
        }
    }
}