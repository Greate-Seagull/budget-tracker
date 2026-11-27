package com.lichsu.budget_tracker.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichsu.budget_tracker.ui.components.layout.MonthSwitcher
import com.lichsu.budget_tracker.ui.components.buttons.NormalIcon
import com.lichsu.budget_tracker.ui.transactions.components.EmptyTransactions
import com.lichsu.budget_tracker.ui.transactions.components.MonthSummary
import com.lichsu.budget_tracker.ui.transactions.components.TransactionList
import com.lichsu.budget_tracker.ui.transactions.components.TransactionsTopBar
import java.time.YearMonth

@Composable
fun TransactionsRoute(
    viewModel: TransactionsViewModel = hiltViewModel(),
    onAddTransaction: () -> Unit = {},
    onModifyTransaction: (id: Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    TransactionsScreen(
        uiState = uiState,
        filter = filter,
        query = query,
        onFilterApplied = viewModel::onFilterApplied,
        onQueryChange = viewModel::onQueryChange,
        onMonthSelected = viewModel::onMonthSelected,
        onAddTransaction = onAddTransaction,
        onModifyTransaction = onModifyTransaction,
    )
}

@Composable
fun TransactionsScreen(
    uiState: TransactionsUiState,
    filter: TransactionFilter,
    query: String,
    onFilterApplied: (TransactionFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    onAddTransaction: () -> Unit,
    onModifyTransaction: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TransactionsTopBar(
                query = query,
                onQueryChange = onQueryChange,
                categories = uiState.categories,
                amounts = uiState.amounts,
                filter = filter,
                onFilterApplied = onFilterApplied,
            )
        },
        floatingActionButton = { AddTransactionFab(onClick = onAddTransaction) },
        floatingActionButtonPosition = FabPosition.Center,
    ) { inner ->
        TransactionsContent(
            uiState = uiState,
            onMonthSelected = onMonthSelected,
            onAddTransaction = onAddTransaction,
            onModifyTransaction = onModifyTransaction,
            modifier = Modifier.padding(inner),
        )
    }
}

@Composable
private fun AddTransactionFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        NormalIcon(rememberVectorPainter(Icons.Default.Add))
    }
}

@Composable
private fun TransactionsContent(
    uiState: TransactionsUiState,
    onMonthSelected: (YearMonth) -> Unit,
    onAddTransaction: () -> Unit,
    onModifyTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 10.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MonthSummary(uiState.monthTotal)
        MonthSwitcher(selected = uiState.selectedMonth, onSelected = onMonthSelected)
        when {
            uiState.isLoading -> { /* Tải khá nhanh */ }
            uiState.isEmpty -> EmptyTransactions(onAdd = onAddTransaction)
            else -> TransactionList(
                items = uiState.items,
                selectedMonth = uiState.selectedMonth,
                today = uiState.today,
                onMonthChanged = onMonthSelected,
                onRowClicked = onModifyTransaction,
            )
        }
    }
}
