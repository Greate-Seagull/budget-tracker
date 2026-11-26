package com.lichsu.budget_tracker.ui.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.data.repository.CategoryRepository
import com.lichsu.budget_tracker.data.repository.CategoryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    // Lưu tên enum trong SavedStateHandle: xoay màn hình hoặc app bị hệ thống tắt vẫn nhớ đang ở tab Chi hay Thu.
    private val selectedType = savedState.getStateFlow(KEY_TYPE, TransactionType.EXPENSE.name)

    val uiState = selectedType.map(TransactionType::valueOf).flatMapLatest { type ->
        repository.observeByType(type).map { categories ->
            // sortedBy giữ nguyên thứ tự id của các dòng còn lại, chỉ đẩy "Khác" (isDefault = true) xuống cuối.
            CategoriesUiState(type, categories.sortedBy { it.isDefault }, loaded = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    fun selectType(type: TransactionType) {
        savedState[KEY_TYPE] = type.name
    }

    /** Thêm vào loại đang chọn. Đọc từ SavedStateHandle chứ không từ uiState.value vì uiState có thể chưa kịp cập nhật. */
    suspend fun add(name: String, iconKey: String, colorHex: String): CategoryResult =
        repository.add(name, TransactionType.valueOf(selectedType.value), iconKey, colorHex)

    suspend fun update(id: Long, name: String, iconKey: String, colorHex: String): CategoryResult =
        repository.update(id, name, iconKey, colorHex)

    suspend fun countTransactions(id: Long): Int = repository.countTransactions(id)

    suspend fun delete(id: Long): CategoryResult = repository.delete(id)

    private companion object {
        const val KEY_TYPE = "categoryType"
    }
}
