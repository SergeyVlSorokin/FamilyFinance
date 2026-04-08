package com.familyfinance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A full-screen-width bottom-sheet–style calculator accessible from [CurrencyInput].
 *
 * Implementation note: we use a [Dialog] with [DialogProperties.usePlatformDefaultWidth] = false
 * instead of [ModalBottomSheet], because [ModalBottomSheet] inherits the bounds of its parent
 * window. When [CurrencyInput] is rendered inside an [AlertDialog] (e.g. "New Account"), the
 * sheet would be clipped to the dialog's narrower window. Using a Dialog with max-width override
 * guarantees the calculator always spans the full device width.
 *
 * UX contract (FR-023):
 *  - Initial display is pre-populated from [initialValue] (or "0").
 *  - Digit/operator buttons build an expression string (e.g. "15.5+10").
 *  - [=] evaluates the current expression and replaces the display.
 *  - [Save] runs a final evaluation (if needed) and calls [onSave] with the result string.
 *  - [Cancel] / dialog dismiss calls [onDismiss] without touching the source field.
 *  - [C] clears the expression to "0".
 *  - [⌫] removes the last character of the expression.
 *
 * @trace TASK-201
 * @trace FR-023
 */
@Composable
fun AmountCalculator(
    initialValue: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val evaluator = remember { ExpressionEvaluator() }

    // Sanitise the seed: if the incoming value is blank / non-numeric, start at "0"
    val seed = remember(initialValue) {
        if (initialValue.isNotBlank() && initialValue.toDoubleOrNull() != null) initialValue else "0"
    }

    // The raw expression string the user is building, e.g. "15.5+10"
    var expression by remember { mutableStateOf(seed) }
    // Whether the expression has already been evaluated (to decide replace-vs-append logic)
    var justEvaluated by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            // Must be false so the dialog fills the FULL device width, not the parent window width
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        // Anchor to the bottom of the full screen, mimicking a bottom sheet
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Drag-handle visual
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ) {}
                    }

                    // ─── Display ──────────────────────────────────────────────
                    DisplayField(expression = expression)

                    Spacer(modifier = Modifier.height(4.dp))

                    // ─── Button grid ──────────────────────────────────────────
                    CalculatorGrid(
                        onDigit     = { digit -> appendInput(expression, justEvaluated, digit).also { (e, ev) -> expression = e; justEvaluated = ev } },
                        onOperator  = { op    -> appendOperator(expression, op).also { (e, ev) -> expression = e; justEvaluated = ev } },
                        onDecimal   = { appendDecimal(expression, justEvaluated).also { (e, ev) -> expression = e; justEvaluated = ev } },
                        onClear     = { expression = "0"; justEvaluated = false },
                        onBackspace = { expression = doBackspace(expression); justEvaluated = false },
                        onEquals    = {
                            val result = evaluator.evaluate(expression)
                            expression = result
                            justEvaluated = true
                        },
                        onSave      = {
                            val result = evaluator.evaluate(expression)
                            if (result != "Error") onSave(result) else onDismiss()
                        },
                        onCancel    = onDismiss
                    )
                }
            }
        }
    }
}


// ─── Display composable ──────────────────────────────────────────────────────

