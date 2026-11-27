package com.lichsu.budget_tracker.ui.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
private fun AppButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        enabled = enabled,
    ) {
        Text(label, color = contentColor, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    AppButton(label, onClick, modifier, enabled, scheme.primary, scheme.onPrimary)
}

@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    AppButton(label, onClick, modifier, enabled, scheme.surfaceContainerHighest, scheme.onBackground)
}

@Composable
fun IconButton(
    icon: Painter,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
    ) {
        NormalIcon(icon)
    }
}

@Composable
fun IconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
    ) {
        NormalIcon(icon)
    }
}
