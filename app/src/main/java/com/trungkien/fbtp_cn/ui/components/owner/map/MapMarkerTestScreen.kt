package com.trungkien.fbtp_cn.ui.components.owner.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary

/**
 * Demo component để test marker hiển thị đúng vị trí
 */
@Composable
fun MapMarkerTestScreen(
    modifier: Modifier = Modifier
) {
    // Test data với các vị trí khác nhau
    val testFields = listOf(
        Field(
            fieldId = "test1",
            ownerId = "test",
            name = "Sân Tennis Quận 1",
            address = "123 Nguyễn Huệ, Quận 1, TP.HCM",
            geo = GeoLocation(lat = 10.7769, lng = 106.7009, geohash = ""),
            sports = listOf("TENNIS")
        ),
        Field(
            fieldId = "test2", 
            ownerId = "test",
            name = "Sân Bóng Đá Quận 7",
            address = "456 Nguyễn Thị Thập, Quận 7, TP.HCM",
            geo = GeoLocation(lat = 10.7374, lng = 106.7214, geohash = ""),
            sports = listOf("FOOTBALL")
        ),
        Field(
            fieldId = "test3",
            ownerId = "test", 
            name = "Sân Cầu Lông Quận 3",
            address = "789 Võ Văn Tần, Quận 3, TP.HCM",
            geo = GeoLocation(lat = 10.7908, lng = 106.6881, geohash = ""),
            sports = listOf("BADMINTON")
        )
    )
    
    var selectedFieldIndex by remember { mutableStateOf(0) }
    val selectedField = testFields[selectedFieldIndex]
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header với thông tin test
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Test Marker Position",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Chọn sân để test marker hiển thị đúng vị trí:",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Buttons để chọn sân test
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    testFields.forEachIndexed { index, field ->
                        Button(
                            onClick = { selectedFieldIndex = index },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (index == selectedFieldIndex) GreenPrimary else Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = field.sports.first(),
                                fontSize = 12.sp,
                                color = if (index == selectedFieldIndex) Color.White else Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Thông tin sân được chọn
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = selectedField.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = selectedField.address,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tọa độ: ${String.format("%.6f", selectedField.geo.lat)}, ${String.format("%.6f", selectedField.geo.lng)}",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Map view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            OwnerMapView(
                field = selectedField,
                onMarkerClick = {
                    // Test marker click
                }
            )
        }
    }
}
