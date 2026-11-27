package com.lichsu.budget_tracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.budget.BudgetScreen
import com.lichsu.budget_tracker.ui.budget.toWarningMessage
import com.lichsu.budget_tracker.ui.categories.CategoriesScreen
import com.lichsu.budget_tracker.ui.home.Tv3HomeRoute
import com.lichsu.budget_tracker.ui.recurring.RecurringScreen
import com.lichsu.budget_tracker.ui.settings.SettingsScreen
import com.lichsu.budget_tracker.ui.settings.SubScreenScaffold
import com.lichsu.budget_tracker.ui.stats.StatsScreen
import com.lichsu.budget_tracker.ui.transactionform.TransactionFormRoute
import com.lichsu.budget_tracker.ui.transactions.TransactionsRoute
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Settings as SettingsIcon

private const val PARALLAX = 4 // màn cũ chỉ dịch 1/4 chiều rộng
import kotlinx.coroutines.launch

private data class Destination(
    val route: Any,
    @param:StringRes val title: Int,
    val icon: ImageVector,
    @param:StringRes val label: Int = title,
)

private val destinations = listOf(
    Destination(Home, R.string.tv3_home, Icons.Default.Home),
    Destination(Stats, R.string.tv3_stats, Icons.Default.BarChart),
    Destination(Budget, R.string.tv3_budget, Icons.Default.AccountBalanceWallet),
    // Lặp lại không còn là tab (nhóm chốt 2026-11-23): mở từ Cài đặt, route Recurring giữ nguyên.
    Destination(Settings, R.string.tv1_settings, Icons.Default.SettingsIcon),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val entry by navController.currentBackStackEntryAsState()
    val active = destinations.firstOrNull { entry?.destination?.hasRoute(it.route::class) == true }
    val colors = MaterialTheme.colorScheme
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun select(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = colors.background,
        contentColor = colors.onBackground,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { /* giữ nguyên như bản kia */ },
        bottomBar = { /* giữ nguyên như bản kia */ },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Home, // hoặc Transactions, xem ghi chú bên dưới
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),

            // ---- Hiệu ứng của bạn ----
            enterTransition = {
                slideInHorizontally(tween(560, easing = EaseOutSine)) { it }
            },
            exitTransition = {
                slideOutHorizontally(tween(560, easing = EaseOutSine)) { -it / PARALLAX }
            },
            popEnterTransition = {
                slideInHorizontally(tween(500, easing = EaseIn)) { -it / PARALLAX }
            },
            popExitTransition = {
                slideOutHorizontally(tween(500, easing = EaseIn)) { it }
            },
        ) {
            composable<Home> {
                Tv3HomeRoute(
                    onOpenBudget = { select(Budget) },
                    onAddTransaction = { navController.navigate(TransactionForm()) },
                    onEditTransaction = { navController.navigate(TransactionForm(it)) },
                )
            }

            // ---- Màn của bạn ----
            composable<Transactions> {
                TransactionsRoute(
                    onAddTransaction = { navController.navigate(TransactionForm()) },
                    onModifyTransaction = { id -> navController.navigate(TransactionForm(id)) },
                )
            }

            composable<Stats> { StatsScreen() }
            composable<Budget> { BudgetScreen() }
            composable<Recurring> {
                SubScreenScaffold(stringResource(R.string.tv3_recurring), onBack = { navController.popBackStack() }) {
                    RecurringScreen()
                }
            }
            composable<Settings> {
                SettingsScreen(
                    onOpenCategories = { navController.navigate(Categories) },
                    onOpenRecurring = { navController.navigate(Recurring) },
                )
            }
            composable<Categories> { CategoriesScreen(onBack = { navController.popBackStack() }) }
            composable<TransactionForm> {
                TransactionFormRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { warnings ->
                        navController.popBackStack()
                        if (warnings.isNotEmpty()) {
                            val message = warnings.joinToString("\n") { it.toWarningMessage(context) }
                            scope.launch { snackbar.showSnackbar(message) }
                        }
                    },
                )
            }
        }
    }
}