package com.lichsu.budget_tracker.util

fun ClosedFloatingPointRange<Float>.coerceIn(
    bounds: ClosedFloatingPointRange<Float>,
): ClosedFloatingPointRange<Float> =
    start.coerceIn(bounds.start, bounds.endInclusive)..
            endInclusive.coerceIn(bounds.start, bounds.endInclusive)
