package com.lichsu.budget_tracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.lichsu.budget_tracker.ui.home.Tv3HomeRoute
import com.lichsu.budget_tracker.ui.recurring.RecurringScreen
import com.lichsu.budget_tracker.ui.stats.StatsScreen
import com.lichsu.budget_tracker.ui.transactionform.TransactionFormRoute
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
    Destination(Recurring, R.string.tv3_recurring, Icons.Default.Repeat, R.string.tv3_recurring_tab),
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
        topBar = {
            if (active != null) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(active.title), style = MaterialTheme.typography.bodyLarge)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background,
                        titleContentColor = colors.onBackground,
                    ),
                )
            }
        },
        bottomBar = {
            if (active != null) {
                NavigationBar(containerColor = colors.secondary) {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = destination == active,
                            onClick = { if (destination != active) select(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.onPrimary,
                                selectedTextColor = colors.onSecondary,
                                indicatorColor = colors.primary,
                                unselectedIconColor = colors.onSecondary,
                                unselectedTextColor = colors.onSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController,
            startDestination = Home,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable<Home> {
                Tv3HomeRoute(
                    onOpenBudget = { select(Budget) },
                    onAddTransaction = { navController.navigate(TransactionForm()) },
                    onEditTransaction = { navController.navigate(TransactionForm(it)) },
                )
            }
            composable<Stats> { StatsScreen() }
            composable<Budget> { BudgetScreen() }
            composable<Recurring> { RecurringScreen() }
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
