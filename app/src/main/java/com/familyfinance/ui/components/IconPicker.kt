package com.familyfinance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

val CATEGORY_ICONS = mapOf(
    "Fastfood" to Icons.Default.Fastfood,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "AccountBalance" to Icons.Default.AccountBalance,
    "Payments" to Icons.Default.Payments,
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "Home" to Icons.Default.Home,
    "Flight" to Icons.Default.Flight,
    "Celebration" to Icons.Default.Celebration,
    "School" to Icons.Default.School,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "MedicalServices" to Icons.Default.MedicalServices,
    "Pets" to Icons.Default.Pets,
    "Build" to Icons.Default.Build,
    "TheaterComedy" to Icons.Default.TheaterComedy,
    "LocalParking" to Icons.Default.LocalParking,
    "ElectricBolt" to Icons.Default.ElectricBolt,
    "WaterDrop" to Icons.Default.WaterDrop,
    "Lan" to Icons.Default.Lan,
    "CreditCard" to Icons.Default.CreditCard,
    "Savings" to Icons.Default.Savings,
    "LocalGasStation" to Icons.Default.LocalGasStation,
    "Restaurant" to Icons.Default.Restaurant,
    "LocalMall" to Icons.Default.LocalMall,
    "Checkroom" to Icons.Default.Checkroom,
    "Commute" to Icons.Default.Commute,
    "Style" to Icons.Default.Style,
    "Favorite" to Icons.Default.Favorite,
    "LocalCafe" to Icons.Default.LocalCafe,
    "DirectionsBike" to Icons.AutoMirrored.Filled.DirectionsBike,
    "SelfImprovement" to Icons.Default.SelfImprovement
)

fun getIconByName(name: String?): ImageVector {
    return CATEGORY_ICONS[name] ?: Icons.Default.Category
}

@Composable
fun IconPicker(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = modifier.heightIn(max = 200.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CATEGORY_ICONS.keys.toList()) { iconName ->
            val icon = CATEGORY_ICONS[iconName]!!
            val isSelected = iconName == selectedIconName
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                        else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(iconName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconName,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
