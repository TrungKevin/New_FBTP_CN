package com.trungkien.fbtp_cn.ui.components.owner.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

data class QuickAction(
    val icon: Int,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeQuickActions(
    onManageFields: () -> Unit,
    onBookingList: () -> Unit,
    onAddField: () -> Unit,
    onStatistics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickAction(
            icon = R.drawable.stadium,
            label = "Quản lý sân",
            color = Color(0xFF4CAF50),
            onClick = onManageFields
        ),
        QuickAction(
            icon = R.drawable.event,
            label = "Đặt sân",
            color = Color(0xFF2196F3),
            onClick = onBookingList
        ),
        QuickAction(
            icon = R.drawable.add_circle,
            label = "Thêm sân",
            color = Color(0xFFFF9800),
            onClick = onAddField
        ),
        QuickAction(
            icon = R.drawable.bartchar,
            label = "Thống kê",
            color = Color(0xFF9C27B0),
            onClick = onStatistics
        )
    )

    Column(modifier = modifier) {
        Text(
            text = "Thao tác nhanh",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(actions.size) { index ->
                QuickActionCard(action = actions[index])
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = action.onClick,
        modifier = modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = action.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Giảm từ 1.dp xuống 0.dp
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = action.color.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    painter = painterResource(action.icon),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(24.dp),
                    tint = action.color
                )
            }

            Text(
                text = action.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = action.color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeQuickActions() {
    FBTP_CNTheme {
        HomeQuickActions(
            onManageFields = {},
            onBookingList = {},
            onAddField = {},
            onStatistics = {}
        )
    }
}
