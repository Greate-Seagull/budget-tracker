package com.lichsu.budget_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lichsu.budget_tracker.data.local.entity.recurring.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_rules ORDER BY dayOfMonth, id")
    fun observeRules(): Flow<List<RecurringRuleEntity>>

    @Query(
        """
        SELECT * FROM recurring_rules
        WHERE lastGeneratedMonth IS NULL OR lastGeneratedMonth < :throughMonth
        ORDER BY id
        """,
    )
    suspend fun getPendingRules(throughMonth: String): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun findById(id: Long): RecurringRuleEntity?

    @Insert
    suspend fun insert(rule: RecurringRuleEntity)

    @Update
    suspend fun update(rule: RecurringRuleEntity)

    @Delete
    suspend fun delete(rule: RecurringRuleEntity)

    @Query("UPDATE recurring_rules SET lastGeneratedMonth = :month WHERE id = :id")
    suspend fun markGenerated(id: Long, month: String)
}
