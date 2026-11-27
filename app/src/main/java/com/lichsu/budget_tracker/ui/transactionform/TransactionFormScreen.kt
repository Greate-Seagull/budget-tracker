package com.lichsu.budget_tracker.ui.transactionform

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.ui.components.buttons.ChoosingButton
import com.lichsu.budget_tracker.ui.components.buttons.IconButton
import com.lichsu.budget_tracker.ui.components.buttons.PrimaryButton
import com.lichsu.budget_tracker.ui.components.buttons.SecondaryButton
import com.lichsu.budget_tracker.ui.components.dialogs.ConfirmDialog
import com.lichsu.budget_tracker.ui.components.dialogs.DatePickAction
import com.lichsu.budget_tracker.ui.components.inputs.AmountField
import com.lichsu.budget_tracker.ui.components.inputs.SegmentedSwitch
import com.lichsu.budget_tracker.ui.components.text.NormalText
import com.lichsu.budget_tracker.ui.transactionform.components.CategoryPickerSheet
import com.lichsu.budget_tracker.ui.transactionform.components.NoteInput
import com.lichsu.budget_tracker.ui.util.toDisplayString
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Composable
fun TransactionFormRoute(
    viewModel: TransactionFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNotFound: (message: String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectFormEvents(
        events = viewModel.events,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNotFound = onNotFound,
    )

    TransactionFormScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onTypeChanged = viewModel::onTypeChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onDateChanged = viewModel::onDateChanged,
        onNoteChanged = viewModel::onNoteChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onNavigateBack = onNavigateBack,
        onSaved = viewModel::save,
        onDeleteConfirmed = viewModel::delete,
    )
}

@Composable
private fun CollectFormEvents(
    events: Flow<TransactionFormEvent>,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNotFound: (String) -> Unit,
) {
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val currentOnNotFound by rememberUpdatedState(onNotFound)
    val notFoundMessage = stringResource(R.string.form_not_found)
    val context = LocalContext.current

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                TransactionFormEvent.NotFound -> currentOnNotFound(notFoundMessage)

                TransactionFormEvent.Deleted -> currentOnNavigateBack()

                is TransactionFormEvent.Saved -> onSaved(event.budgetWarnings)

                is TransactionFormEvent.Error -> {
                    snackbarHostState.showSnackbar(context.getString(event.messageRes))
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
            TransactionFormTopBar(
                isEditing = uiState.isEditing,
                onNavigateBack = onNavigateBack,
                onDeleteConfirmed = onDeleteConfirmed,
            )
        },
        bottomBar = {
            TransactionFormBottomBar(
                isSaving = uiState.isSaving,
                onCancel = onNavigateBack,
                onSave = onSaved,
            )
        },
    ) { innerPadding ->
        TransactionFormContent(
            uiState = uiState,
            onTypeChanged = onTypeChanged,
            onAmountChanged = onAmountChanged,
            onCategoryChanged = onCategoryChanged,
            onDateChanged = onDateChanged,
            onNoteChanged = onNoteChanged,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun TransactionFormTopBar(
    isEditing: Boolean,
    onNavigateBack: () -> Unit,
    onDeleteConfirmed: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
    ) {
        IconButton(R.drawable.ic_back, onNavigateBack)

        NormalText(if (isEditing) stringResource(R.string.form_title_edit) else stringResource(R.string.form_title_new))

        if (isEditing) {
            ConfirmDialog(title = stringResource(R.string.form_delete_title), onConfirmed = onDeleteConfirmed) { open ->
                IconButton(R.drawable.ic_delete, onClick = open)
            }
        } else {
            Spacer(Modifier.size(40.dp)) // giữ tiêu đề ở giữa
        }
    }
}

@Composable
private fun TransactionFormBottomBar(
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(top = 12.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SecondaryButton(
            stringResource(R.string.action_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = !isSaving,
        )
        PrimaryButton(
            if (isSaving) stringResource(R.string.action_saving) else stringResource(R.string.action_save),
            onClick = onSave,
            modifier = Modifier.weight(1f),
            enabled = !isSaving,
        )
    }
}

@Composable
private fun TransactionFormContent(
    uiState: TransactionFormUiState,
    onTypeChanged: (TransactionType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCategoryChanged: (Long) -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SegmentedSwitch(
            options = TransactionType.entries,
            selected = uiState.type,
            onValueChanged = onTypeChanged,
            labelRes = { it.labelRes },
        )

        AmountField(
            value = uiState.amountText,
            onValueChange = onAmountChanged,
            errorText = uiState.amountError?.name, // TODO: đổi sang chuỗi hiển thị cho người dùng
        )

        CategoryField(uiState, onCategoryChanged)
        DateField(uiState.date, onDateChanged)

        NoteInput(note = uiState.note, onNoteChange = onNoteChanged)
    }
}

@Composable
private fun CategoryField(uiState: TransactionFormUiState, onSelected: (Long) -> Unit) {
    CategoryPickerSheet(
        categories = uiState.categories,
        selectedId = uiState.selectedCategoryId,
        onSelected = onSelected,
    ) { open ->
        ChoosingButton(
            icon = R.drawable.ic_category,
            label = stringResource(R.string.form_category_label),
            value = uiState.selectedCategory?.name ?: stringResource(R.string.form_category_placeholder),
            errorText = if (uiState.categoryError) stringResource(R.string.form_category_error) else null,
            onClick = open,
        )
    }
}

@Composable
private fun DateField(date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    DatePickAction(initialDate = date, onDateSelected = onDateSelected) { open ->
        ChoosingButton(
            icon = R.drawable.ic_calendar,
            label = stringResource(R.string.form_date_label),
            value = date.toDisplayString(),
            onClick = open,
        )
    }
}
