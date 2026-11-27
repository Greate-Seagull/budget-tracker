package com.lichsu.budget_tracker.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.domain.model.DatePreset
import com.lichsu.budget_tracker.domain.model.DateRange
import com.lichsu.budget_tracker.ui.components.inputs.ChoiceChip
import com.lichsu.budget_tracker.ui.components.layout.SectionTitle
import com.lichsu.budget_tracker.ui.components.text.NormalText
import kotlinx.coroutines.awaitCancellation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DateBoxFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
private fun DatePreset.label(): String = when (this) {
    DatePreset.TODAY -> stringResource(R.string.date_preset_today)
    DatePreset.YESTERDAY -> stringResource(R.string.date_preset_yesterday)
    DatePreset.LAST_7 -> stringResource(R.string.date_preset_last_7)
    DatePreset.LAST_30 -> stringResource(R.string.date_preset_last_30)
    DatePreset.LAST_90 -> stringResource(R.string.date_preset_last_90)
    DatePreset.THIS_MONTH -> stringResource(R.string.date_preset_this_month)
    DatePreset.LAST_MONTH -> stringResource(R.string.date_preset_last_month)
    DatePreset.THIS_YEAR -> stringResource(R.string.date_preset_this_year)
}

@Composable
fun DateRangeFilter(
    value: DateRange,
    onValueChange: (DateRange) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    today: LocalDate = remember { LocalDate.now() },
) {
    var showPicker by remember { mutableStateOf(false) }
    val activePreset = DatePreset.entries.firstOrNull { it.range(today) == value }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!title.isNullOrEmpty()) SectionTitle(title)

        DatePresetChips(
            active = activePreset,
            onSelect = { onValueChange(it.range(today)) },
        )

        DateRangeFields(
            value = value,
            pickerOpen = showPicker,
            onClick = { showPicker = true },
        )

        if (showPicker) {
            DateRangePickerDialog(
                initial = value,
                onDismiss = { showPicker = false },
                onConfirm = {
                    onValueChange(it)
                    showPicker = false
                },
            )
        }
    }
}

@Composable
private fun DatePresetChips(active: DatePreset?, onSelect: (DatePreset) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DatePreset.entries) { preset ->
            ChoiceChip(
                selected = preset == active,
                onClick = { onSelect(preset) },
                content = { NormalText(preset.label()) },
            )
        }
    }
}

/** Hai ô Từ / Đến chỉ để hiển thị; bấm vào bất kỳ đâu sẽ mở picker. */
@Composable
private fun DateRangeFields(value: DateRange, pickerOpen: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateBox(stringResource(R.string.date_from), value.start, pickerOpen, Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
            DateBox(stringResource(R.string.date_to), value.end, pickerOpen, Modifier.weight(1f))
        }
        Box(
            Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun DateBox(
    label: String,
    date: LocalDate,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }

    // Giả lập focus: phát Focus khi active, Unfocus khi tắt
    LaunchedEffect(active) {
        if (active) {
            val focus = FocusInteraction.Focus()
            interaction.emit(focus)
            try {
                awaitCancellation()
            } finally {
                interaction.tryEmit(FocusInteraction.Unfocus(focus))
            }
        }
    }

    OutlinedTextField(
        value = date.format(DateBoxFormatter),
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        label = { Text(label) },
        shape = MaterialTheme.shapes.medium,
        interactionSource = interaction,
        modifier = modifier,
    )
}
