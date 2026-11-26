package com.lichsu.budget_tracker.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
data object Stats

@Serializable
data object Budget

@Serializable
data object Recurring

@Serializable
data class TransactionForm(val id: Long? = null)

// TV1: tab Cài đặt (màn 6) và màn Danh mục mở từ Cài đặt (màn 7).
@Serializable
data object Settings

@Serializable
data object Categories
