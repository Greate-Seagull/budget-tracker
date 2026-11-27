package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.domain.model.AmountStats
import com.lichsu.budget_tracker.ui.categories.CategoryUi
import com.lichsu.budget_tracker.ui.components.inputs.ExpandableSearchButton
import com.lichsu.budget_tracker.ui.components.buttons.IconButton
import com.lichsu.budget_tracker.ui.components.inputs.SearchExpandDirection
import com.lichsu.budget_tracker.ui.transactions.TransactionFilter

@Composable
fun TransactionsTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    categories: List<CategoryUi>,
    amounts: AmountStats,
    filter: TransactionFilter,
    onFilterApplied: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
    ) {
        IconButton(rememberVectorPainter(Icons.Filled.BarChart))

        ExpandableSearchButton(
            query = query,
            onQueryChange = onQueryChange,
            expandDirection = SearchExpandDirection.Start,
            modifier = Modifier.weight(1f),
        )

        TransactionFilterBottomSheet(
            categories = categories,
            amounts = amounts,
            applied = filter,
            onApply = onFilterApplied,
        ) { open ->
            IconButton(
                icon = rememberVectorPainter(Icons.Filled.FilterList),
                onClick = open,
            )
        }
    }
}
