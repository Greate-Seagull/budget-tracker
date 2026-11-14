package com.lichsu.budget_tracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.dao.MonthlyExpenseTotals
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.ui.components.FinanceCard
import com.lichsu.budget_tracker.ui.components.FinanceMonthSwitcher
import com.lichsu.budget_tracker.ui.components.monthLabel
import com.lichsu.budget_tracker.ui.transactionform.components.Switch
import com.lichsu.budget_tracker.util.MoneyFormat
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { FinanceMonthSwitcher(state.month, viewModel::shiftMonth) }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Switch(
                    TransactionType.entries,
                    state.type,
                    viewModel::selectType,
                    labelRes = { it.labelRes },
                )
            }
        }
        when {
            !state.loaded -> item { CircularProgressIndicator() }
            state.categories.isEmpty() -> item {
                FinanceCard {
                    Text(
                        stringResource(R.string.tv3_stats_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(stringResource(R.string.tv3_stats_empty_detail))
                }
            }
            else -> {
                item { CategoryChartCard(state) }
                item {
                    FinanceCard {
                        Text(stringResource(R.string.tv3_daily_totals), style = MaterialTheme.typography.bodyLarge)
                        DailyBarChart(state.month, state.daily)
                    }
                }
            }
        }
        if (state.loaded && state.type == TransactionType.EXPENSE) {
            item { ExpenseComparison(state.month, state.comparison) }
        }
    }
}

@Composable
private fun CategoryChartCard(state: StatsUiState) {
    val total = state.categories.sumOf { it.total }
    val fallback = MaterialTheme.colorScheme.tertiary
    val colors = state.categories.map { category ->
        category.colorHex?.let { value ->
            runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrNull()
        } ?: fallback
    }
    FinanceCard {
        Text(stringResource(R.string.tv3_by_category), style = MaterialTheme.typography.bodyLarge)
        CategoryPieChart(state.categories, colors, total)
        state.categories.forEachIndexed { index, category ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(12.dp).background(colors[index], CircleShape))
                Column(Modifier.weight(1f)) {
                    Text(category.categoryName ?: stringResource(R.string.tv3_uncategorized))
                    Text(MoneyFormat.vnd(category.total), style = MaterialTheme.typography.titleSmall)
                }
                Text(MoneyFormat.percent(category.total.toDouble() / total))
            }
        }
    }
}

@Composable
private fun ExpenseComparison(month: YearMonth, totals: MonthlyExpenseTotals) {
    val delta = totals.current - totals.previous
    FinanceCard {
        Text(stringResource(R.string.tv3_comparison), style = MaterialTheme.typography.bodyLarge)
        ComparisonLine(monthLabel(month), MoneyFormat.vnd(totals.current))
        ComparisonLine(monthLabel(month.minusMonths(1)), MoneyFormat.vnd(totals.previous))
        val detail = when {
            delta == 0L -> stringResource(R.string.tv3_same_expense)
            totals.previous == 0L -> stringResource(
                R.string.tv3_increased_no_baseline,
                MoneyFormat.vnd(delta),
            )
            else -> stringResource(
                if (delta > 0) R.string.tv3_expense_increased else R.string.tv3_expense_decreased,
                MoneyFormat.vnd(abs(delta)),
                MoneyFormat.percent(abs(delta).toDouble() / totals.previous),
            )
        }
        Text(detail, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun ComparisonLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}
