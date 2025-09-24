package com.trungkien.fbtp_cn.ui.components.renter.bookinghis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.trungkien.fbtp_cn.model.ServiceLine
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.ReviewSummary
import com.trungkien.fbtp_cn.repository.FieldRepository
import com.trungkien.fbtp_cn.repository.ReviewRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterBookingDetailSheet(
    booking: Booking,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load Field info for richer display
    var field by remember(booking.fieldId) { mutableStateOf<Field?>(null) }
    var reviewSummary by remember(booking.fieldId) { mutableStateOf<ReviewSummary?>(null) }
    
    LaunchedEffect(booking.fieldId) {
        val fieldRepo = FieldRepository()
        val reviewRepo = ReviewRepository()
        
        // Load field data
        fieldRepo.getFieldById(booking.fieldId).onSuccess { f -> field = f }
        
        // Load review summary
        reviewRepo.getReviewSummary(booking.fieldId).onSuccess { summary -> 
            reviewSummary = summary 
        }
    }
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
                InfoRow(R.drawable.stadium, "Tên sân", field?.name ?: booking.fieldId, valueColor = Color(0xFF1F2937))
                InfoRow(R.drawable.map, "Địa chỉ", field?.address ?: "—", valueColor = Color(0xFF6B7280))
                // ✅ FIX: Hiển thị giá cơ bản với số giờ thực tế từ booking (áp dụng công thức từ checkout screen)
                val actualDuration = formatDurationFromSlots(booking.slotsCount)
                InfoRow(R.drawable.money, "Giá cơ bản", formatCurrency(booking.basePrice) + "/$actualDuration", valueColor = Color(0xFFDC2626))
                // ✅ FIX: Hiển thị đánh giá từ ReviewSummary thay vì từ Field
                val ratingText = reviewSummary?.let { summary ->
                    val score = if (summary.averageRating > 0f) String.format("%.1f", summary.averageRating) else "0.0"
                    "⭐$score (${summary.totalReviews} đánh giá)"
                } ?: "⭐0.0 (0 đánh giá)"
                InfoRow(R.drawable.star, "Đánh giá", ratingText, valueColor = Color(0xFFF59E0B))
            }

            // Thời gian với card style
            SectionCard(
                title = "📅 Thời gian",
                titleColor = Color(0xFF7C3AED)
            ) {
                InfoRow(R.drawable.calendar, "Ngày", booking.date, valueColor = Color(0xFF1F2937))
                InfoRow(R.drawable.schedule, "Giờ", "${booking.startAt} - ${booking.endAt}", valueColor = Color(0xFF1F2937))
            }

            // Dịch vụ thêm với card style
            SectionCard(
                title = "🛒 Dịch vụ thêm",
                titleColor = Color(0xFFEA580C)
            ) {
                if (booking.serviceLines.isEmpty()) {
                    Text(
                        "Không có dịch vụ thêm",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    booking.serviceLines.forEach { service ->
                        InfoRow(
                            R.drawable.bookmark,
                            service.name,
                            "+${service.price}₫ x${service.quantity}",
                            valueColor = Color(0xFF059669)
                        )
                    }
                }
            }

            // Thông tin thanh toán & tổng tiền
            SectionCard(
                title = "💳 Thanh toán",
                titleColor = Color(0xFF2563EB)
            ) {
                // ✅ FIX: Hiển thị số giờ thực tế từ slots (áp dụng công thức từ checkout screen)
                InfoRow(
                    R.drawable.schedule,
                    "Số giờ",
                    formatDurationFromSlots(booking.slotsCount)
                )
                InfoRow(R.drawable.schedule, "Số slot", "${booking.slotsCount}")
                InfoRow(R.drawable.money, "Tiền dịch vụ", formatCurrency(booking.servicePrice), boldValue = true, valueColor = Color(0xFF059669))
                // ✅ FIX: Mặc định phương thức thanh toán là "Thanh toán trực tiếp tại sân"
                InfoRow(R.drawable.bookmark, "Phương thức", booking.paymentMethod ?: "Thanh toán trực tiếp tại sân")
                InfoRow(R.drawable.event, "Trạng thái", booking.status)
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
                        formatCurrency(booking.totalPrice),
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

private fun formatCurrency(amount: Long): String {
    return try {
        val nf = java.text.NumberFormat.getInstance(java.util.Locale("vi", "VN"))
        nf.format(amount) + "₫"
    } catch (e: Exception) {
        amount.toString() + "₫"
    }
}

// ✅ FIX: Function format số giờ từ slots theo công thức của checkout screen
private fun formatDurationFromSlots(slotsCount: Int): String {
    if (slotsCount <= 0) return "0 phút"
    
    // ✅ Công thức từ checkout screen: hours = max(0, (count - 1)) * 0.5
    val hours = ((slotsCount - 1).coerceAtLeast(0)) * 0.5
    
    // Convert hours to total minutes
    val totalMinutes = (hours * 60).toInt()
    val hoursPart = totalMinutes / 60
    val minutesPart = totalMinutes % 60
    
    return when {
        hoursPart == 0 -> "$minutesPart phút"
        minutesPart == 0 -> "$hoursPart giờ"
        else -> "$hoursPart giờ $minutesPart phút"
    }
}

// ✅ FIX: Function format số giờ giống như trong BookingSummaryCard (giữ lại để tương thích)
private fun formatDurationFromMinutes(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0 phút"
    
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    
    return when {
        hours == 0 -> "$minutes phút"
        minutes == 0 -> "$hours giờ"
        else -> "$hours giờ $minutes phút"
    }
}

@Preview
@Composable
fun RenterBookingDetailSheetPreview() {
    val sampleBooking = Booking(
        bookingId = "booking_preview_001",
        renterId = "renter_preview_001",
        ownerId = "owner_preview_001",
        fieldId = "field_preview_001",
        date = "2024-01-01",
        startAt = "18:00",
        endAt = "20:00",
        slotsCount = 2,
        minutes = 120,
        basePrice = 200000,
        servicePrice = 60000,
        totalPrice = 460000,
        status = "PAID",
        serviceLines = listOf(
            ServiceLine(
                serviceId = "svc_water",
                name = "Nước uống",
                billingType = "PER_UNIT",
                price = 10000,
                quantity = 2,
                lineTotal = 20000
            ),
            ServiceLine(
                serviceId = "svc_ball",
                name = "Bóng đá",
                billingType = "FLAT_PER_BOOKING",
                price = 50000,
                quantity = 1,
                lineTotal = 50000
            )
        )
    )

    Surface(color = Color(0xFFF8FAFC)) {
        RenterBookingDetailSheet(booking = sampleBooking, onClose = {})
    }
}