package com.lichsu.budget_tracker.ui.transactionform.components.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.transactionform.components.CircleIcon
import com.lichsu.budget_tracker.ui.transactionform.components.labels.NormalText

@Composable
fun ChoosingButton(
    icon: Int,
    label: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        // Trái
        CircleIcon(icon)

        Spacer(Modifier.width(14.dp))

        NormalText(label, Modifier.weight(1f))

        // Phải
        NormalText(value)

        Spacer(Modifier.width(10.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}