package com.lichsu.budget_tracker.di

import androidx.sqlite.db.SupportSQLiteDatabase

object DefaultCategories {

    private val items = listOf(
        "Shopping",
        "Food",
        "Entertainment",
        "Education",
        "Beauty",
        "Sports",
        "Social",
        "Travel",
        "Clothing",
        "Health",
        "Devices",
        "Pets",
        "Repairs",
        "Gifts",
        "Donations",
        "Lottery",
        "Kids",
        "Investment",
        "Salary",
        "Other"
    )

    fun seed(db: SupportSQLiteDatabase) {
        items.forEach { name ->
            db.execSQL(
                "INSERT INTO categories (name) VALUES (?)",
                arrayOf(name)
            )
        }
    }
}