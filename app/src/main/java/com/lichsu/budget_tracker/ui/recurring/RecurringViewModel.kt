package com.lichsu.budget_tracker.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.recurring.RecurringRuleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class RecurringUiState(
    val rules: List<RecurringRuleEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val loaded: Boolean = false,
)

@HiltViewModel
class RecurringViewModel @Inject constructor(private val db: AppDatabase) : ViewModel() {
    private val rules = db.recurringDao()
    private val categories = db.categoryDao()

    val uiState = combine(rules.observeRules(), categories.observeAll()) { rules, categories ->
        RecurringUiState(rules, categories, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringUiState())

    suspend fun save(rule: RecurringRuleEntity) {
        db.withTransaction {
            require(categories.findById(rule.categoryId)?.type == rule.type)
            if (rule.id == 0L) {
                rules.insert(rule.copy(lastGeneratedMonth = YearMonth.now().minusMonths(1).toString()))
            } else {
                val current = checkNotNull(rules.findById(rule.id))
                rules.update(rule.copy(lastGeneratedMonth = current.lastGeneratedMonth))
            }
        }
    }

    suspend fun delete(rule: RecurringRuleEntity) = rules.delete(rule)
}
