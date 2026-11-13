package com.lichsu.budget_tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.dao.BudgetUsage
import com.lichsu.budget_tracker.data.local.entity.budgets.BudgetEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class Tv3HomeUiState(
    val month: YearMonth = YearMonth.now(),
    val totalBudget: BudgetUsage? = null,
    val income: Long = 0,
    val expense: Long = 0,
    val recent: List<TransactionEntity> = emptyList(),
    val categoryNames: Map<Long, String> = emptyMap(),
    val loaded: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class Tv3HomeViewModel @Inject constructor(
    db: AppDatabase,
    budgets: BudgetRepository,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val stats = db.statsDao()

    val uiState = month.flatMapLatest { current ->
        val start = current.atDay(1)
        val end = current.plusMonths(1).atDay(1)
        combine(
            budgets.observeUsage(current),
            stats.observeMonthlyTotals(start, end),
            stats.observeRecentInMonth(start, end),
            db.categoryDao().observeAll(),
        ) { limits, totals, recent, categories ->
            Tv3HomeUiState(
                month = current,
                totalBudget = limits.find { it.categoryId == BudgetEntity.TOTAL_CATEGORY_ID },
                income = totals.find { it.type == TransactionType.INCOME }?.total ?: 0L,
                expense = totals.find { it.type == TransactionType.EXPENSE }?.total ?: 0L,
                recent = recent,
                categoryNames = categories.associate { it.id to it.name },
                loaded = true,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Tv3HomeUiState())

    fun refreshMonth() {
        month.value = YearMonth.now()
    }
}
