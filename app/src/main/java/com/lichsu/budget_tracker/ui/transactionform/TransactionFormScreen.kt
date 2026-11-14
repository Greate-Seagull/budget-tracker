package com.lichsu.budget_tracker.ui.transactionform

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.lichsu.budget_tracker.data.local.dao.BudgetUsage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.ui.theme.BudgetTrackerTheme
import com.lichsu.budget_tracker.ui.transactionform.components.AmountInput
import com.lichsu.budget_tracker.ui.transactionform.components.CategoryPickerSheet
import com.lichsu.budget_tracker.ui.transactionform.components.ConfirmAction
import com.lichsu.budget_tracker.ui.transactionform.components.DatePickAction
import com.lichsu.budget_tracker.ui.transactionform.components.NoteInput
import com.lichsu.budget_tracker.ui.transactionform.components.Switch
import com.lichsu.budget_tracker.ui.transactionform.components.buttons.ChoosingButton
import com.lichsu.budget_tracker.ui.transactionform.components.buttons.IconButton
import com.lichsu.budget_tracker.ui.transactionform.components.buttons.PrimaryButton
import com.lichsu.budget_tracker.ui.transactionform.components.buttons.SecondaryButton
import com.lichsu.budget_tracker.ui.transactionform.components.labels.NormalText
import com.lichsu.budget_tracker.ui.util.toDisplayString
import java.time.LocalDate

@Preview(showBackground = true)
@Composable
private fun TransactionFormScreenPreview() {
    BudgetTrackerTheme {
        TransactionFormRoute()
    }
}

@Composable
fun TransactionFormRoute(
    viewModel: TransactionFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onSaved: (List<BudgetUsage>) -> Unit = { onNavigateBack() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    TransactionFormScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        viewModel::onTypeChanged,
        viewModel::onAmountChanged,
        viewModel::onDateChanged,
        onNoteChanged = viewModel::onNoteChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onSaved = viewModel::save,
        onDeleteConfirmed = viewModel::delete,
        onNavigateBack = onNavigateBack,
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TransactionFormEvent.Saved -> onSaved(event.budgetWarnings)
                TransactionFormEvent.Deleted -> onNavigateBack()

                is TransactionFormEvent.Error ->
                    snackbarHostState.showSnackbar(event.message)

                TransactionFormEvent.NotFound -> {
                    snackbarHostState.showSnackbar("Giao dịch không tồn tại")
                    onNavigateBack()
                }
            }
        }
    }
}

@Composable
fun TransactionFormScreen(
    uiState: TransactionFormUiState = TransactionFormUiState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onTypeChanged: (TransactionType) -> Unit = {},
    onAmountChanged: (String) -> Unit = {},
    onDateChanged: (LocalDate) -> Unit = {},
    onNoteChanged: (String) -> Unit = {},
    onCategoryChanged: (Long) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
) {
    BackHandler(onBack = onNavigateBack)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ){
                IconButton(R.drawable.ic_back, onNavigateBack)

                NormalText(stringResource(R.string.tv3_transaction_title))

                if (uiState.isEditing) {
                    ConfirmAction(
                        title = stringResource(R.string.tv3_transaction_delete),
                        onConfirmed = onDeleteConfirmed
                    ) {
                        open -> IconButton(R.drawable.ic_delete, onClick = open)
                    }
                } else {
                    Spacer(Modifier.size(40.dp))
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(top = 12.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ){
                SecondaryButton("Hủy bỏ", onClick = onNavigateBack, modifier = Modifier.weight(1f))

                PrimaryButton(stringResource(R.string.tv3_transaction_save), onClick = onSaved, modifier = Modifier.weight(1f))
            }
        }
    ){
        innerPadding -> Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp, horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Switch(
                TransactionType.entries,
                uiState.type,
                onTypeChanged,
                labelRes = { it.labelRes }
            )

            AmountInput(value = uiState.amountText, onAmountChanged)
            if (uiState.amountError != null) {
                Text(stringResource(R.string.tv3_invalid_amount), color = MaterialTheme.colorScheme.error)
            }

            CategoryPickerSheet(
                categories = uiState.categories,
                selectedId = uiState.selectedCategoryId,
                onSelected = onCategoryChanged
            ) {
                open -> ChoosingButton(
                    icon = R.drawable.ic_category,
                    label = "Phân loại",
                    value = uiState.categories.find { it.id == uiState.selectedCategoryId }?.name
                        ?: stringResource(R.string.tv3_select),
                    onClick = open
                )
            }

            if (uiState.categoryError) {
                Text(stringResource(R.string.tv3_select_category), color = MaterialTheme.colorScheme.error)
            }

            DatePickAction(
                initialDate = uiState.date,
                onDateSelected = onDateChanged,
            ) {
                open -> ChoosingButton(
                    icon = R.drawable.ic_calendar,
                    label = "Ngày thực hiện",
                    value = uiState.date.toDisplayString(),
                    onClick = open
                )
            }

            NoteInput(note = uiState.note, onNoteChanged)
        }
    }
}