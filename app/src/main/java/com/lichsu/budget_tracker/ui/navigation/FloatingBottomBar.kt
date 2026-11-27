package com.lichsu.budget_tracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.components.animators.SLIDE_DURATION
import com.lichsu.budget_tracker.ui.components.animators.SlidingSelector

private val BarHeight = 68.dp

@Composable
fun FloatingBottomBar(
    destinations: List<Destination>,
    active: Destination,
    onSelect: (Destination) -> Unit,
    onActionClick: (BarAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Giữ action cuối cùng để animation thoát vẫn còn icon (khi active.action chuyển về null)
    val current = active.action
    var lastAction by remember { mutableStateOf(current) }
    if (current != null) lastAction = current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BarSurface(Modifier.weight(1f).height(BarHeight)) {
            SlidingSelector(
                selectedIndex = destinations.indexOf(active),
                optionCount = destinations.size,
                modifier = Modifier.fillMaxSize().padding(6.dp),
                slideDuration = 1000,
                shape = CircleShape,
            ) {
                destinations.forEach {
                    BottomBarItem(
                        destination = it,
                        selected = it == active,
                        onClick = { onSelect(it) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = current != null,
            enter = fadeIn() + scaleIn() + expandHorizontally(),
            exit = fadeOut() + scaleOut() + shrinkHorizontally(),
        ) {
            lastAction?.let { action ->
                BarSurface(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(BarHeight),
                    onClick = { onActionClick(action) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = stringResource(action.description),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
    if (onClick == null) {
        Surface(modifier = modifier, shape = CircleShape, color = color, shadowElevation = 8.dp, content = content)
    } else {
        Surface(onClick = onClick, modifier = modifier, shape = CircleShape, color = color, shadowElevation = 8.dp, content = content)
    }
}

@Composable
private fun BottomBarItem(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSecondary,
        animationSpec = tween(SLIDE_DURATION),
        label = "tabContent",
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(destination.icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
//        Spacer(Modifier.height(2.dp))
//        Text(stringResource(destination.label), style = MaterialTheme.typography.labelSmall, color = content, maxLines = 1)
    }
}