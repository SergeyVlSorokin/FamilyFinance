package com.familyfinance.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyfinance.ui.components.CurrencyInput
import com.familyfinance.ui.components.formatAsCurrency
import com.familyfinance.ui.components.toCents
import com.familyfinance.ui.components.toDecimalString

/**
 * Dialog for entering a refund linked to an original transaction.
 * @trace TASK-203
 */
@Composable
fun RefundEntryDialog(
    refundContext: RefundContext,
    onDismiss: () -> Unit,
    onConfirm: (amountCents: Long, moreFollow: Boolean) -> Unit
) {
    val original = refundContext.target
    val remainingCents = kotlin.math.abs(original.amountCents) - refundContext.refundedSoFar
    
    var amountText by remember { mutableStateOf(remainingCents.coerceAtLeast(0L).toDecimalString()) }
    var moreRefundsFollow by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Refund") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Read-only original transaction info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Account:", style = MaterialTheme.typography.bodySmall)
                        Text(refundContext.accountName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Category:", style = MaterialTheme.typography.bodySmall)
                        Text(refundContext.categoryName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Project:", style = MaterialTheme.typography.bodySmall)
                        Text(refundContext.projectName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Summary of amounts
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Original amount:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = original.amountCents.formatAsCurrency(original.currencyCode),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Already returned:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = refundContext.refundedSoFar.formatAsCurrency(original.currencyCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Refund entry fields
                CurrencyInput(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Refund Amount",
                    currency = original.currencyCode
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = moreRefundsFollow,
                        onCheckedChange = { moreRefundsFollow = it }
                    )
                    Text(
                        text = "More refunds will follow",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(amountText.toCents(), moreRefundsFollow)
                },
                enabled = amountText.isNotBlank() && amountText.toDoubleOrNull() ?: 0.0 > 0.0
            ) {
                Text("Save Refund")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
