package com.lichsu.budget_tracker.ui.categories

/**
 * Bảng màu cho danh mục và cách đọc chuỗi "#RRGGBB" lưu trong cột colorHex.
 * Không import android.* hay Compose để test được bằng JUnit trên JVM.
 */
object CategoryColors {
    /** 8 màu đầu trùng màu seedCategories của TV3, nên danh mục tạo sẵn luôn có ô màu được chọn. */
    val palette = listOf(
        "#008B8B", "#5076C8", "#A95691", "#CA7139", "#7662B6",
        "#448364", "#C85863", "#67718A", "#B08A2E", "#3B8EA5",
    )

    /** Màu xám của "Khác", dùng khi colorHex hỏng (ví dụ tệp sao lưu bị sửa tay). */
    const val FALLBACK = "#67718A"

    /** "#RRGGBB" → số ARGB 0xFFRRGGBB; sai định dạng trả null. */
    fun parse(hex: String): Long? {
        val digits = hex.trim().removePrefix("#")
        // Tự kiểm từng ký tự: toLongOrNull(16) nhận cả dấu "+"/"-" ở đầu.
        if (digits.length != 6 || !digits.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
        return 0xFF000000L or digits.toLong(16)
    }

    /** Luôn trả về một màu vẽ được. */
    fun argbOrFallback(hex: String): Long = parse(hex) ?: checkNotNull(parse(FALLBACK))
}
