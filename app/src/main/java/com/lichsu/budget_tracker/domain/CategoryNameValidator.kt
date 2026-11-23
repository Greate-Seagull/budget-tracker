package com.lichsu.budget_tracker.domain

import java.text.Normalizer

enum class CategoryNameError {
    EMPTY,
    DUPLICATE,
}

sealed interface CategoryNameResult {
    data class Invalid(val error: CategoryNameError) : CategoryNameResult
    data class Valid(val value: String) : CategoryNameResult
}

/** Kiểm tên danh mục (F03). Hàm thuần, không dùng Android, test bằng JUnit trên JVM. */
object CategoryNameValidator {

    /**
     * @param raw tên người dùng gõ
     * @param existingNames tên các danh mục khác cùng loại (đã bỏ chính danh mục đang sửa)
     */
    fun validate(raw: String, existingNames: Collection<String>): CategoryNameResult {
        val name = normalize(raw)
        if (name.isEmpty()) return CategoryNameResult.Invalid(CategoryNameError.EMPTY)

        val key = compareKey(name)
        if (existingNames.any { compareKey(it) == key }) {
            return CategoryNameResult.Invalid(CategoryNameError.DUPLICATE)
        }
        return CategoryNameResult.Valid(name)
    }

    /**
     * Đưa chữ có dấu về một dạng (NFC: bàn phím khác nhau có thể gõ "á" thành 1 hoặc 2 ký tự),
     * bỏ khoảng trắng hai đầu và gộp nhiều khoảng trắng liền nhau thành một.
     */
    fun normalize(raw: String): String =
        Normalizer.normalize(raw, Normalizer.Form.NFC).trim().replace(WHITESPACE, " ")

    // So trùng bằng lowercase() của Kotlin, không dùng LOWER() của SQLite vì nó chỉ hạ chữ ASCII ("Đ" vẫn là "Đ").
    private fun compareKey(name: String): String = normalize(name).lowercase()

    private val WHITESPACE = Regex("\\s+")
}
