package com.example

import com.example.util.DateUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDateUtils_isOverdue() {
        val now = System.currentTimeMillis()
        val pastDate = now - (2 * 24 * 60 * 60 * 1000) // 2 days ago
        val futureDate = now + (2 * 24 * 60 * 60 * 1000) // in 2 days

        assertTrue(DateUtils.isOverdue(pastDate))
        assertFalse(DateUtils.isOverdue(futureDate))
    }

    @Test
    fun testDateUtils_isDueSoon() {
        val now = System.currentTimeMillis()
        val tomorrow = now + (1 * 24 * 60 * 60 * 1000)
        val tenDaysLater = now + (10 * 24 * 60 * 60 * 1000)

        assertTrue(DateUtils.isDueSoon(tomorrow, thresholdDays = 2))
        assertFalse(DateUtils.isDueSoon(tenDaysLater, thresholdDays = 2))
    }

    @Test
    fun testDateUtils_formatDate() {
        val timestamp = 1700000000000L
        val formatted = DateUtils.formatDate(timestamp)
        assertTrue(formatted.isNotBlank())
    }
}
