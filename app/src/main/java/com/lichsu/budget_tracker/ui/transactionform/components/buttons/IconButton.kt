package com.lichsu.budget_tracker.ui.transactionform.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lichsu.budget_tracker.ui.transactionform.components.icons.NormalIcon

@Composable
fun IconButton(
    icon: Int,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        NormalIcon(icon)
    }
}