package com.lichsu.budget_tracker.ui.categories

import com.lichsu.budget_tracker.data.local.SEEDED_CATEGORY_ICONS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryStyleTest {

    @Test fun parsesHexColorInAnyCaseAndWithSpaces() {
        assertEquals(0xFF008B8BL, CategoryColors.parse("#008B8B"))
        assertEquals(0xFFA95691L, CategoryColors.parse("#a95691"))
        assertEquals(0xFF67718AL, CategoryColors.parse("  #67718A "))
    }

    @Test fun rejectsMalformedHexColor() {
        listOf("", "#", "#12345", "#1234567", "#GGGGGG", "#+12345", "#-12345", "đỏ").forEach {
            assertNull("\"$it\" phải bị từ chối", CategoryColors.parse(it))
        }
    }

    @Test fun brokenColorFallsBackToGrey() {
        assertEquals(CategoryColors.parse(CategoryColors.FALLBACK), CategoryColors.argbOrFallback("#XYZ"))
    }

    @Test fun paletteIsValidAndHasNoDuplicates() {
        CategoryColors.palette.forEach { assertNotNull(it, CategoryColors.parse(it)) }
        assertEquals(CategoryColors.palette.size, CategoryColors.palette.map { it.uppercase() }.toSet().size)
    }

    @Test fun everySeededCategoryHasAnIcon() {
        SEEDED_CATEGORY_ICONS.forEach { (_, name, key) ->
            assertTrue("thiếu biểu tượng cho $name ($key)", CategoryIcons.contains(key))
        }
        assertEquals(CategoryIcons.all.size, CategoryIcons.all.map { it.first }.toSet().size)
    }

    @Test fun unknownIconKeyUsesFallback() {
        assertSame(CategoryIcons.of(CategoryIcons.FALLBACK_KEY), CategoryIcons.of("khong-co"))
    }
}
