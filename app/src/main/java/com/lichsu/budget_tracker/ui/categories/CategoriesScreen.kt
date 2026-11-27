package com.lichsu.budget_tracker.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.DEFAULT_CATEGORY_NAME
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.ui.components.buttons.PrimaryButton
import com.lichsu.budget_tracker.ui.components.inputs.SegmentedSwitch
import com.lichsu.budget_tracker.ui.settings.SubScreenScaffold
import kotlinx.coroutines.launch

/** Giá trị của editingId khi đang thêm mới. Id thật do Room tạo luôn lớn hơn 0. */
private const val NEW_CATEGORY = 0L

/** Màn 7 (SPEC §4), F03: danh sách theo loại, thêm, sửa, xóa danh mục. */
@Composable
fun CategoriesScreen(onBack: () -> Unit, viewModel: CategoriesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // rememberSaveable lưu vào Bundle: xoay màn hình khi hộp thoại đang mở thì hộp thoại vẫn mở (SPEC §7).
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addedMessage = stringResource(R.string.tv1_category_added)
    val updatedMessage = stringResource(R.string.tv1_category_updated)
    val deletedMessage = stringResource(R.string.tv1_category_deleted)
    fun notify(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    SubScreenScaffold(
        title = stringResource(R.string.tv1_categories),
        onBack = onBack,
        snackbarHostState = snackbar,
        floatingActionButton = {
            if (state.loaded) {
                FloatingActionButton(
                    onClick = { editingId = NEW_CATEGORY },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.tv1_category_add))
                }
            }
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            SegmentedSwitch(
                TransactionType.entries,
                state.type,
                viewModel::selectType,
                labelRes = { it.labelRes },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            )
            when {
                !state.loaded -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.categories.isEmpty() -> EmptyCategories(onAdd = { editingId = NEW_CATEGORY })
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    // Chừa khoảng dưới cùng để nút "+" không che dòng cuối.
                    contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // key = id: thêm hoặc xóa một dòng thì Compose không vẽ lại cả danh sách.
                    items(state.categories, key = { it.id }) { category ->
                        CategoryRow(
                            category = category,
                            onClick = { editingId = category.id },
                            onDelete = { deletingId = category.id },
                        )
                    }
                }
            }
        }
    }

    val editId = editingId
    val editing = state.categories.find { it.id == editId }
    if (state.loaded && (editId == NEW_CATEGORY || editing != null)) {
        // key: mở hộp thoại cho dòng khác thì tạo state mới, không giữ chữ của lần mở trước.
        key(editId) {
            CategoryDialog(
                existing = editing,
                type = state.type,
                onDismiss = { editingId = null },
                onSave = { name, iconKey, colorHex ->
                    if (editing == null) viewModel.add(name, iconKey, colorHex)
                    else viewModel.update(editing.id, name, iconKey, colorHex)
                },
                onSaved = {
                    editingId = null
                    notify(if (editing == null) addedMessage else updatedMessage)
                },
            )
        }
    }

    val deleting = state.categories.find { it.id == deletingId }
    if (deleting != null) {
        key(deleting.id) {
            DeleteCategoryDialog(
                category = deleting,
                defaultName = state.defaultName ?: DEFAULT_CATEGORY_NAME,
                countTransactions = viewModel::countTransactions,
                onDelete = { viewModel.delete(deleting.id) },
                onDismiss = { deletingId = null },
                onDeleted = {
                    deletingId = null
                    notify(deletedMessage)
                },
            )
        }
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CategoryBadge(category.iconKey, category.colorHex)
            Column(Modifier.weight(1f)) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (category.isDefault) {
                    Text(stringResource(R.string.tv1_category_default), style = MaterialTheme.typography.bodySmall)
                }
            }
            // "Khác" không có nút xóa (SPEC F03); repository cũng tự chặn nếu bị gọi.
            if (!category.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.tv1_category_delete_a11y, category.name),
                    )
                }
            }
        }
    }
}

/** Hình tròn mang màu của danh mục, biểu tượng trắng ở giữa. */
@Composable
private fun CategoryBadge(iconKey: String, colorHex: String, size: Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(CategoryColors.argbOrFallback(colorHex))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            CategoryIcons.of(iconKey),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}

@Composable
private fun EmptyCategories(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(48.dp))
        Text(stringResource(R.string.tv1_categories_empty), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.tv1_categories_empty_detail),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(label = stringResource(R.string.tv1_category_add), onClick = onAdd, modifier = Modifier)
    }
}
