package com.lichsu.budget_tracker.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.ui.components.ThousandSeparatorTransformation
import com.lichsu.budget_tracker.ui.components.text.AnimatedErrorText
import com.lichsu.budget_tracker.ui.components.text.animatedErrorColor

@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
) {
    val container = animatedErrorColor(errorText != null, "amountContainer")

    Column(modifier.fillMaxWidth()) {
        AmountBox(value, onValueChange, container)
        AnimatedErrorText(errorText)
    }
}

@Composable
private fun AmountBox(
    value: String,
    onValueChange: (String) -> Unit,
    container: Color,
) {
    val content = MaterialTheme.colorScheme.onPrimary
    val textStyle = MaterialTheme.typography.titleLarge

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.currency_symbol), color = content, style = textStyle)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = textStyle.copy(color = content),
            cursorBrush = SolidColor(content),
            visualTransformation = remember { ThousandSeparatorTransformation() },
            modifier = Modifier.width(IntrinsicSize.Min),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(text = "0", style = textStyle, color = content.copy(alpha = 0.5f))
                    }
                    inner()
                }
            },
        )
    }
}
