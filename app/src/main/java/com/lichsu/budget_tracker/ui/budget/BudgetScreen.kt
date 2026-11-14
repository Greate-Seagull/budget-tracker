package com.lichsu.budget_tracker.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.lichsu.budget_tracker.data.local.entity.budgets.BudgetEntity
import com.lichsu.budget_tracker.ui.components.FinanceCard
import com.lichsu.budget_tracker.ui.components.FinanceMonthSwitcher
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun BudgetScreen(viewModel: BudgetViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingCategory by rememberSaveable { mutableStateOf<Long?>(null) }
    val totalId = BudgetEntity.TOTAL_CATEGORY_ID
    val usageById = state.usage.associateBy { it.categoryId }
    val edit = stringResource(R.string.tv3_edit)
    val setLimit = stringResource(R.string.tv3_set_limit)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { FinanceMonthSwitcher(state.month, viewModel::shiftMonth) }
        if (!state.loaded) {
            item { CircularProgressIndicator() }
        } else {
            item {
                val total = usageById[totalId]
                FinanceCard {
                    Text(stringResource(R.string.tv3_total_limit), style = MaterialTheme.typography.bodyLarge)
                    if (total == null) Text(stringResource(R.string.tv3_no_total_limit))
                    else BudgetProgress(total)
                    TextButton(onClick = { editingCategory = totalId }) {
                        Text(if (total == null) setLimit else edit)
                    }
                }
            }
            item { Text(stringResource(R.string.tv3_category_limits), style = MaterialTheme.typography.titleMedium) }
            if (state.categories.isEmpty()) {
                item { Text(stringResource(R.string.tv3_no_expense_categories)) }
            }
            items(state.categories, key = { it.id }) { category ->
                val usage = usageById[category.id]
                FinanceCard {
                    Text(category.name, style = MaterialTheme.typography.bodyLarge)
                    if (usage == null) Text(stringResource(R.string.tv3_no_limit))
                    else BudgetProgress(usage)
                    TextButton(onClick = { editingCategory = category.id }) {
                        Text(if (usage == null) setLimit else edit)
                    }
                }
            }
        }
    }

    val id = editingCategory
    if (id != null && state.loaded) {
        val name = if (id == totalId) stringResource(R.string.tv3_total_limit)
        else state.categories.find { it.id == id }?.name
        if (name != null) {
            BudgetLimitDialog(
                title = name,
                initialLimit = usageById[id]?.limitAmount,
                onDismiss = { editingCategory = null },
                onSave = { viewModel.setLimit(id, it) },
            )
        }
    }
}

@Composable
private fun BudgetLimitDialog(
    title: String,
    initialLimit: Long?,
    onDismiss: () -> Unit,
    onSave: suspend (Long?) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(initialLimit?.toString().orEmpty()) }
    var saving by remember { mutableStateOf(false) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val amount = text.toLongOrNull()
    val invalid = text.isNotBlank() && (amount == null || amount <= 0)
    fun submit(value: Long?) {
        if (saving) return
        saving = true
        saveFailed = false
        scope.launch {
            try {
                onSave(value)
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

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { value ->
                    if (value.all { it in '0'..'9' }) text = value
                    saveFailed = false
                },
                label = { Text(stringResource(R.string.tv3_limit_input)) },
                supportingText = {
                    Text(
                        stringResource(
                            when {
                                invalid -> R.string.tv3_invalid_limit
                                saveFailed -> R.string.tv3_save_failed
                                else -> R.string.tv3_limit_optional
                            },
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = invalid || saveFailed,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !saving,
            )
        },
        confirmButton = {
            TextButton(onClick = { submit(amount) }, enabled = !saving && !invalid) {
                Text(stringResource(R.string.tv3_save))
            }
        },
        dismissButton = {
            if (initialLimit != null) {
                TextButton(onClick = { submit(null) }, enabled = !saving) {
                    Text(stringResource(R.string.tv3_remove_limit))
                }
            }
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text(stringResource(R.string.tv3_cancel))
            }
        },
    )
}
