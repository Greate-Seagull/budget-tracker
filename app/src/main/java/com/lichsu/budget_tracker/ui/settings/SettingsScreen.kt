package com.lichsu.budget_tracker.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R

/** Một dòng trong Cài đặt. onClick = null nghĩa là chức năng chưa làm: dòng mờ, bấm không được. */
private data class SettingsEntry(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val detail: Int,
    val onClick: (() -> Unit)?,
)

/** Màn 6 (SPEC §4): danh sách lối vào các chức năng quản lý và dữ liệu. */
@Composable
fun SettingsScreen(onOpenCategories: () -> Unit, onOpenRecurring: () -> Unit) {
    // Xuất CSV (F07), Sao lưu/Khôi phục (F08), Giao diện (F10), dữ liệu mẫu, xóa dữ liệu: các buổi sau.
    val groups = listOf(
        R.string.tv1_settings_group_manage to listOf(
            SettingsEntry(Icons.Default.Category, R.string.tv1_categories, R.string.tv1_settings_categories_detail, onOpenCategories),
            SettingsEntry(Icons.Default.Repeat, R.string.tv1_settings_recurring, R.string.tv1_settings_recurring_detail, onOpenRecurring),
        ),
        R.string.tv1_settings_group_data to listOf(
            SettingsEntry(Icons.Default.TableChart, R.string.tv1_settings_export_csv, R.string.tv1_settings_export_csv_detail, null),
            SettingsEntry(Icons.Default.Backup, R.string.tv1_settings_backup, R.string.tv1_settings_backup_detail, null),
            SettingsEntry(Icons.Default.Restore, R.string.tv1_settings_restore, R.string.tv1_settings_restore_detail, null),
            SettingsEntry(Icons.Default.AutoAwesome, R.string.tv1_settings_sample_data, R.string.tv1_settings_sample_data_detail, null),
            SettingsEntry(Icons.Default.DeleteForever, R.string.tv1_settings_clear_data, R.string.tv1_settings_clear_data_detail, null),
        ),
        R.string.tv1_settings_group_display to listOf(
            SettingsEntry(Icons.Default.Palette, R.string.tv1_settings_theme, R.string.tv1_settings_theme_detail, null),
        ),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        groups.forEach { (header, entries) ->
            item(key = header) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(header), style = MaterialTheme.typography.titleMedium)
                    SettingsGroup(entries)
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(entries: List<SettingsEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.12f))
            SettingsRow(entry)
        }
    }
}

@Composable
private fun SettingsRow(entry: SettingsEntry) {
    val enabled = entry.onClick != null
    val detail = stringResource(entry.detail)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { entry.onClick?.invoke() }
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(entry.icon, contentDescription = null)
        Column(Modifier.weight(1f)) {
            Text(stringResource(entry.title), style = MaterialTheme.typography.bodyLarge)
            Text(
                if (enabled) detail else stringResource(R.string.tv1_coming_soon, detail),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (enabled) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

// Mức mờ Material dùng cho thành phần bị tắt.
private const val DISABLED_ALPHA = 0.38f
