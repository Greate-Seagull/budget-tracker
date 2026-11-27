package com.lichsu.budget_tracker.ui.transactionform.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.categories.CategoryUi
import com.lichsu.budget_tracker.ui.components.buttons.NormalIcon
import com.lichsu.budget_tracker.ui.components.dialogs.AppBottomSheet
import com.lichsu.budget_tracker.ui.components.inputs.SearchBar
import com.lichsu.budget_tracker.ui.components.text.NormalText

/** Chỉ hiện ô tìm kiếm khi có nhiều hơn số này danh mục. */
private const val SEARCH_THRESHOLD = 8

@Composable
fun CategoryPickerSheet(
    categories: List<CategoryUi>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    trigger: @Composable (open: () -> Unit) -> Unit,
) {
    AppBottomSheet(title = stringResource(R.string.category_picker_title), trigger = trigger) { close, expand ->
        CategoryPickerContent(
            categories = categories,
            selectedId = selectedId,
            onSelected = { close { onSelected(it.id) } },
            onSearchFocused = expand,
        )
    }
}

@Composable
fun CategoryPickerContent(
    categories: List<CategoryUi>,
    selectedId: Long?,
    modifier: Modifier = Modifier,
    onSelected: (CategoryUi) -> Unit,
    onSearchFocused: () -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(categories, query) { categories.filterByName(query) }

    if (categories.size > SEARCH_THRESHOLD) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onFocused = onSearchFocused,
        )
    }

    val maxHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    LazyColumn(modifier = modifier.heightIn(max = maxHeight)) {
        items(filtered, key = { it.id }) { category ->
            CategoryRow(
                category = category,
                selected = category.id == selectedId,
                onClick = { onSelected(category) },
            )
        }
        if (filtered.isEmpty()) {
            item { NormalText(stringResource(R.string.category_not_found)) }
        }
    }
}

private fun List<CategoryUi>.filterByName(query: String): List<CategoryUi> =
    if (query.isBlank()) this
    else filter { it.name.contains(query.trim(), ignoreCase = true) }

@Composable
private fun CategoryRow(category: CategoryUi, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        NormalText(category.name, Modifier.weight(1f))
        if (selected) NormalIcon(Icons.Default.Check)
    }
}
