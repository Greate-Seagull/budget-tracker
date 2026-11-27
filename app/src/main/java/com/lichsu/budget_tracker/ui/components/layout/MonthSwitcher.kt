package com.lichsu.budget_tracker.ui.components.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.components.text.NormalText
import java.time.YearMonth
import java.time.format.TextStyle

private sealed interface PickerItem {
    data class Year(val year: Int) : PickerItem
    data class Month(val value: YearMonth) : PickerItem
}

/** ... Dec, [2026], Jan, Feb ... (nhãn năm chèn trước tháng 1). */
private fun buildPickerItems(yearRange: IntRange): List<PickerItem> = buildList {
    for (y in yearRange) {
        for (m in 1..12) {
            if (m == 1) add(PickerItem.Year(y))
            add(PickerItem.Month(YearMonth.of(y, m)))
        }
    }
}

@Composable
fun MonthSwitcher(
    selected: YearMonth,
    onSelected: (YearMonth) -> Unit = {},
    modifier: Modifier = Modifier,
    yearRange: IntRange = (selected.year - 10)..(selected.year + 10),
) {
    val pickerItems = remember(yearRange) { buildPickerItems(yearRange) }
    val listState = rememberLazyListState()

    // Tự cuộn tới tháng đang chọn (chừa 2 item bên trái cho đẹp)
    LaunchedEffect(selected) {
        val index = pickerItems.indexOfFirst { it is PickerItem.Month && it.value == selected }
        if (index >= 0) listState.animateScrollToItem((index - 2).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(pickerItems) { item ->
            when (item) {
                is PickerItem.Year -> YearLabel(item.year)
                is PickerItem.Month -> MonthChip(
                    month = item.value,
                    selected = item.value == selected,
                    onClick = { onSelected(item.value) },
                )
            }
        }
    }
}

@Composable
private fun YearLabel(year: Int) {
    NormalText(
        label = year.toString(),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 14.dp),
    )
}

@Composable
private fun MonthChip(month: YearMonth, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) scheme.primary else scheme.secondary,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        NormalText(
            label = month.month.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale),
            color = if (selected) scheme.onPrimary else scheme.onSecondary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
        )
    }
}
