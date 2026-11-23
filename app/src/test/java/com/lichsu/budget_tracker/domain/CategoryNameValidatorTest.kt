package com.lichsu.budget_tracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryNameValidatorTest {
    private val expenseNames = listOf("Ăn uống", "Di chuyển", "Đi chợ", "Khác")

    @Test fun blankOrWhitespaceNameIsEmpty() {
        for (raw in listOf("", "   ", "\t\n ")) {
            assertEquals(invalid(CategoryNameError.EMPTY), CategoryNameValidator.validate(raw, expenseNames))
        }
    }

    @Test fun trimsAndCollapsesSpacesBeforeSaving() {
        assertEquals(
            CategoryNameResult.Valid("Cà phê sáng"),
            CategoryNameValidator.validate("  Cà   phê \t sáng ", expenseNames),
        )
    }

    @Test fun duplicateIgnoresCaseIncludingVietnameseLetters() {
        for (raw in listOf("ăn uống", "ĂN UỐNG", "  Ăn  Uống ", "đi chợ", "ĐI CHỢ", "khác")) {
            assertEquals(raw, invalid(CategoryNameError.DUPLICATE), CategoryNameValidator.validate(raw, expenseNames))
        }
    }

    @Test fun duplicateMatchesDecomposedAccents() {
        // "Ăn uống" gõ kiểu tổ hợp: chữ gốc + dấu rời (NFD), nhìn giống hệt nhưng khác byte.
        val decomposed = java.text.Normalizer.normalize("Ăn uống", java.text.Normalizer.Form.NFD)
        assertEquals(invalid(CategoryNameError.DUPLICATE), CategoryNameValidator.validate(decomposed, expenseNames))
    }

    @Test fun similarButDifferentNamesAreAllowed() {
        assertEquals(CategoryNameResult.Valid("Ăn uống ngoài"), CategoryNameValidator.validate("Ăn uống ngoài", expenseNames))
        assertEquals(CategoryNameResult.Valid("Di chuyen"), CategoryNameValidator.validate("Di chuyen", expenseNames))
    }

    @Test fun emptyListAcceptsAnyNonBlankName() {
        assertEquals(CategoryNameResult.Valid("Lương"), CategoryNameValidator.validate("Lương", emptyList()))
    }

    private fun invalid(error: CategoryNameError) = CategoryNameResult.Invalid(error)
}
