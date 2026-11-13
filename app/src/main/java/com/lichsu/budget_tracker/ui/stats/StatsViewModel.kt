package com.lichsu.budget_tracker.ui.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.dao.CategoryTotal
import com.lichsu.budget_tracker.data.local.dao.DailyTotal
import com.lichsu.budget_tracker.data.local.dao.MonthlyExpenseTotals
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class StatsUiState(
    val month: YearMonth = YearMonth.now(),
    val type: TransactionType = TransactionType.EXPENSE,
    val categories: List<CategoryTotal> = emptyList(),
    val daily: List<DailyTotal> = emptyList(),
    val comparison: MonthlyExpenseTotals = MonthlyExpenseTotals(0, 0),
    val loaded: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    db: AppDatabase,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val dao = db.statsDao()
    private val selectedMonth = savedState.getStateFlow("statsMonth", YearMonth.now().toString())
    private val selectedType = savedState.getStateFlow("statsType", TransactionType.EXPENSE.name)

    val uiState = combine(selectedMonth, selectedType) { month, type ->
        YearMonth.parse(month) to TransactionType.valueOf(type)
    }.flatMapLatest { (month, type) ->
        val start = month.atDay(1)
        val end = month.plusMonths(1).atDay(1)
        combine(
            dao.observeCategoryTotals(type, start, end),
            dao.observeDailyTotals(type, start, end),
            dao.observeExpenseComparison(month.minusMonths(1).atDay(1), start, end),
        ) { categories, daily, comparison ->
            StatsUiState(month, type, categories, daily, comparison, loaded = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun shiftMonth(offset: Long) {
        savedState["statsMonth"] = YearMonth.parse(selectedMonth.value).plusMonths(offset).toString()
    }

    fun selectType(type: TransactionType) {
        savedState["statsType"] = type.name
    }
}
