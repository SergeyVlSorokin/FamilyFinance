package com.familyfinance.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp

@Composable
fun CurrencyInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Amount",
    modifier: Modifier = Modifier,
    placeholder: String = "0.00"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        prefix = { Text("$ ") },
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
