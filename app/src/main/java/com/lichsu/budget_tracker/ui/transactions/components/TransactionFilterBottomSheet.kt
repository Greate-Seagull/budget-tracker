package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.domain.model.AmountStats
import com.lichsu.budget_tracker.domain.toggle
import com.lichsu.budget_tracker.ui.categories.CategoryUi
import com.lichsu.budget_tracker.ui.components.buttons.PrimaryButton
import com.lichsu.budget_tracker.ui.components.buttons.SecondaryButton
import com.lichsu.budget_tracker.ui.components.dialogs.AppBottomSheet
import com.lichsu.budget_tracker.ui.components.dialogs.DateRangeFilter
import com.lichsu.budget_tracker.ui.components.inputs.PriceRangeFilter
import com.lichsu.budget_tracker.ui.components.inputs.TextChipGroup
import com.lichsu.budget_tracker.ui.components.layout.SectionCard
import com.lichsu.budget_tracker.ui.transactions.TransactionFilter
import com.lichsu.budget_tracker.util.coerceIn

@Composable
fun TransactionFilterBottomSheet(
    categories: List<CategoryUi>,
    amounts: AmountStats,
    applied: TransactionFilter,
    onApply: (TransactionFilter) -> Unit,
    trigger: @Composable (open: () -> Unit) -> Unit,
) {
    var draft by remember(applied) { mutableStateOf(applied) }

    AppBottomSheet(title = stringResource(R.string.filter_title), trigger = trigger) { close, _ ->
        TypeSection(draft) { draft = it }
        CategorySection(categories, draft) { draft = it }
        DateSection(draft) { draft = it }
        AmountSection(amounts, draft) { draft = it }

        FilterActions(
            onReset = { draft = TransactionFilter() },
            onApply = {
                onApply(draft)
                close {}
            },
        )
    }
}

@Composable
private fun TypeSection(draft: TransactionFilter, onChange: (TransactionFilter) -> Unit) {
    SectionCard {
        TextChipGroup(
            title = stringResource(R.string.filter_type),
            items = TransactionType.entries,
            isSelected = { it in draft.types },
            onItemClick = { onChange(draft.copy(types = draft.types.toggle(it))) },
            label = { stringResource(it.labelRes) },
        )
    }
}

@Composable
private fun CategorySection(
    categories: List<CategoryUi>,
    draft: TransactionFilter,
    onChange: (TransactionFilter) -> Unit,
) {
    SectionCard {
        TextChipGroup(
            title = stringResource(R.string.filter_category),
            items = categories,
            isSelected = { it.id in draft.categoryIds },
            onItemClick = { onChange(draft.copy(categoryIds = draft.categoryIds.toggle(it.id))) },
            label = { it.name },
        )
    }
}

@Composable
private fun DateSection(draft: TransactionFilter, onChange: (TransactionFilter) -> Unit) {
    SectionCard {
        DateRangeFilter(
            title = stringResource(R.string.filter_date),
            value = draft.dateRange,
            onValueChange = { onChange(draft.copy(dateRange = it)) },
        )
    }
}

@Composable
private fun AmountSection(
    amounts: AmountStats,
    draft: TransactionFilter,
    onChange: (TransactionFilter) -> Unit,
) {
    val bounds = amounts.min.toFloat()..amounts.max.toFloat()
    SectionCard {
        PriceRangeFilter(
            title = stringResource(R.string.filter_price_range),
            histogram = amounts.histogram,
            valueRange = bounds,
            value = (draft.amountRange ?: bounds).coerceIn(bounds),
            // Chọn đủ cả khoảng = không lọc theo số tiền
            onValueChange = { onChange(draft.copy(amountRange = if (it == bounds) null else it)) },
        )
    }
}

@Composable
private fun FilterActions(onReset: () -> Unit, onApply: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SecondaryButton(stringResource(R.string.action_reset), onClick = onReset, modifier = Modifier.weight(1f))
        PrimaryButton(stringResource(R.string.action_apply), onClick = onApply, modifier = Modifier.weight(1f))
    }
}
