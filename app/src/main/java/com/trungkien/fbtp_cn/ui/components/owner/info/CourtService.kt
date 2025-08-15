package com.trungkien.fbtp_cn.ui.components.owner.info

import android.R.attr.rotation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.model.Field

@Composable
fun CourtService(field: Field, modifier: Modifier = Modifier) {
    var isServicesCollapsed by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // BẢNG GIÁ SÂN
        Text(
            text = "BẢNG GIÁ SÂN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Bảng giá
        CourtPriceTable()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // DANH SÁCH DỊCH VỤ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DANH SÁCH DỊCH VỤ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isServicesCollapsed = !isServicesCollapsed }
            ) {
                Text(
                    text = if (isServicesCollapsed) "Mở rộng" else "Rút gọn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(
                            rotationZ = if (isServicesCollapsed) 0f else 90f
                        )
                )
            }
        }
        
        if (!isServicesCollapsed) {
            Spacer(modifier = Modifier.height(16.dp))
            ServicesList()
        }
    }
}

@Composable
private fun CourtPriceTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
            ) {
                // Thứ
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Thứ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Khung giờ
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Khung giờ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Giá
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Giá",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Dữ liệu giá
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thứ 2-6
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T2 - T6",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Khung giờ 5h-9h
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "5h - 9h",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Giá 120.000
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "120.000 ₫",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thứ 2-6 (span)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Khung giờ 9h-17h
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "9h - 17h",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Giá 120.000
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "120.000 ₫",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thứ 2-6 (span)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Khung giờ 17h-23h
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "17h - 23h",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Giá 170.000
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "170.000 ₫",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Cuối tuần
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thứ 7-CN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T7 - CN",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Khung giờ 5h-9h
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "5h - 9h",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Giá 120.000
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "120.000 ₫",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thứ 7-CN (span)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Khung giờ 9h-17h
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "9h - 17h",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Giá 120.000
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "120.000 ₫",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thứ 7-CN (span)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Khung giờ 17h-23h
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "17h - 23h",
                        textAlign = TextAlign.Center
                    )
                }
                
                // Giá 170.000
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "170.000 ₫",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ServicesList() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Banh
        ServiceCategory(
            title = "Banh",
            services = listOf(
                ServiceItem("Hộp banh", "180.000 ₫ / Trái"),
                ServiceItem("Hộp Banh", "180.000 ₫")
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nước đóng chai
        ServiceCategory(
            title = "Nước đóng chai",
            services = listOf(
                ServiceItem("Revive", "15.000 ₫ / Chai"),
                ServiceItem("Red bull", "25.000 ₫ / Chai"),
                ServiceItem("Aqua", "15.000 ₫ / Chai"),
                ServiceItem("Nước suối", "10.000 ₫ / Chai"),
                ServiceItem("Bugari", "16.000 ₫ / Chai"),
                ServiceItem("Bogari", "30.000 ₫ / Chai")
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phí thuê vợt
        ServiceCategory(
            title = "Phí Thuê Vợt",
            services = listOf(
                ServiceItem("Phí Thuê Vợt Banh", "20.000 ₫ / Cái")
            )
        )
    }
}

@Composable
private fun ServiceCategory(
    title: String,
    services: List<ServiceItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
            
            // Services
            services.forEach { service ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = service.price,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

data class ServiceItem(
    val name: String,
    val price: String
)


