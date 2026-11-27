package com.lichsu.budget_tracker.domain

fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item