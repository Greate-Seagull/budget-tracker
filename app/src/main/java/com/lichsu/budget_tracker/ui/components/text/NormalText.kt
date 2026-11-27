package com.lichsu.budget_tracker.ui.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun NormalText(
    label: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    Text(text = label, color = color, modifier = modifier, style = style)
}

@Composable
fun Title(label: String, modifier: Modifier = Modifier) {
    NormalText(label, modifier, style = MaterialTheme.typography.titleMedium)
}
