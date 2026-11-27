package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.components.layout.rememberNumberFormat
import com.lichsu.budget_tracker.ui.transactions.TransactionListItem
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun TransactionList(
    items: List<TransactionListItem>,
    selectedMonth: YearMonth,
    today: LocalDate,
    modifier: Modifier = Modifier,
    onMonthChanged: (YearMonth) -> Unit,
    onRowClicked: (Long) -> Unit,
) {
    val locale = LocalLocale.current.platformLocale
    val numberFormat = rememberNumberFormat()
    val listState = rememberLazyListState()

    // Tâm màn hình, quy về toạ độ của list
    val screenHeightPx = LocalWindowInfo.current.containerSize.height
    var listTopInWindow by remember { mutableIntStateOf(0) }
    val screenCenterInList = screenHeightPx / 2 - listTopInWindow

    SyncMonthWithScroll(
        listState = listState,
        items = items,
        selectedMonth = selectedMonth,
        centerPx = screenCenterInList,
        onMonthChanged = onMonthChanged,
    )

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { listTopInWindow = it.positionInWindow().y.roundToInt() },
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(items, key = { it.key }) { item ->
            when (item) {
                is TransactionListItem.DayItem ->
                    DayCard(item.day, today, locale, numberFormat, onRowClicked)
                is TransactionListItem.MonthItem -> MonthLabel(item.month)
            }
        }
    }
}
