package com.lichsu.budget_tracker.ui.components.inputs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.components.animators.SLIDE_DURATION
import com.lichsu.budget_tracker.ui.components.animators.SlidingSelector

@Composable
fun <T : Enum<T>> SegmentedSwitch(
    options: List<T>,
    selected: T,
    onValueChanged: (T) -> Unit,
    labelRes: (T) -> Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(4.dp)
    ) {
        SlidingSelector(
            selectedIndex = options.indexOf(selected),
            optionCount = options.size,
        ) {
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
