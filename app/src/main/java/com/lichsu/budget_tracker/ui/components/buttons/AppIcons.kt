package com.lichsu.budget_tracker.ui.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun NormalIcon(painter: Painter) {
    Icon(
        painter = painter,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.size(24.dp),
    )
}

@Composable
fun NormalIcon(@DrawableRes icon: Int) {
    NormalIcon(painter = painterResource(icon))
}

@Composable
fun NormalIcon(imageVector: ImageVector) {
    NormalIcon(painter = rememberVectorPainter(imageVector))
}

@Composable
fun CircleIcon(
    icon: Painter,
    background: Color = MaterialTheme.colorScheme.primary,
    onBackground: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = onBackground,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun CircleIcon(
    @DrawableRes icon: Int,
    background: Color = MaterialTheme.colorScheme.primary,
    onBackground: Color = MaterialTheme.colorScheme.onPrimary,
) {
    CircleIcon(painterResource(icon), background, onBackground)
}
