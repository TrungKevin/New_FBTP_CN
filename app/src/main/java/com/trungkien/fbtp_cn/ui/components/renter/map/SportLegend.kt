package com.trungkien.fbtp_cn.ui.components.renter.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.trungkien.fbtp_cn.ui.components.owner.map.SportMarkerIcon

/**
 * Legend hiển thị các loại marker sân để người dùng dễ nhận biết
 */
@Composable
fun SportLegend(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Text(
                text = "Loại sân",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Badminton
            LegendItem(
                sportType = "BADMINTON",
                label = "Sân cầu lông",
                color = Color(0xFF4CAF50)
            )
            
            // Football
            LegendItem(
                sportType = "FOOTBALL",
                label = "Sân bóng đá",
                color = Color(0xFF2196F3)
            )
            
            // Pickleball
            LegendItem(
                sportType = "PICKLEBALL",
                label = "Sân pickleball",
                color = Color(0xFF9C27B0)
            )
            
            // Tennis
            LegendItem(
                sportType = "TENNIS",
                label = "Sân tennis",
                color = Color(0xFFFF4444)
            )
        }
    }
}

@Composable
private fun LegendItem(
    sportType: String,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Marker icon - Tăng kích thước để nhìn rõ hơn và thấy viền màu
        MarkerPreview(
            sportType = sportType,
            size = 48.dp
        )
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MarkerPreview(
    sportType: String,
    size: androidx.compose.ui.unit.Dp
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    AndroidView(
        modifier = Modifier.size(size),
        factory = { ctx ->
            android.widget.ImageView(ctx).apply {
                // Tăng kích thước marker trong legend để viền màu hiển thị rõ hơn
                // 60px cho marker trong legend (lớn hơn marker trong map là 48px)
                val marker = SportMarkerIcon(ctx, sportType, 60)
                setImageDrawable(marker)
            }
        }
    )
}

