package com.lichsu.budget_tracker.ui.components.animators

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

const val SLIDE_DURATION = 300

/**
 * Hàng [optionCount] ô cùng chiều rộng, có pill trượt mượt theo [selectedIndex].
 * Các item trong [content] phải dùng `Modifier.weight(1f)` và không có khoảng cách giữa chúng.
 */
@Composable
fun SlidingSelector(
    selectedIndex: Int,
    optionCount: Int,
    modifier: Modifier = Modifier,
    slideDuration: Int = SLIDE_DURATION,
    shape: Shape = MaterialTheme.shapes.medium,
    pillColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable RowScope.() -> Unit,
) {
    val position by animateFloatAsState(
        targetValue = selectedIndex.coerceAtLeast(0).toFloat(),
        animationSpec = tween(slideDuration, easing = EaseOutCubic),
        label = "selectorPosition",
    )
    var trackWidthPx by remember { mutableIntStateOf(0) }

    Box(modifier) {
        Box(
            Modifier
                .matchParentSize()
                .onSizeChanged { trackWidthPx = it.width }
        ) {
            Box(
                Modifier
                    .fillMaxWidth(1f / optionCount)
                    .fillMaxHeight()
                    .offset { IntOffset((position * trackWidthPx / optionCount).roundToInt(), 0) }
                    .clip(shape)
                    .background(pillColor)
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}