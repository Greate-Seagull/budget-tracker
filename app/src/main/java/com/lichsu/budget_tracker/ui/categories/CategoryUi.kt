package com.lichsu.budget_tracker.ui.categories

import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity

data class CategoryUi(val id: Long, val name: String)

fun CategoryEntity.toUi() = CategoryUi(id = id, name = name)
