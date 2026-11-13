package com.lichsu.budget_tracker.ui.transactionform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.data.repository.transactions.RoomTransactionRepository
import com.lichsu.budget_tracker.domain.AmountResult
import com.lichsu.budget_tracker.domain.AmountValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val repo: RoomTransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    companion object {
        const val ARG_TRANSACTION_ID = "transactionId"
        private const val MAX_DIGITS = 12
    }

    // domain
    private val editingId: Long? = savedStateHandle.get<Long>(ARG_TRANSACTION_ID)?.takeIf { it > 0 }

    private var original: TransactionEntity? = null

    // infra
    private val _uiState = MutableStateFlow(TransactionFormUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<TransactionFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Danh mục luôn lọc theo loại đang chọn
//        viewModelScope.launch {
//            _uiState
//                .map { it.type }
//                .distinctUntilChanged()
//                .flatMapLatest { repository.observeCategories(it) }
//                .collect { list -> _uiState.update { it.copy(categories = list) } }
//        }

        editingId?.let { id ->
            viewModelScope.launch {
                val transaction = repo.findById(id)
                if (transaction == null)
                    _events.send(TransactionFormEvent.NotFound)
                else {
                    original = transaction
                    _uiState.update {
                        it.copy(
                            type = transaction.type,
                            amountText = transaction.amount.toString(),
                            selectedCategoryId = transaction.categoryId,
                            date = transaction.date,
                            note = transaction.note
                        )
                    }
                }
            }
        }
    }

    fun onTypeChanged(type: TransactionType) {
        _uiState.update {
            if (it.type == type) it
            else it.copy(type = type, selectedCategoryId = null, categoryError = false) // đổi loại -> reset danh mục
        }
    }

    fun onCategoryChanged(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id, categoryError = false) }
    }

    fun onAmountChanged(raw: String) {
        // Chỉ nhận chữ số, tối đa 12 chữ số (100.000.000.000 có 12 chữ số), bỏ số 0 đứng đầu
        val filtered = raw.filter { it in '0'..'9' }
            .take(MAX_DIGITS)
        val digits = filtered.trimStart('0')
            .ifEmpty { if (filtered.isEmpty()) "" else "0" }
        _uiState.update {
            it.copy(
                amountText = digits,
                // Báo lỗi ngay khi gõ; ô rỗng chưa báo (chỉ báo khi bấm Lưu)
                amountError = if (digits.isEmpty()) null
                else (AmountValidator.validate(digits) as? AmountResult.Invalid)?.error,
            )
        }
    }

    fun onDateChanged(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNoteChanged(note: String) = _uiState.update { it.copy(note = note) }

    fun save() {
        val input = _uiState.value
        if (input.isSaving)
            return

        // Validation
        val amountResult = AmountValidator.validate(input.amountText)
        if (amountResult !is AmountResult.Valid ||
            input.selectedCategoryId == null) {
            _uiState.update {
                it.copy(
                    amountError = (amountResult as? AmountResult.Invalid)?.error,
                    categoryError = input.selectedCategoryId == null
                )
            }
            return
        }

        // Saving
        _uiState.update {
            it.copy(isSaving = true)
        }
        viewModelScope.launch {
            try {
                val transaction = TransactionEntity(
                    id = original?.id ?: 0,
                    type = input.type,
                    amount = amountResult.value,
                    categoryId = input.selectedCategoryId,
                    date = input.date,
                    note = input.note,
                    createdAt = original?.createdAt ?: System.currentTimeMillis()
                )
                if (original == null)
                    repo.insert(transaction)
                else
                    repo.update(transaction)

                // TODO(F05): khi TV3 có BudgetChecker, gọi ở đây (khoảng 3 dòng):
                //   if (s.type == TxType.EXPENSE) {
                //       val status = budgetChecker.check(categoryId, YearMonth.from(s.date))
                //       // truyền status cho màn danh sách để hiện Snackbar
                //   }

                _events.send(TransactionFormEvent.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Transaction save failed")
                _events.send(TransactionFormEvent.Error("Không thể lưu, thử lại sau"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun delete() {
        val tx = original ?: return
        viewModelScope.launch {
            try {
                repo.delete(tx)
                _events.send(TransactionFormEvent.Deleted)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Transaction delete failed")
                _events.send(TransactionFormEvent.Error("Không thể xóa, thử lại sau"))
            }
        }
    }
}