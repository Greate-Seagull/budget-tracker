package com.lichsu.budget_tracker.ui.components.inputs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.components.layout.SectionTitle
import com.lichsu.budget_tracker.ui.components.text.NormalText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipGroup(
    title: String,
    items: List<T>,
    isSelected: (T) -> Boolean,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    chipContent: @Composable (item: T) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle(title)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items.forEach { item ->
                ChoiceChip(
                    selected = isSelected(item),
                    onClick = { onItemClick(item) },
                ) {
                    chipContent(item)
                }
            }
        }
    }
}

@Composable
fun ChoiceChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val container by animateColorAsState(
        targetValue = if (selected) scheme.primary else scheme.surfaceContainerHigh,
        label = "chipContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
        label = "chipContent",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Checkbox },
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
fun <T> TextChipGroup(
    title: String,
    items: List<T>,
    isSelected: (T) -> Boolean,
    onItemClick: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    ChipGroup(
        title = title,
        items = items,
        isSelected = isSelected,
        onItemClick = onItemClick,
        modifier = modifier,
    ) { item ->
        NormalText(
            label = label(item),
        )
    }
}