package com.lichsu.budget_tracker.di

import android.content.Context
import androidx.room.Room
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.DEFAULT_CATEGORIES
import com.lichsu.budget_tracker.data.local.MIGRATION_1_2
import com.lichsu.budget_tracker.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "budget.db")
            .addMigrations(MIGRATION_1_2)
            .addCallback(DEFAULT_CATEGORIES)
            .build()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
}
