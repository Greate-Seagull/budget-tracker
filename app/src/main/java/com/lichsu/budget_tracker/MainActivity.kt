package com.lichsu.budget_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.lichsu.budget_tracker.domain.RecurringProcessor
import com.lichsu.budget_tracker.ui.navigation.AppNavHost
import com.lichsu.budget_tracker.ui.theme.BudgetTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var recurringProcessor: RecurringProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetTrackerTheme {
                AppNavHost()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            try {
                recurringProcessor.process()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Timber.e(exception, "Recurring transaction generation failed")
            }
        }
    }
}
