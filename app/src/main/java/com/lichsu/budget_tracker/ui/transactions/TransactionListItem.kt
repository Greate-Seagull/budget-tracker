package com.lichsu.budget_tracker.ui.transactions

import java.time.LocalDate
import java.time.YearMonth

/** Giao dịch của một ngày, hiển thị trong một card. */
data class TransactionDay(
    val date: LocalDate,
    val total: Long,
    val items: List<TransactionUi>,
)

/** Một phần tử của danh sách: card của một ngày, hoặc nhãn tháng. */
sealed interface TransactionListItem {
    val month: YearMonth
    val key: String

    data class DayItem(val day: TransactionDay) : TransactionListItem {
        override val month: YearMonth = YearMonth.from(day.date)
        override val key: String = "d_${day.date.toEpochDay()}"
    }

    data class MonthItem(override val month: YearMonth, val total: Long) : TransactionListItem {
        override val key: String = "m_$month"
    }
}

/**
 * Mới nhất ở chỉ số 0 (nằm ở đáy do reverseLayout).
 * Nhãn tháng đứng sau ngày cuối của tháng nên hiện ngay trên ngày đầu tiên.
 */
fun List<TransactionUi>.toListItems(): List<TransactionListItem> =
    groupBy { YearMonth.from(it.date) }
        .toSortedMap(compareByDescending { it })
        .flatMap { (month, monthTxs) ->
            monthTxs.toDayItems() +
                    TransactionListItem.MonthItem(month, monthTxs.sumOf { it.signedAmount })
        }

private fun List<TransactionUi>.toDayItems(): List<TransactionListItem> =
    groupBy { it.date }
        .toSortedMap(compareByDescending { it })
        .map { (date, list) ->
            TransactionListItem.DayItem(
                TransactionDay(date, list.sumOf { it.signedAmount }, list)
            )
        }

fun List<TransactionListItem>.indexOfMonthLabel(month: YearMonth): Int =
    indexOfFirst { it is TransactionListItem.MonthItem && it.month == month }
