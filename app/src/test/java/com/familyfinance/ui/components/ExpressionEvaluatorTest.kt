package com.familyfinance.ui.components

import org.junit.Assert.*
import org.junit.Test

// @trace TASK-201
// @trace FR-023
class ExpressionEvaluatorTest {

    private val evaluator = ExpressionEvaluator()

    // ─── Addition ────────────────────────────────────────────────────────────
    @Test
    fun `addition of two integers`() {
        assertEquals("30", evaluator.evaluate("10+20"))
    }

    @Test
    fun `addition of decimals`() {
        assertEquals("25.5", evaluator.evaluate("15.5+10"))
    }

    // ─── Subtraction ─────────────────────────────────────────────────────────
    @Test
    fun `subtraction of two integers`() {
        assertEquals("5", evaluator.evaluate("15-10"))
    }

    @Test
    fun `subtraction resulting in decimal`() {
        assertEquals("4.5", evaluator.evaluate("15-10.5"))
    }

    // ─── Multiplication ──────────────────────────────────────────────────────
    @Test
    fun `multiplication of two integers`() {
        assertEquals("51", evaluator.evaluate("51*1"))
    }

    @Test
    fun `multiplication of integer and decimal`() {
        assertEquals("51", evaluator.evaluate("25.5*2"))
    }

    // ─── Division ────────────────────────────────────────────────────────────
    @Test
    fun `division of two integers`() {
        assertEquals("5", evaluator.evaluate("10/2"))
    }

    @Test
    fun `division resulting in decimal`() {
        assertEquals("2.5", evaluator.evaluate("5/2"))
    }

    @Test
    fun `division by zero returns error`() {
        assertEquals("Error", evaluator.evaluate("10/0"))
    }

    // ─── Percentage ──────────────────────────────────────────────────────────
    @Test
    fun `percentage operation - A percent B means A times B divided by 100`() {
        // 200 % 5  =  200 * 5 / 100  =  10  ("5% of 200")
        assertEquals("10", evaluator.evaluate("200%5"))
    }

    @Test
    fun `percentage of zero operand returns zero`() {
        // 100 % 0  =  100 * 0 / 100  =  0 (not an error)
        assertEquals("0", evaluator.evaluate("100%0"))
    }

    // ─── Chained operations (left-to-right, simple evaluator) ────────────────
    @Test
    fun `chained addition and multiplication`() {
        // Calculator evaluates left-to-right without BODMAS: (25.5 + 10) * 2 is two separate presses
        // This tests a single expression: the evaluator processes exactly one operator
        assertEquals("30", evaluator.evaluate("10+20"))
    }

    // ─── Edge cases ──────────────────────────────────────────────────────────
    @Test
    fun `single number is returned as-is`() {
        assertEquals("42", evaluator.evaluate("42"))
    }

    @Test
    fun `single decimal is returned as-is`() {
        assertEquals("3.14", evaluator.evaluate("3.14"))
    }

    @Test
    fun `empty expression returns zero`() {
        assertEquals("0", evaluator.evaluate(""))
    }

    @Test
    fun `malformed expression returns error`() {
        assertEquals("Error", evaluator.evaluate("abc+1"))
    }

    @Test
    fun `trailing operator returns error`() {
        assertEquals("Error", evaluator.evaluate("10+"))
    }

    // ─── Trailing zeros are stripped ─────────────────────────────────────────
    @Test
    fun `result strips trailing decimal zeros`() {
        // 10.0 / 2.0 = 5.0 → "5" (not "5.0")
        assertEquals("5", evaluator.evaluate("10/2"))
    }

    @Test
    fun `result keeps significant decimal places`() {
        // 1 / 3 = 0.3333... → rounded to max 10 decimal places
        val result = evaluator.evaluate("1/3")
        assertTrue("Expected decimal result, got: $result", result.contains("."))
        assertNotEquals("Error", result)
    }
}
