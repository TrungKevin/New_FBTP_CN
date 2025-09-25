package com.trungkien.fbtp_cn.ui.components.renter.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person

import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun RenterQuickActions(
    onSearchClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header with better contrast
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Quick",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "Thao tác nhanh",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick actions grid with better spacing and text visibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp) // Increased spacing
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp) // Increased spacing
            ) {
                RenterQuickActionCard(
                    action = QuickAction("Tìm sân", Icons.Default.Search),
                    onClick = onSearchClick,
                    modifier = Modifier.weight(1f)
                )

                RenterQuickActionCard(
                    action = QuickAction("Bản đồ", Icons.Default.LocationOn),
                    onClick = onMapClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp) // Increased spacing
            ) {
                RenterQuickActionCard(
                    action = QuickAction("Lịch sử", Icons.Default.DateRange),
                    onClick = onHistoryClick,
                    modifier = Modifier.weight(1f)
                )

                RenterQuickActionCard(
                    action = QuickAction("Hồ sơ", Icons.Default.Person),
                    onClick = onProfileClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RenterQuickActionCard(
    action: QuickAction,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp) // Increased height for better text visibility
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Increased elevation
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Increased padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with background
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.size(40.dp) // Increased icon container size
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp) // Increased icon size
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp)) // Increased spacing

            // Title with better typography
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium, // Changed to titleMedium
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp)) // Small spacing

            // Subtitle with better visibility
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.bodySmall, // Changed to bodySmall
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2, // Allow 2 lines for subtitle
                modifier = Modifier.padding(horizontal = 4.dp) // Add horizontal padding
            )
        }
    }
}

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val subtitle: String = ""
)

private val quickActions = listOf(
    QuickAction("Tìm sân", Icons.Default.Search),
    QuickAction("Bản đồ", Icons.Default.LocationOn),
    QuickAction("Lịch sử", Icons.Default.DateRange),
    QuickAction("Hồ sơ", Icons.Default.Person)
)

@Preview
@Composable
fun RenterQuickActionsPreview() {
    FBTP_CNTheme {
        RenterQuickActions(
            onSearchClick = {},
            onMapClick = {},
            onHistoryClick = {},
            onProfileClick = {}
        )
    }
}
