package com.lichsu.budget_tracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** DateRangePicker làm việc với millis theo UTC. */
fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.toUtcDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
