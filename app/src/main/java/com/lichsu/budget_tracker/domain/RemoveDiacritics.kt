package com.lichsu.budget_tracker.domain

import java.text.Normalizer

private val COMBINING_MARKS = Regex("\\p{Mn}+")

fun String.removeDiacritics(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .replace('đ', 'd')
        .replace('Đ', 'D')