package com.lichsu.budget_tracker.ui.components.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.components.text.AnimatedErrorText
import com.lichsu.budget_tracker.ui.components.text.animatedErrorColor

@Composable
fun NormalRow(
    modifier: Modifier = Modifier,
    errorText: String? = null,
    onClick: () -> Unit = {},
    leading: @Composable (tint: Color) -> Unit,
    trailing: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
) {
    val tint = animatedErrorColor(errorText != null, "rowIconColor")

    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick)
                .padding(14.dp),
        ) {
            leading(tint)
            Spacer(Modifier.width(14.dp))
            Box(Modifier.weight(1f)) { title() }
            trailing()
        }
        AnimatedErrorText(errorText)
    }
}