@Composable
private fun DisplayField(expression: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Text(
            text = expression,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── Calculator grid ─────────────────────────────────────────────────────────

@Composable
private fun CalculatorGrid(
    onDigit:    (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal:  () -> Unit,
    onClear:    () -> Unit,
    onBackspace: () -> Unit,
    onEquals:   () -> Unit,
    onSave:     () -> Unit,
    onCancel:   () -> Unit,
) {
    // Row 1: C  <-  %  /
    ButtonRow {
        CalcButton("C",  style = CalcButtonStyle.Function, onClick = onClear)
        CalcButton("⌫",  style = CalcButtonStyle.Function, onClick = onBackspace)
        CalcButton("%",  style = CalcButtonStyle.Operator,  onClick = { onOperator("%") })
        CalcButton("÷",  style = CalcButtonStyle.Operator,  onClick = { onOperator("/") })
    }
    // Row 2: 7  8  9  ×
    ButtonRow {
        CalcButton("7", onClick = { onDigit("7") })
        CalcButton("8", onClick = { onDigit("8") })
        CalcButton("9", onClick = { onDigit("9") })
        CalcButton("×", style = CalcButtonStyle.Operator, onClick = { onOperator("*") })
    }
    // Row 3: 4  5  6  -
    ButtonRow {
        CalcButton("4", onClick = { onDigit("4") })
        CalcButton("5", onClick = { onDigit("5") })
        CalcButton("6", onClick = { onDigit("6") })
        CalcButton("−", style = CalcButtonStyle.Operator, onClick = { onOperator("-") })
    }
    // Row 4: 1  2  3  +
    ButtonRow {
        CalcButton("1", onClick = { onDigit("1") })
        CalcButton("2", onClick = { onDigit("2") })
        CalcButton("3", onClick = { onDigit("3") })
        CalcButton("+", style = CalcButtonStyle.Operator, onClick = { onOperator("+") })
    }
    // Row 5: 0  .  =  (wide)
    ButtonRow {
        CalcButton("0", modifier = Modifier.weight(1f), onClick = { onDigit("0") })
        CalcButton(".", modifier = Modifier.weight(1f), onClick = onDecimal)
        CalcButton("=", modifier = Modifier.weight(2f), style = CalcButtonStyle.Equals, onClick = onEquals)
    }
    // Row 6: Cancel | Save (full-width row split evenly)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel", style = MaterialTheme.typography.titleMedium)
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ButtonRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

private enum class CalcButtonStyle { Default, Operator, Function, Equals }

@Composable
private fun RowScope.CalcButton(
    label: String,
    style: CalcButtonStyle = CalcButtonStyle.Default,
    modifier: Modifier = Modifier.weight(1f),
    onClick: () -> Unit
) {
    val (containerColor, contentColor) = when (style) {
        CalcButtonStyle.Operator  -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        CalcButtonStyle.Function  -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        CalcButtonStyle.Equals    -> Pair(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        )
        CalcButtonStyle.Default   -> Pair(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Expression state helpers ─────────────────────────────────────────────────

private val OPERATORS = setOf('+', '-', '*', '/', '%')

/** Appends a digit character to the expression. */
private fun appendInput(expr: String, justEvaluated: Boolean, digit: String): Pair<String, Boolean> {
    return if (justEvaluated || expr == "0") {
        // After evaluation or when display is just "0", start fresh with the new digit
        Pair(digit, false)
    } else {
        Pair(expr + digit, false)
    }
}

/** Appends an operator, replacing any trailing operator if already present. */
private fun appendOperator(expr: String, op: String): Pair<String, Boolean> {
    if (expr == "Error") return Pair("0$op", false)
    // Replace trailing operator if present
    val trimmed = if (expr.isNotEmpty() && expr.last() in OPERATORS) expr.dropLast(1) else expr
    return Pair(trimmed + op, false)
}

/** Appends a decimal point only if the current operand doesn't already have one. */
private fun appendDecimal(expr: String, justEvaluated: Boolean): Pair<String, Boolean> {
    val base = if (justEvaluated) "0" else expr
    // Find the last operand (after the last operator)
    val lastOpIdx = base.indexOfLast { it in OPERATORS }
    val currentOperand = if (lastOpIdx == -1) base else base.substring(lastOpIdx + 1)
    return if (currentOperand.contains('.')) {
        Pair(base, false) // do not add a second decimal point
    } else if (currentOperand.isEmpty()) {
        Pair("${base}0.", false) // e.g. "15+" → "15+0."
    } else {
        Pair("$base.", false)
    }
}

/** Removes the last character, falling back to "0" when the string becomes empty. */
private fun doBackspace(expr: String): String {
    if (expr == "Error") return "0"
    val trimmed = if (expr.length > 1) expr.dropLast(1) else "0"
    // If only a minus sign remains after deletion, reset to 0
    return if (trimmed == "-") "0" else trimmed
}
