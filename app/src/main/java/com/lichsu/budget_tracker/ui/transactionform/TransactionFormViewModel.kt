package com.lichsu.budget_tracker.ui.transactionform

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.data.repository.BudgetRepository
import com.lichsu.budget_tracker.data.repository.RoomCategoryRepository
import com.lichsu.budget_tracker.data.repository.RoomTransactionRepository
import com.lichsu.budget_tracker.domain.AmountError
import com.lichsu.budget_tracker.domain.AmountResult
import com.lichsu.budget_tracker.domain.AmountValidator
import com.lichsu.budget_tracker.domain.sanitizeAmountDigits
import com.lichsu.budget_tracker.ui.categories.toUi
import com.lichsu.budget_tracker.ui.navigation.TransactionForm
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val repo: RoomTransactionRepository,
    private val categoryRepo: RoomCategoryRepository,
    private val budgets: BudgetRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val ARG_TRANSACTION_ID = "transactionId"
        private const val MAX_DIGITS = 12 // 100.000.000.000 có 12 chữ số
    }

    private class ValidInput(val amount: Long, val categoryId: Long)

    private val editingId: Long? = savedStateHandle.toRoute<TransactionForm>().id
    private var original: TransactionEntity? = null

    private val _uiState = MutableStateFlow(TransactionFormUiState(isEditing = editingId != null))
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<TransactionFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeCategories()
        editingId?.let(::loadTransaction)
    }

    // ---- Form input ----

    fun onTypeChanged(type: TransactionType) {
        _uiState.update {
            if (it.type == type) it else it.copy(type = type, categoryError = false)
        }
    }

    fun onCategoryChanged(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id, categoryError = false) }
    }

    fun onAmountChanged(raw: String) {
        val digits = sanitizeAmountDigits(raw, MAX_DIGITS)
        _uiState.update { it.copy(amountText = digits, amountError = liveAmountError(digits)) }
    }

    fun onDateChanged(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNoteChanged(note: String) = _uiState.update { it.copy(note = note) }

    // ---- Actions ----

    fun save() {
        val input = _uiState.value
        if (input.isSaving) return
        val valid = validate(input) ?: return

        _uiState.update { it.copy(isSaving = true) }
        val transaction = input.toEntity(original, valid.amount, valid.categoryId)
        val isNew = original == null

        launchAction(
            name = "save",
            errorMessage = R.string.form_save_error,
            onFinally = { _uiState.update { it.copy(isSaving = false) } },
        ) {
            val persist: suspend () -> Unit = {
                if (isNew) repo.insert(transaction) else repo.update(transaction)
            }

            val warnings =
                if (transaction.type == TransactionType.EXPENSE) {
                    budgets.saveExpense(transaction, persist)
                } else {
                    persist()
                    emptyList()
                }

            TransactionFormEvent.Saved(warnings)
        }
    }

    fun delete() {
        val tx = original ?: return
        launchAction(
            name = "delete",
            errorMessage = R.string.form_delete_error,
        ) {
            repo.delete(tx)
            TransactionFormEvent.Deleted
        }
    }

    // ---- Internals ----

    private fun observeCategories() {
        viewModelScope.launch {
            _uiState
                .map { it.type }
                .distinctUntilChanged()
                .flatMapLatest { categoryRepo.observeAll() }
                .map { list -> list.map(CategoryEntity::toUi) }
                .collect { categories -> _uiState.update { it.copy(categories = categories) } }
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = repo.findById(id)
            if (transaction == null) {
                _events.send(TransactionFormEvent.NotFound)
            } else {
                original = transaction
                _uiState.update { it.withTransaction(transaction) }
            }
        }
    }

    /** Báo lỗi ngay khi gõ; ô rỗng chưa báo (chỉ báo khi bấm Lưu). */
    private fun liveAmountError(digits: String): AmountError? =
        if (digits.isEmpty()) null
        else (AmountValidator.validate(digits) as? AmountResult.Invalid)?.error

    /** Trả về dữ liệu hợp lệ, hoặc đánh dấu lỗi lên UI và trả về null. */
    private fun validate(input: TransactionFormUiState): ValidInput? {
        val amountResult = AmountValidator.validate(input.amountText)
        val categoryId = input.selectedCategoryId

        if (amountResult is AmountResult.Valid && categoryId != null) {
            return ValidInput(amountResult.value, categoryId)
        }
        _uiState.update {
            it.copy(
                amountError = (amountResult as? AmountResult.Invalid)?.error,
                categoryError = categoryId == null,
            )
        }
        return null
    }

    /** Chạy [block] và phát event nó trả về; thất bại thì phát lỗi. */
    private fun launchAction(
        name: String,
        @StringRes errorMessage: Int,
        onFinally: () -> Unit = {},
        block: suspend () -> TransactionFormEvent,
    ) {
        viewModelScope.launch {
            try {
                _events.send(block())
                Timber.i("Transaction $name succeeded")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Transaction $name failed")
                _events.send(TransactionFormEvent.Error(errorMessage))
            } finally {
                onFinally()
            }
        }
    }
}
