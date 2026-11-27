package com.lichsu.budget_tracker.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** "1250000" -> "1.250.000" chỉ ở phần hiển thị; giá trị thật vẫn là chuỗi chữ số. */
class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = original.reversed().chunked(3).joinToString(".").reversed()

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val total = if (original.isNotEmpty()) (original.length - 1) / 3 else 0
                val remaining = original.length - offset
                val separatorsAfter = if (remaining > 0) (remaining - 1) / 3 else 0
                return offset + total - separatorsAfter
            }

            override fun transformedToOriginal(offset: Int): Int =
                offset - formatted.take(offset).count { it == '.' }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
