package com.trungkien.fbtp_cn.ui.components.owner.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary

/**
 * Component hiển thị thông tin vị trí đã chọn
 */
@Composable
fun LocationInfoCard(
    location: GeoLocation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = GreenPrimary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Location confirmed",
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Vị trí đã chọn",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Lat: ${String.format("%.6f", location.lat)}, Lng: ${String.format("%.6f", location.lng)}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
            
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = GreenPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Component hiển thị trạng thái chọn vị trí
 */
@Composable
fun LocationSelectionStatus(
    hasLocation: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasLocation) 
                GreenPrimary.copy(alpha = 0.1f) 
            else 
                Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = GreenPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (hasLocation) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                    contentDescription = "Status",
                    tint = if (hasLocation) GreenPrimary else Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = when {
                    isLoading -> "Đang tìm vị trí..."
                    hasLocation -> "Vị trí đã được chọn"
                    else -> "Chưa chọn vị trí"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    isLoading -> Color(0xFF666666)
                    hasLocation -> GreenPrimary
                    else -> Color(0xFFFF9800)
                }
            )
        }
    }
}
