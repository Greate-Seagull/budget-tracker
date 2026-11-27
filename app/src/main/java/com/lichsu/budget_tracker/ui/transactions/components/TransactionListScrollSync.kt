package com.lichsu.budget_tracker.ui.transactions.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import com.lichsu.budget_tracker.ui.transactions.TransactionListItem
import com.lichsu.budget_tracker.ui.transactions.indexOfMonthLabel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import java.time.YearMonth
import kotlin.math.abs

/** Tháng của card ngày có tâm gần [centerPx] nhất (toạ độ của list); null nếu không có card ngày nào. */
internal fun LazyListState.centerMonth(
    items: List<TransactionListItem>,
    centerPx: Int,
): YearMonth? {
    var best: YearMonth? = null
    var bestDistance = Int.MAX_VALUE
    for (visible in layoutInfo.visibleItemsInfo) {
        val day = items.getOrNull(visible.index) as? TransactionListItem.DayItem ?: continue
        val distance = abs(visible.offset + visible.size / 2 - centerPx)
        if (distance < bestDistance) {
            bestDistance = distance
            best = day.month
        }
    }
    return best
}

/**
 * Đồng bộ hai chiều giữa danh sách và tháng đang chọn:
 *  - Cuộn tay  -> báo tháng của card gần [centerPx] nhất qua [onMonthChanged].
 *  - Chọn tháng ở picker -> cuộn để nhãn tháng nằm ở đầu màn hình.
 */
@Composable
internal fun SyncMonthWithScroll(
    listState: LazyListState,
    items: List<TransactionListItem>,
    selectedMonth: YearMonth,
    centerPx: Int,
    onMonthChanged: (YearMonth) -> Unit,
) {
    val currentOnMonthChanged by rememberUpdatedState(onMonthChanged)
    val currentItems by rememberUpdatedState(items)
    val currentSelectedMonth by rememberUpdatedState(selectedMonth)
    val currentCenter by rememberUpdatedState(centerPx)

    // true khi chính code đang cuộn (do chọn tháng ở picker)
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    val labelSizePx = with(LocalDensity.current) { MonthLabelDefaults.Height.roundToPx() }

    // Cuộn tay: tháng của card gần giữa màn hình nhất được chọn
    LaunchedEffect(listState) {
        snapshotFlow {
            if (listState.isScrollInProgress && !isProgrammaticScroll) {
                listState.centerMonth(currentItems, currentCenter)
            } else null
        }
            .filterNotNull()
            .filter { it != currentSelectedMonth }
            .collect { currentOnMonthChanged(it) }
    }

    // Chọn tháng ở picker -> cuộn để nhãn tháng nằm ở đầu màn hình
    LaunchedEffect(selectedMonth, items) {
        if (listState.isScrollInProgress) return@LaunchedEffect
        if (listState.centerMonth(items, currentCenter) == selectedMonth) return@LaunchedEffect
        val index = items.indexOfMonthLabel(selectedMonth)
        if (index < 0) return@LaunchedEffect

        val info = listState.layoutInfo
        val free = info.viewportSize.height -
                info.beforeContentPadding - info.afterContentPadding - labelSizePx

        isProgrammaticScroll = true
        try {
            listState.animateScrollToItem(index, scrollOffset = -free)
        } finally {
            isProgrammaticScroll = false   // chạy cả khi animation bị người dùng chạm vào hủy
        }
    }
}
