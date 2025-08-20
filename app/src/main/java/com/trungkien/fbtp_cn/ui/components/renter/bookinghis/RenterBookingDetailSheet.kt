package com.trungkien.fbtp_cn.ui.components.renter.bookinghis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.ServiceOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterBookingDetailSheet(
    booking: Booking,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFFF8FAFC),
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header với gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                        )
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "Chi tiết đặt sân",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Thông tin sân với card style
            SectionCard(
                title = "🏟️ Thông tin sân",
                titleColor = Color(0xFF059669)
            ) {
                InfoRow(R.drawable.stadium, "Tên sân", booking.fieldName, valueColor = Color(0xFF1F2937))
                InfoRow(R.drawable.map, "Địa chỉ", booking.fieldAddress.ifBlank { "—" }, valueColor = Color(0xFF6B7280))
                InfoRow(R.drawable.bartchar, "Giá", "${booking.fieldPrice}₫/giờ", valueColor = Color(0xFFDC2626))
                InfoRow(R.drawable.star, "Đánh giá", "⭐4.5 (128 đánh giá)", valueColor = Color(0xFFF59E0B))
            }

            // Thời gian với card style
            SectionCard(
                title = "📅 Thời gian",
                titleColor = Color(0xFF7C3AED)
            ) {
                InfoRow(R.drawable.event, "Ngày", booking.date, valueColor = Color(0xFF1F2937))
                InfoRow(R.drawable.event, "Giờ", booking.timeRange, valueColor = Color(0xFF1F2937))
            }

            // Dịch vụ thêm với card style
            SectionCard(
                title = "🛒 Dịch vụ thêm",
                titleColor = Color(0xFFEA580C)
            ) {
                if (booking.services.isEmpty()) {
                    Text(
                        "Không có dịch vụ thêm",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    booking.services.forEach { service ->
                        InfoRow(
                            R.drawable.bookmark,
                            service.serviceName,
                            "+${service.price}₫ x${service.quantity}",
                            valueColor = Color(0xFF059669)
                        )
                    }
                }
            }

            // Tổng tiền với highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FDF4))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "💰 Tổng thanh toán",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                    Text(
                        "${booking.totalPrice}₫",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button với gradient
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Đóng",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    titleColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(
    iconResId: Int,
    label: String,
    value: String,
    boldValue: Boolean = false,
    valueColor: Color = Color(0xFF1F2937)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            value,
            fontWeight = if (boldValue) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
fun RenterBookingDetailSheetPreview() {
    val sampleBooking = Booking(
        id = "booking_preview_001",
        fieldId = "field_preview_001",
        fieldName = "Sân bóng ABC",
        timeRange = "18:00 - 20:00",
        status = "Confirmed",
        fieldAddress = "123 Đường XYZ, Quận 1, TP.HCM",
        fieldPrice = 200000,
        date = "2024-01-01",
        totalPrice = 400000,
        services = listOf(
            ServiceOrder(
                id = "order_1",
                serviceId = "svc_water",
                serviceName = "Nước uống",
                quantity = 2,
                price = 10000
            ),
            ServiceOrder(
                id = "order_2",
                serviceId = "svc_ball",
                serviceName = "Bóng đá",
                quantity = 1,
                price = 50000
            )
        )
    )

    Surface(color = Color(0xFFF8FAFC)) {
        RenterBookingDetailSheet(booking = sampleBooking, onClose = {})
    }
}