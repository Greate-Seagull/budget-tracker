package com.lichsu.budget_tracker.ui.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bảng iconKey → biểu tượng. Cột iconKey trong DB chỉ lưu chữ (ví dụ "food"), không lưu ảnh,
 * nên đổi bộ biểu tượng sau này không phải nâng version DB. Chỉ màn của TV1 dùng (nhóm chốt 2026-11-23).
 */
object CategoryIcons {
    const val FALLBACK_KEY = "other"

    /** Thứ tự hiện trong lưới chọn. 11 key đầu là của 12 danh mục tạo sẵn (SEEDED_CATEGORY_ICONS). */
    val all: List<Pair<String, ImageVector>> = listOf(
        "food" to Icons.Filled.Restaurant,
        "transport" to Icons.Filled.DirectionsCar,
        "shopping" to Icons.Filled.ShoppingBag,
        "home" to Icons.Filled.Home,
        "entertainment" to Icons.Filled.SportsEsports,
        "education" to Icons.Filled.School,
        "health" to Icons.Filled.LocalHospital,
        "salary" to Icons.Filled.Payments,
        "bonus" to Icons.Filled.EmojiEvents,
        "allowance" to Icons.Filled.VolunteerActivism,
        FALLBACK_KEY to Icons.Filled.Category,
        "coffee" to Icons.Filled.LocalCafe,
        "bills" to Icons.Filled.Receipt,
        "electricity" to Icons.Filled.Bolt,
        "phone" to Icons.Filled.PhoneAndroid,
        "travel" to Icons.Filled.Flight,
        "sport" to Icons.Filled.FitnessCenter,
        "beauty" to Icons.Filled.Spa,
        "pets" to Icons.Filled.Pets,
        "child" to Icons.Filled.ChildCare,
        "gift" to Icons.Filled.CardGiftcard,
        "savings" to Icons.Filled.Savings,
        "bank" to Icons.Filled.AccountBalance,
        "work" to Icons.Filled.Work,
    )

    private val byKey = all.toMap()

    /** Key lạ (dữ liệu cũ hoặc tệp sao lưu sửa tay) vẫn có biểu tượng, không crash. */
    fun of(key: String): ImageVector = byKey[key] ?: byKey.getValue(FALLBACK_KEY)

    fun contains(key: String): Boolean = key in byKey
}
