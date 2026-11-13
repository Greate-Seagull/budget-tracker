package com.lichsu.budget_tracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lichsu.budget_tracker.ui.transactionform.TransactionFormRoute

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = TransactionForm()) {
        composable<TransactionForm> {
            TransactionFormRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}