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
