package com.lichsu.budget_tracker.ui.transactionform.components.labels

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Title(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium
    )
}