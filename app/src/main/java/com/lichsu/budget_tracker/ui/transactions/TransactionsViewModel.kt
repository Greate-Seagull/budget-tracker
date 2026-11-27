package com.lichsu.budget_tracker.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichsu.budget_tracker.data.repository.RoomCategoryRepository
import com.lichsu.budget_tracker.data.repository.RoomTransactionRepository
import com.lichsu.budget_tracker.domain.model.buildAmountStats
import com.lichsu.budget_tracker.ui.categories.CategoryUi
import com.lichsu.budget_tracker.ui.categories.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: RoomTransactionRepository,
    private val catRepo: RoomCategoryRepository,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()   // cho ô nhập, tức thì

    fun onMonthSelected(month: YearMonth) { selectedMonth.value = month }
    fun onQueryChange(q: String) { _query.value = q }
    fun onFilterApplied(filter: TransactionFilter) { _filter.value = filter }

    private val normalizedQuery: Flow<String> =
        _query
            .debounce(250.milliseconds)
            .map { it.toSearchQuery() }
            .distinctUntilChanged()

    private val allTransactions: Flow<List<TransactionUi>> =
        repository.observeAll().map { list -> list.map { it.toUi() } }

    private val items: Flow<List<TransactionListItem>> =
        combine(allTransactions, normalizedQuery, filter) { txs, q, f ->
            txs.filterBy(q, f).toListItems()
        }.flowOn(Dispatchers.Default)

    private val categories: Flow<List<CategoryUi>> =
        catRepo.observeAll()
            .map { list -> list.map { it.toUi() } }
            .distinctUntilChanged()

    private val amounts =
        allTransactions
            .map { list -> buildAmountStats(list.map { abs(it.amount) }) }
            .flowOn(Dispatchers.Default)

    val uiState: StateFlow<TransactionsUiState> =
        combine(items, selectedMonth, categories, amounts) { list, month, cats, ams ->
            TransactionsUiState(
                selectedMonth = month,
                today = LocalDate.now(),
                items = list,
                categories = cats,
                amounts = ams,
                isLoading = false,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TransactionsUiState(selectedMonth.value, LocalDate.now()),
        )
}
