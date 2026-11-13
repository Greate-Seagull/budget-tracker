package com.lichsu.budget_tracker.ui.transactionform.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun <T : Enum<T>> Switch(
    options: List<T>,
    selected: T,
    onValueChanged: (T) -> Unit,
    labelRes: (T) -> Int,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .width(IntrinsicSize.Max)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onValueChanged(option) }
                    .padding(horizontal = 30.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(labelRes(option)),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}