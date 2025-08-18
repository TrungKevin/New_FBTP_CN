package com.trungkien.fbtp_cn.ui.components.renter.fieldsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

data class FieldTypeFilter(
    val id: String,
    val name: String,
    val iconResId: Int,
    val isSelected: Boolean = false
)

@Composable
fun RenterFieldTypeFilter(
    selectedType: String? = null,
    onTypeSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fieldTypes = remember {
        listOf(
            FieldTypeFilter("all", "Tất cả", R.drawable.star, selectedType == null),
            FieldTypeFilter("football", "Bóng đá", R.drawable.football, selectedType == "football"),
            FieldTypeFilter("tennis", "Tennis", R.drawable.tennis, selectedType == "tennis"),
            FieldTypeFilter("badminton", "Cầu lông", R.drawable.badminton, selectedType == "badminton"),
            FieldTypeFilter("pickleball", "Pickleball", R.drawable.pickleball, selectedType == "pickleball")
        )
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Loại sân",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(
                onClick = { /* TODO: Show all types */ }
            ) {
                Text(
                    text = "Xem tất cả",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Horizontal scrollable filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(fieldTypes) { fieldType ->
                FieldTypeChip(
                    fieldType = fieldType,
                    onClick = { onTypeSelected(fieldType.id) }
                )
            }
        }
    }
}

@Composable
fun FieldTypeChip(
    fieldType: FieldTypeFilter,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (fieldType.isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (fieldType.isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = fieldType.iconResId),
                contentDescription = fieldType.name,
                tint = if (fieldType.isSelected) 
                    Color.White 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = fieldType.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (fieldType.isSelected) 
                    Color.White 
                else 
                    MaterialTheme.colorScheme.onSurface,
                fontWeight = if (fieldType.isSelected) 
                    FontWeight.Bold 
                else 
                    FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RenterFieldTypeFilterPreview() {
    FBTP_CNTheme {
        RenterFieldTypeFilter()
    }
}

@Preview(showBackground = true)
@Composable
fun FieldTypeChipPreview() {
    FBTP_CNTheme {
        FieldTypeChip(
            fieldType = FieldTypeFilter("tennis", "Tennis", R.drawable.tennis, true)
        )
    }
}
