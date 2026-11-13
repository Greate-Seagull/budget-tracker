package com.lichsu.budget_tracker.ui.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.recurring.RecurringRuleEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.domain.AmountResult
import com.lichsu.budget_tracker.domain.AmountValidator
import com.lichsu.budget_tracker.ui.components.FinanceCard
import com.lichsu.budget_tracker.ui.transactionform.components.CategoryPickerSheet
import com.lichsu.budget_tracker.ui.transactionform.components.CategoryUi
import com.lichsu.budget_tracker.ui.transactionform.components.Switch
import com.lichsu.budget_tracker.ui.transactionform.components.buttons.ChoosingButton
import com.lichsu.budget_tracker.ui.transactionform.components.buttons.PrimaryButton
import com.lichsu.budget_tracker.util.MoneyFormat
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun RecurringScreen(viewModel: RecurringViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleting by remember { mutableStateOf(false) }
    var deleteFailed by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val names = state.categories.associate { it.id to it.name }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (!state.loaded) {
            item { CircularProgressIndicator() }
        } else {
            item {
                PrimaryButton(
                    label = stringResource(R.string.tv3_add_recurring),
                    onClick = { editingId = 0L },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (state.rules.isEmpty()) {
                item {
                    FinanceCard {
                        Text(stringResource(R.string.tv3_recurring_empty), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.tv3_recurring_empty_detail))
                    }
                }
            }
            items(state.rules, key = { it.id }) { rule ->
                FinanceCard {
                    Text(
                        rule.note.ifBlank {
                            names[rule.categoryId] ?: stringResource(R.string.tv3_uncategorized)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(stringResource(rule.type.labelRes) + " · " + MoneyFormat.vnd(rule.amount))
                    Text(stringResource(R.string.tv3_recurring_day, rule.dayOfMonth))
                    Row {
                        TextButton(onClick = { editingId = rule.id }) { Text(stringResource(R.string.tv3_edit)) }
                        TextButton(onClick = {
                            deleteFailed = false
                            deletingId = rule.id
                        }) { Text(stringResource(R.string.tv3_delete)) }
                    }
                }
            }
        }
    }

    val editId = editingId
    val existing = state.rules.find { it.id == editId }
    if (state.loaded && (editId == 0L || existing != null)) {
        key(editId) {
            RecurringDialog(
                existing = existing,
                categories = state.categories,
                onDismiss = { editingId = null },
                onSave = viewModel::save,
            )
        }
    }

    val toDelete = state.rules.find { it.id == deletingId }
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { if (!deleting) deletingId = null },
            title = { Text(stringResource(R.string.tv3_delete_recurring)) },
            text = {
                Text(stringResource(if (deleteFailed) R.string.tv3_delete_failed else R.string.tv3_delete_recurring_detail))
            },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        deleting = true
                        scope.launch {
                            try {
                                viewModel.delete(toDelete)
                                deletingId = null
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (_: Exception) {
                                deleteFailed = true
                            } finally {
                                deleting = false
                            }
                        }
                    },
                ) { Text(stringResource(R.string.tv3_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }, enabled = !deleting) {
                    Text(stringResource(R.string.tv3_cancel))
                }
            },
        )
    }
}

@Composable
private fun RecurringDialog(
    existing: RecurringRuleEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: suspend (RecurringRuleEntity) -> Unit,
) {
    var typeName by rememberSaveable { mutableStateOf((existing?.type ?: TransactionType.EXPENSE).name) }
    var categoryId by rememberSaveable { mutableStateOf(existing?.categoryId) }
    var amountText by rememberSaveable { mutableStateOf(existing?.amount?.toString().orEmpty()) }
    var dayText by rememberSaveable { mutableStateOf(existing?.dayOfMonth?.toString().orEmpty()) }
    var note by rememberSaveable { mutableStateOf(existing?.note.orEmpty()) }
    var saving by remember { mutableStateOf(false) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val type = TransactionType.valueOf(typeName)
    val options = categories.filter { it.type == type }.map { CategoryUi(it.id, it.name, R.drawable.ic_category) }
    val selected = options.find { it.id == categoryId }
    val amount = (AmountValidator.validate(amountText) as? AmountResult.Valid)?.value
    val day = dayText.toIntOrNull()?.takeIf { it in 1..31 }
    val invalidAmount = amountText.isNotEmpty() && amount == null
    val invalidDay = dayText.isNotEmpty() && day == null

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = {
            Text(stringResource(if (existing == null) R.string.tv3_add_recurring else R.string.tv3_edit_recurring))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Switch(TransactionType.entries, type, { value ->
                    if (!saving && value != type) {
                        typeName = value.name
                        categoryId = null
                    }
                }, labelRes = { it.labelRes })
                CategoryPickerSheet(options, categoryId, { categoryId = it }) { open ->
                    ChoosingButton(
                        icon = R.drawable.ic_category,
                        label = stringResource(R.string.tv3_category),
                        value = selected?.name ?: stringResource(R.string.tv3_select),
                        onClick = { if (!saving) open() },
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.all { it in '0'..'9' }) amountText = value
                    },
                    label = { Text(stringResource(R.string.tv3_amount_input)) },
                    supportingText = if (invalidAmount) {
                        { Text(stringResource(R.string.tv3_invalid_amount)) }
                    } else null,
                    singleLine = true,
                    isError = invalidAmount,
                    enabled = !saving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = dayText,
                    onValueChange = { value ->
                        if (value.length <= 2 && value.all { it in '0'..'9' }) dayText = value
                    },
                    label = { Text(stringResource(R.string.tv3_day_input)) },
                    supportingText = {
                        Text(stringResource(if (invalidDay) R.string.tv3_invalid_day else R.string.tv3_short_month))
                    },
                    singleLine = true,
                    isError = invalidDay,
                    enabled = !saving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.tv3_note)) },
                    enabled = !saving,
                    maxLines = 3,
                )
                if (saveFailed) {
                    Text(stringResource(R.string.tv3_save_failed), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && selected != null && amount != null && day != null,
                onClick = {
                    if (selected != null && amount != null && day != null && !saving) {
                        saving = true
                        saveFailed = false
                        scope.launch {
                            try {
                                onSave(
                                    RecurringRuleEntity(
                                        id = existing?.id ?: 0,
                                        type = type,
                                        amount = amount,
                                        categoryId = selected.id,
                                        note = note.trim(),
                                        dayOfMonth = day,
                                    ),
                                )
                                onDismiss()
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (_: Exception) {
                                saveFailed = true
                            } finally {
                                saving = false
                            }
                        }
                    }
                },
            ) { Text(stringResource(R.string.tv3_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text(stringResource(R.string.tv3_cancel)) }
        },
    )
}
