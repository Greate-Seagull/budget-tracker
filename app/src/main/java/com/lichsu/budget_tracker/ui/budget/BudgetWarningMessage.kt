package com.lichsu.budget_tracker.ui.budget

import android.content.Context
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.dao.BudgetUsage
import com.lichsu.budget_tracker.domain.BudgetChecker
import com.lichsu.budget_tracker.domain.BudgetStatus
import com.lichsu.budget_tracker.util.MoneyFormat

internal fun BudgetUsage.toWarningMessage(context: Context): String {
    val label = categoryName ?: context.getString(R.string.tv3_total_limit)
    val resource = if (BudgetChecker.check(spent, limitAmount) == BudgetStatus.EXCEEDED)
        R.string.tv3_alert_exceeded else R.string.tv3_alert_warning
    return context.getString(resource, label, MoneyFormat.percent(spent.toDouble() / limitAmount))
}
