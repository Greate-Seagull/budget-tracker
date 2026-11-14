package com.lichsu.budget_tracker.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.dao.BudgetUsage
import com.lichsu.budget_tracker.domain.BudgetChecker
import com.lichsu.budget_tracker.domain.BudgetStatus
import com.lichsu.budget_tracker.ui.components.FinanceCard
import com.lichsu.budget_tracker.util.MoneyFormat

@Composable
fun BudgetSummaryCard(
    usage: BudgetUsage?,
    modifier: Modifier = Modifier,
    onOpenBudget: (() -> Unit)? = null,
) {
    FinanceCard(modifier) {
        Text(stringResource(R.string.tv3_total_limit), style = MaterialTheme.typography.bodyLarge)
        if (usage == null) {
            Text(stringResource(R.string.tv3_no_total_limit))
        } else {
            BudgetProgress(usage)
        }
        if (onOpenBudget != null) {
            TextButton(onClick = onOpenBudget) { Text(stringResource(R.string.tv3_manage_budget)) }
        }
    }
}

@Composable
internal fun BudgetProgress(usage: BudgetUsage) {
    val status = BudgetChecker.check(usage.spent, usage.limitAmount)
    val ratio = usage.spent.toDouble() / usage.limitAmount
    val color = when (status) {
        BudgetStatus.NORMAL -> MaterialTheme.colorScheme.primary
        BudgetStatus.WARNING -> Color(0xFFCF990B)
        BudgetStatus.EXCEEDED -> MaterialTheme.colorScheme.error
    }
    val difference = usage.limitAmount - usage.spent
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(MoneyFormat.vnd(usage.spent), style = MaterialTheme.typography.titleSmall)
        Text(MoneyFormat.percent(ratio), style = MaterialTheme.typography.titleSmall)
    }
    LinearProgressIndicator(
        progress = { ratio.coerceIn(0.0, 1.0).toFloat() },
        modifier = Modifier.fillMaxWidth(),
        color = color,
        trackColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.14f),
    )
    Text(
        stringResource(
            if (difference >= 0) R.string.tv3_budget_left else R.string.tv3_budget_over,
            MoneyFormat.vnd(kotlin.math.abs(difference)),
            MoneyFormat.vnd(usage.limitAmount),
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
    if (status != BudgetStatus.NORMAL) {
        Text(
            stringResource(
                if (status == BudgetStatus.WARNING) R.string.tv3_budget_warning else R.string.tv3_budget_reached,
            ),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
