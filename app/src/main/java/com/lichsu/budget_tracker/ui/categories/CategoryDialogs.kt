package com.lichsu.budget_tracker.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.data.repository.CategoryResult
import com.lichsu.budget_tracker.domain.CategoryNameError
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/** Giới hạn ở ô nhập để tên vừa một dòng; kiểm rỗng và trùng nằm ở CategoryRepository. */
private const val MAX_NAME_LENGTH = 40

/**
 * Hộp thoại thêm (existing = null) hoặc sửa danh mục: tên, biểu tượng, màu.
 * Mọi chữ đang nhập dùng rememberSaveable nên xoay màn hình không mất (SPEC §7).
 */
@Composable
internal fun CategoryDialog(
    existing: CategoryEntity?,
    type: TransactionType,
    onDismiss: () -> Unit,
    onSave: suspend (name: String, iconKey: String, colorHex: String) -> CategoryResult,
    onSaved: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var iconKey by rememberSaveable { mutableStateOf(existing?.iconKey ?: CategoryIcons.FALLBACK_KEY) }
    var colorHex by rememberSaveable { mutableStateOf(existing?.colorHex ?: CategoryColors.palette.first()) }
    var nameError by rememberSaveable { mutableStateOf<CategoryNameError?>(null) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    // Không lưu qua xoay màn hình: coroutine đang lưu cũng bị hủy cùng hộp thoại cũ.
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (saving) return
        saving = true
        saveFailed = false
        scope.launch {
            try {
                when (val result = onSave(name, iconKey, colorHex)) {
                    CategoryResult.Success -> onSaved()
                    is CategoryResult.InvalidName -> nameError = result.error
                    // Danh mục vừa bị xóa ở chỗ khác; DefaultCannotBeDeleted không xảy ra khi lưu.
                    CategoryResult.NotFound, CategoryResult.DefaultCannotBeDeleted -> onDismiss()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Timber.e(exception, "Không lưu được danh mục")
                saveFailed = true
            } finally {
                saving = false
            }
        }
    }

    val title = when {
        existing != null -> R.string.tv1_category_edit
        type == TransactionType.INCOME -> R.string.tv1_category_add_income
        else -> R.string.tv1_category_add_expense
    }
    val errorMessage = when {
        nameError == CategoryNameError.EMPTY -> R.string.tv1_category_error_empty
        nameError == CategoryNameError.DUPLICATE -> R.string.tv1_category_error_duplicate
        saveFailed -> R.string.tv1_category_save_failed
        else -> null
    }
    val selectedColor = Color(CategoryColors.argbOrFallback(colorHex))

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(title)) },
        text = {
            // Cuộn được: màn hình nằm ngang thấp, lưới biểu tượng không vừa.
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { value ->
                        if (value.length <= MAX_NAME_LENGTH) name = value
                        nameError = null
                        saveFailed = false
                    },
                    label = { Text(stringResource(R.string.tv1_category_name)) },
                    supportingText = { errorMessage?.let { Text(stringResource(it)) } },
                    isError = errorMessage != null,
                    singleLine = true,
                    enabled = !saving,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(stringResource(R.string.tv1_category_icon), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryIcons.all.forEach { (key, icon) ->
                        val selected = key == iconKey
                        ChoiceCircle(
                            selected = selected,
                            enabled = !saving,
                            background = if (selected) selectedColor else MaterialTheme.colorScheme.secondary,
                            onClick = { iconKey = key },
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                Text(stringResource(R.string.tv1_category_color), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryColors.palette.forEach { hex ->
                        val selected = hex.equals(colorHex, ignoreCase = true)
                        ChoiceCircle(
                            selected = selected,
                            enabled = !saving,
                            background = Color(CategoryColors.argbOrFallback(hex)),
                            onClick = { colorHex = hex },
                        ) {
                            if (selected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }, enabled = !saving) { Text(stringResource(R.string.tv1_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text(stringResource(R.string.tv1_cancel)) }
        },
    )
}

/** Ô tròn chọn được, dùng cho cả lưới biểu tượng và lưới màu. */
@Composable
private fun ChoiceCircle(
    selected: Boolean,
    enabled: Boolean,
    background: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            // selectable + Role.RadioButton: TalkBack đọc được "đã chọn / chưa chọn".
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/**
 * Hỏi trước khi xóa (SPEC F03, §7): nói rõ bao nhiêu giao dịch sẽ chuyển sang "Khác".
 * Việc chuyển giao dịch, quy tắc lặp và xóa hạn mức nằm trong CategoryRepository.delete (một transaction).
 */
@Composable
internal fun DeleteCategoryDialog(
    category: CategoryEntity,
    defaultName: String,
    countTransactions: suspend (Long) -> Int,
    onDelete: suspend () -> CategoryResult,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
) {
    var counted by remember { mutableStateOf(false) }
    var count by remember { mutableStateOf<Int?>(null) } // null sau khi đếm = đếm lỗi
    var deleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(category.id) {
        count = try {
            countTransactions(category.id)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Timber.e(exception, "Không đếm được giao dịch của danh mục")
            null
        }
        counted = true
    }

    fun confirm() {
        if (deleting) return
        deleting = true
        errorMessage = null
        scope.launch {
            try {
                when (onDelete()) {
                    CategoryResult.Success -> onDeleted()
                    CategoryResult.DefaultCannotBeDeleted -> errorMessage = R.string.tv1_category_delete_default
                    CategoryResult.NotFound, is CategoryResult.InvalidName -> onDismiss()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Timber.e(exception, "Không xóa được danh mục")
                errorMessage = R.string.tv1_category_delete_failed
            } finally {
                deleting = false
            }
        }
    }

    val total = count
    val movesText = when {
        !counted -> stringResource(R.string.tv1_category_delete_counting)
        total == null -> stringResource(R.string.tv1_category_delete_moves_unknown, defaultName)
        total == 0 -> stringResource(R.string.tv1_category_delete_no_transactions)
        else -> stringResource(R.string.tv1_category_delete_moves, total, defaultName)
    }

    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        title = { Text(stringResource(R.string.tv1_category_delete_title, category.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(movesText)
                Text(stringResource(R.string.tv1_category_delete_side_effects, defaultName))
                errorMessage?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirm() },
                // Chờ đếm xong để người dùng thấy con số trước khi bấm.
                enabled = counted && !deleting,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(R.string.tv1_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !deleting) { Text(stringResource(R.string.tv1_cancel)) }
        },
    )
}
