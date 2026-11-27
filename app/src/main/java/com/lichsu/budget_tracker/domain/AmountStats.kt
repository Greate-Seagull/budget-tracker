package com.lichsu.budget_tracker.domain.model

private const val DEFAULT_BUCKETS = 20

data class AmountStats(
    val min: Long,
    val max: Long,
    val histogram: List<Int>,
) {
    companion object {
        val Empty = AmountStats(0L, 0L, List(DEFAULT_BUCKETS) { 0 })
    }
}

fun buildAmountStats(amounts: List<Long>, buckets: Int = DEFAULT_BUCKETS): AmountStats {
    val min = amounts.minOrNull() ?: 0L
    val max = amounts.maxOrNull() ?: 0L
    return AmountStats(min, max, buildHistogram(amounts, min, max, buckets))
}

fun buildHistogram(
    amounts: List<Long>,
    min: Long,
    max: Long,
    buckets: Int = DEFAULT_BUCKETS,
): List<Int> {
    if (amounts.isEmpty() || max <= min) return List(buckets) { 0 }
    val width = (max - min).toDouble() / buckets
    val counts = IntArray(buckets)
    amounts.forEach { a ->
        val i = ((a - min) / width).toInt().coerceAtMost(buckets - 1)
        counts[i]++
    }
    return counts.toList()
}
