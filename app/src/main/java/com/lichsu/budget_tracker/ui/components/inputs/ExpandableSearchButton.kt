package com.lichsu.budget_tracker.ui.components.inputs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R

enum class SearchExpandDirection { Start, End }

@Stable
class SearchButtonState(initialExpanded: Boolean = false) {
    var expanded by mutableStateOf(initialExpanded)
        private set

    fun open() { expanded = true }
    fun close() { expanded = false }
    fun toggle() { expanded = !expanded }

    companion object {
        val Saver: Saver<SearchButtonState, Boolean> = Saver(
            save = { it.expanded },
            restore = { SearchButtonState(it) }
        )
    }
}

@Composable
fun rememberSearchButtonState(initialExpanded: Boolean = false): SearchButtonState =
    rememberSaveable(saver = SearchButtonState.Saver) {
        SearchButtonState(initialExpanded)
    }

@Composable
fun ExpandableSearchButton(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: SearchButtonState = rememberSearchButtonState(),
    expandDirection: SearchExpandDirection = SearchExpandDirection.End,
    fieldWidth: Dp = Dp.Unspecified,   // Unspecified = giãn theo không gian còn lại
    placeholder: String = stringResource(R.string.search_placeholder),
    onSearch: (String) -> Unit = {},
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (expandDirection == SearchExpandDirection.End)
            Arrangement.Start else Arrangement.End,
    ) {
        val searchField: @Composable () -> Unit = {
            SearchField(
                visible = state.expanded,
                expandFrom = if (expandDirection == SearchExpandDirection.End)
                    Alignment.Start else Alignment.End,
                fieldModifier = if (fieldWidth == Dp.Unspecified)
                    Modifier.weight(1f) else Modifier.width(fieldWidth),
                query = query,
                onQueryChange = onQueryChange,
                placeholder = placeholder,
                onSearch = onSearch,
                onClose = state::close,
            )
        }

        if (expandDirection == SearchExpandDirection.Start) searchField()

        IconButton(onClick = state::toggle) {
            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_cd))
        }

        if (expandDirection == SearchExpandDirection.End) searchField()
    }
}

@Composable
private fun RowScope.SearchField(
    visible: Boolean,
    expandFrom: Alignment.Horizontal,
    fieldModifier: Modifier,   // thay cho tham số width
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onSearch: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    AnimatedVisibility(
        visible = visible,
        modifier = fieldModifier,   // weight(1f) hoặc width(...) áp dụng ở đây
        enter = expandHorizontally(expandFrom = expandFrom) + fadeIn(),
        exit = shrinkHorizontally(shrinkTowards = expandFrom) + fadeOut(),
    ) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Row(
            modifier = Modifier
                .fillMaxWidth()   // lấp đầy vùng mà AnimatedVisibility được cấp
                .height(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch(query)
                        keyboard?.hide()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            IconButton(
                onClick = {
                    // Có chữ thì xóa chữ, rỗng thì đóng thanh tìm kiếm
                    if (query.isNotEmpty()) onQueryChange("") else onClose()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_clear),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}