package com.familyfinance.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp

/**
 * A monetary amount input field with an integrated inline calculator.
 *
 * The calculator icon is placed on the **right** side of the field (trailing icon).
 * Tapping the icon opens [AmountCalculator] as a modal bottom sheet.
 * Saving from the calculator replaces the current field value.
 * Cancelling the calculator leaves the field unchanged.
 *
 * @trace TASK-201
 * @trace FR-023
 * @trace TASK-119
 */
@Composable
fun CurrencyInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Amount",
    modifier: Modifier = Modifier,
    placeholder: String = "0.00",
    currency: String? = null
) {
    var showCalculator by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Accept only valid decimal input
            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        prefix = { Text("${currency ?: "$"} ") },
        trailingIcon = {
            IconButton(onClick = { showCalculator = true }) {
                Icon(
                    imageVector = Icons.Outlined.Calculate,
                    contentDescription = "Open calculator",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        ),
        shape = MaterialTheme.shapes.extraLarge,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )

    // Show the calculator bottom-sheet when the icon is clicked
    if (showCalculator) {
        AmountCalculator(
            initialValue = value,
            onSave = { result ->
                onValueChange(result)
                showCalculator = false
            },
            onDismiss = {
                showCalculator = false
            }
        )
    }
}

/**
 * Helper to convert decimal string to Long cents
 */
fun String.toCents(): Long {
    return (this.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
}

/**
 * Helper to convert Long cents to decimal string
 */
fun Long.toDecimalString(): String {
    return if (this == 0L) "" else (this / 100.0).toString()
}
