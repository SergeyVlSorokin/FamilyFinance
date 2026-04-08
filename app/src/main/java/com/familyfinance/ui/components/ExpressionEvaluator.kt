package com.familyfinance.ui.components

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Evaluates a simple single-operator arithmetic expression.
 *
 * The calculator is designed around a sequential key-press model (like a physical calculator),
 * where the user builds the expression one token at a time. The evaluator therefore handles
 * exactly ONE operator between TWO operands.  This keeps the logic KISS-compliant and avoids
 * the complexity of a full expression parser with operator precedence.
 *
 * Operators supported (FR-023): +  -  *  /  %
 * Arithmetic uses BigDecimal for monetary precision (no floating-point rounding errors).
 *
 * @trace TASK-201
 * @trace FR-023
 */
class ExpressionEvaluator {

    companion object {
        // Regex to split expression into: left operand, operator, right operand.
        // Handles negative operands by allowing a leading minus only at the very start.
        // % is included here so A%B is parsed as (A * B / 100), matching phone-calculator behaviour.
        private val EXPRESSION_REGEX = Regex("""^(-?[\d.]+)([+\-*/%])(-?[\d.]+)$""")
        private const val MAX_SCALE  = 10
    }

    /**
     * Evaluates [expression] and returns the result as a human-readable decimal string.
     * Returns "Error" for division/modulo by zero, malformed input, or parse failures.
     * Returns "0" for an empty/blank expression.
     */
    fun evaluate(expression: String): String {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return "0"

        return try {
            when {
                EXPRESSION_REGEX.matches(trimmed) -> evaluateBinary(trimmed)
                // Single number — validate and return
                else                              -> validateSingle(trimmed)
            }
        } catch (e: ArithmeticException) {
            "Error"
        } catch (e: NumberFormatException) {
            "Error"
        }
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private fun evaluateBinary(expr: String): String {
        val match = EXPRESSION_REGEX.matchEntire(expr) ?: return "Error"
        val (_, leftStr, op, rightStr) = match.groupValues

        val left  = leftStr.toBigDecimalOrNull()  ?: return "Error"
        val right = rightStr.toBigDecimalOrNull() ?: return "Error"

        val result = when (op) {
            "+" -> left.add(right)
            "-" -> left.subtract(right)
            "*" -> left.multiply(right)
            "/" -> {
                if (right.compareTo(BigDecimal.ZERO) == 0) return "Error"
                left.divide(right, MAX_SCALE, RoundingMode.HALF_UP)
            }
            // Phone-calculator % semantics: A % B  =  A * B / 100
            // e.g. 200 % 5  =  200 * 0.05  = 10  ("5% of 200")
            // 100 % 0  =  100 * 0 / 100  = 0  (zero is a valid percentage value)
            "%" -> left.multiply(right).divide(BigDecimal(100), MAX_SCALE, RoundingMode.HALF_UP)
            else -> return "Error"
        }
        return format(result)
    }

    private fun validateSingle(expr: String): String {
        val value = expr.toBigDecimalOrNull() ?: return "Error"
        return format(value)
    }

    /**
     * Strips trailing decimal zeros so that "25.50" → "25.5" and "5.0" → "5",
     * but keeps meaningful decimals like "2.5".
     */
    private fun format(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        // toPlainString avoids scientific notation (e.g. "1E+2" → "100")
        return stripped.toPlainString()
    }
}
