package com.lichsu.budget_tracker.ui.components.inputs

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun PriceHistogram(
    bins: List<Int>,
    startFraction: Float,
    endFraction: Float,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        if (bins.isEmpty()) return@Canvas
        val maxCount = bins.max().coerceAtLeast(1)
        val gap = 2.dp.toPx()
        val barWidth = (size.width - gap * (bins.size - 1)) / bins.size

        bins.forEachIndexed { i, count ->
            val center = (i + 0.5f) / bins.size
            val h = size.height * count / maxCount
            drawRect(
                color = if (center in startFraction..endFraction) activeColor else inactiveColor,
                topLeft = Offset(i * (barWidth + gap), size.height - h),
                size = Size(barWidth, h),
            )
        }
    }
}
