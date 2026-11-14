package com.lichsu.budget_tracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.ui.budget.BudgetSummaryCard
import com.lichsu.budget_tracker.ui.components.FinanceCard
import com.lichsu.budget_tracker.ui.components.monthLabel
import com.lichsu.budget_tracker.ui.transactionform.components.buttons.PrimaryButton
import com.lichsu.budget_tracker.ui.util.toDisplayString
import com.lichsu.budget_tracker.util.MoneyFormat

@Composable
fun Tv3HomeRoute(
    onOpenBudget: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: Tv3HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleStartEffect(viewModel) {
        viewModel.refreshMonth()
        onStopOrDispose {}
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PrimaryButton(
                label = stringResource(R.string.tv3_add_transaction),
                onClick = onAddTransaction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!state.loaded) {
            item { CircularProgressIndicator() }
        } else {
            item {
                FinanceCard {
                    Text(monthLabel(state.month), style = MaterialTheme.typography.bodyLarge)
                    HomeTotal(stringResource(R.string.transaction_income), state.income)
                    HomeTotal(stringResource(R.string.transaction_expense), state.expense)
                    HomeTotal(stringResource(R.string.tv3_balance), state.income - state.expense)
                }
            }
            item { BudgetSummaryCard(state.totalBudget, onOpenBudget = onOpenBudget) }
            item { Text(stringResource(R.string.tv3_recent), style = MaterialTheme.typography.bodyLarge) }
            if (state.recent.isEmpty()) {
                item { Text(stringResource(R.string.tv3_stats_empty)) }
            }
            items(state.recent, key = { it.id }) { transaction ->
                val name = state.categoryNames[transaction.categoryId]
                    ?: stringResource(R.string.tv3_uncategorized)
                FinanceCard {
                    val signedAmount = if (transaction.type == TransactionType.EXPENSE)
                        -transaction.amount else transaction.amount
                    Text(transaction.note.ifBlank { name }, style = MaterialTheme.typography.bodyLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(transaction.date.toDisplayString())
                        Text(MoneyFormat.vnd(signedAmount))
                    }
                    TextButton(onClick = { onEditTransaction(transaction.id) }) {
                        Text(stringResource(R.string.tv3_edit))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTotal(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(MoneyFormat.vnd(amount), style = MaterialTheme.typography.titleSmall)
    }
}
