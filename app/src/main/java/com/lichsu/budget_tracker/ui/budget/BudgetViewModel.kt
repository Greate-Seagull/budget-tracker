package com.lichsu.budget_tracker.ui.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichsu.budget_tracker.data.local.dao.BudgetUsage
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class BudgetUiState(
    val month: YearMonth = YearMonth.now(),
    val usage: List<BudgetUsage> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val loaded: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val selectedMonth = savedState.getStateFlow("budgetMonth", YearMonth.now().toString())

    val uiState = selectedMonth.map(YearMonth::parse).flatMapLatest { month ->
        combine(repository.observeUsage(month), repository.observeExpenseCategories()) { usage, categories ->
            BudgetUiState(month, usage, categories, loaded = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    fun shiftMonth(offset: Long) {
        savedState["budgetMonth"] = YearMonth.parse(selectedMonth.value).plusMonths(offset).toString()
    }

    suspend fun setLimit(categoryId: Long, limit: Long?) = repository.setLimit(categoryId, limit)
}
