package com.lichsu.budget_tracker.ui.components.text

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** primary khi bình thường, error khi lỗi; đổi màu mượt. */
@Composable
fun animatedErrorColor(isError: Boolean, label: String): Color {
    val color by animateColorAsState(
        targetValue = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label = label,
    )
    return color
}

/** Dòng báo lỗi (icon + chữ), trượt/mờ khi xuất hiện hoặc biến mất. */
@Composable
fun AnimatedErrorText(errorText: String?, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = errorText,
        modifier = modifier,
        transitionSpec = {
            (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically())
        },
        label = "errorText",
    ) { text ->
        if (text != null) ErrorText(text)
    }
}

@Composable
private fun ErrorText(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp, top = 6.dp),
    ) {
        Icon(
            Icons.Default.ErrorOutline, null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        NormalText(
            text,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
