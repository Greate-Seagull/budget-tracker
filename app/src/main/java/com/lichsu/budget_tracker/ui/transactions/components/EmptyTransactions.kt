package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.layout.EmptyState

@Composable
fun EmptyTransactions(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        icon = Icons.Outlined.ReceiptLong,
        message = stringResource(R.string.transactions_empty),
        actionLabel = stringResource(R.string.add_first_transaction),
        onAction = onAdd,
        modifier = modifier,
    )
}
