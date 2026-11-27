package com.lichsu.budget_tracker.ui.components.inputs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val SLIDE_DURATION = 300

@Composable
fun <T : Enum<T>> SegmentedSwitch(
    options: List<T>,
    selected: T,
    onValueChanged: (T) -> Unit,
    labelRes: (T) -> Int,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)

    // Vị trí pill theo đơn vị "số option": 0f, 1f, 2f... animate mượt giữa các giá trị
    val position by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(SLIDE_DURATION, easing = EaseOutCubic),
        label = "switchPosition",
    )

    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(4.dp)
    ) {
        SlidingPill(position = position, optionCount = options.size)

        Row(verticalAlignment = Alignment.CenterVertically) {
            options.forEach { option ->
                SwitchOption(
                    label = stringResource(labelRes(option)),
                    selected = option == selected,
                    onClick = { onValueChanged(option) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Lớp dưới: pill trượt theo [position]. */
@Composable
private fun BoxScope.SlidingPill(position: Float, optionCount: Int) {
    var trackWidthPx by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .matchParentSize()
            .onSizeChanged { trackWidthPx = it.width }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(1f / optionCount)
                .fillMaxHeight()
                .offset {
                    IntOffset(x = (position * trackWidthPx / optionCount).roundToInt(), y = 0)
                }
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

/** Lớp trên: chữ + vùng bấm của một option. */
@Composable
private fun SwitchOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSecondary,
        animationSpec = tween(SLIDE_DURATION),
        label = "switchTextColor",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 30.dp, vertical = 10.dp)
    ) {
        Text(text = label, color = textColor, style = MaterialTheme.typography.titleSmall)
    }
}
