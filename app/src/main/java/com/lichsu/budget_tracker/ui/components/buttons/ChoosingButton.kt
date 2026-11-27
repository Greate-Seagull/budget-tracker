package com.lichsu.budget_tracker.ui.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.components.text.NormalText

/** Dòng "nhãn ... giá trị >" để mở một bộ chọn. */
@Composable
fun ChoosingButton(
    @DrawableRes icon: Int,
    label: String,
    value: String,
    errorText: String? = null,
    onClick: () -> Unit = {},
) {
    NormalRow(
        errorText = errorText,
        onClick = onClick,
        leading = { tint -> CircleIcon(icon, background = tint) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NormalText(value)
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        NormalText(label)
    }
}
