package com.lichsu.budget_tracker.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.layout.SectionTitle
import kotlin.math.roundToInt

private val ThumbSize = 16.dp
private val HistogramHeight = 110.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceRangeFilter(
    title: String,
    histogram: List<Int>,                         // số lượng item trong từng khoảng giá
    valueRange: ClosedFloatingPointRange<Float>,  // giới hạn tổng
    value: ClosedFloatingPointRange<Float>,       // khoảng đang chọn
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = stringResource(R.string.currency_symbol),
) {
    val scheme = MaterialTheme.colorScheme
    val span = valueRange.endInclusive - valueRange.start
    // span = 0 khi chỉ có một mức giá -> tránh chia cho 0 (NaN)
    fun fractionOf(v: Float) = if (span > 0f) (v - valueRange.start) / span else 0f
    val startFraction = fractionOf(value.start)
    val endFraction = fractionOf(value.endInclusive)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(title)

        Column(modifier = Modifier.fillMaxWidth()) {
            // Histogram: lùi vào bằng nửa nút để thẳng hàng với track
            PriceHistogram(
                bins = histogram,
                startFraction = startFraction,
                endFraction = endFraction,
                activeColor = scheme.primary,
                inactiveColor = scheme.surfaceContainerHigh,
                modifier = Modifier
                    .padding(horizontal = ThumbSize / 2)
                    .fillMaxWidth()
                    .height(HistogramHeight),
            )

            RangeSlider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.fillMaxWidth(),
                startThumb = { PriceThumb(ThumbSize) },
                endThumb = { PriceThumb(ThumbSize) },
                track = {
                    PriceTrack(
                        startFraction = startFraction,
                        endFraction = endFraction,
                        activeColor = scheme.primary,
                        inactiveColor = scheme.surfaceContainerHigh,
                    )
                },
            )

            PriceInputs(
                valueRange = valueRange,
                value = value,
                currency = currency,
                onValueChange = onValueChange,
            )
        }
    }
}

@Composable
private fun PriceInputs(
    valueRange: ClosedFloatingPointRange<Float>,
    value: ClosedFloatingPointRange<Float>,
    currency: String,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PriceField(
            label = stringResource(R.string.price_min),
            value = value.start,
            currency = currency,
            isValid = { it in valueRange.start..value.endInclusive },
            onCommit = { onValueChange(it..value.endInclusive) },
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant),
        )
        PriceField(
            label = stringResource(R.string.price_max),
            value = value.endInclusive,
            currency = currency,
            isValid = { it in value.start..valueRange.endInclusive },
            onCommit = { onValueChange(value.start..it) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PriceThumb(size: Dp) {
    Box(
        Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
    )
}

@Composable
private fun PriceTrack(
    startFraction: Float,
    endFraction: Float,
    activeColor: Color,
    inactiveColor: Color,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(ThumbSize)
            .drawBehind {
                val y = size.height / 2
                val stroke = 2.dp.toPx()
                drawLine(inactiveColor, Offset(0f, y), Offset(size.width, y), stroke)
                drawLine(
                    activeColor,
                    Offset(size.width * startFraction, y),
                    Offset(size.width * endFraction, y),
                    stroke,
                )
            }
    )
}

@Composable
private fun PriceField(
    label: String,
    value: Float,
    currency: String,
    isValid: (Float) -> Boolean,
    onCommit: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(value) { mutableStateOf(value.roundToInt().toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            val digits = input.filter(Char::isDigit).take(9)
            text = digits
            digits.toFloatOrNull()?.takeIf(isValid)?.let(onCommit)
        },
        label = { Text(label) },
        prefix = { Text(currency) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.onFocusChanged {
            // Rời ô nhập thì trả về giá trị hợp lệ gần nhất
            if (!it.isFocused) text = value.roundToInt().toString()
        },
    )
}
