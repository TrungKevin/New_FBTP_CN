package com.trungkien.fbtp_cn.ui.components.owner.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.repository.FieldRepository
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailManage(
    booking: Booking,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onSuggestTime: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Chi tiết đặt sân",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header với thông tin sân
            EnhancedFieldInfoHeader(booking = booking)

            Spacer(modifier = Modifier.height(12.dp))

            // Thông tin người đặt sân
            EnhancedCustomerInfoSection(booking = booking)

            Spacer(modifier = Modifier.height(12.dp))

            // Thông tin đặt sân
            EnhancedBookingInfoSection(booking = booking)

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            EnhancedActionButtonsSection(
                booking = booking,
                onConfirm = onConfirm,
                onCancel = onCancel,
                onSuggestTime = onSuggestTime,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EnhancedFieldInfoHeader(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    // Load field name
    var fieldName by remember(booking.fieldId) { mutableStateOf<String?>(null) }
    LaunchedEffect(booking.fieldId) {
        try {
            FieldRepository().getFieldById(booking.fieldId).getOrNull()?.let { f -> fieldName = f.name }
        } catch (_: Exception) {}
    }
    // Status color - giữ nguyên màu cũ
    val statusColor = when (booking.status) {
        "Đã xác nhận" -> Color(0xFF4CAF50)
        "Chờ xác nhận" -> Color(0xFFFF9800)
        "Đã hủy" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon sân với background
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stadium),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tên sân
            Text(
                text = "Sân ${fieldName ?: booking.fieldId}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Thời gian
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.schedule),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${booking.startAt} - ${booking.endAt}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status badge - giữ nguyên màu cũ
            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = booking.status,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun EnhancedCustomerInfoSection(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var renterName by remember(booking.renterId) { mutableStateOf("") }
    var renterPhone by remember(booking.renterId) { mutableStateOf("") }
    var renterEmail by remember(booking.renterId) { mutableStateOf("") }
    var renterAddress by remember(booking.renterId) { mutableStateOf("") }
    val avatarData by produceState(initialValue = "", key1 = booking.renterId) {
        if (booking.renterId.isNotBlank()) {
            UserRepository().getUserById(
                booking.renterId,
                onSuccess = { u ->
                    renterName = u.name
                    renterPhone = u.phone
                    renterEmail = u.email
                    renterAddress = u.address
                    value = u.avatarUrl ?: ""
                },
                onError = { _ -> value = "" }
            )
        } else value = ""
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Tiêu đề section
            SectionTitle(
                icon = Icons.Default.Person,
                title = "Thông tin người đặt sân"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customer profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                // Avatar
                run {
                    val decoded = try {
                        if (avatarData.isNotBlank()) {
                            val base = if (avatarData.startsWith("data:image")) avatarData.substringAfter(",") else avatarData
                            val bytes = Base64.decode(base, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else null
                    } catch (_: Exception) { null }
                    if (decoded != null) {
                        androidx.compose.foundation.Image(
                            bitmap = decoded.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (avatarData.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (avatarData.startsWith("http") || avatarData.startsWith("data:image")) avatarData else "data:image/jpeg;base64,$avatarData")
                                .allowHardware(false)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (renterName.isNotBlank()) renterName else booking.renterId,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))


                }
            }

            // Contact info với design đẹp hơn
            EnhancedInfoRow(
                icon = Icons.Default.Phone,
                label = "Số điện thoại",
                value = renterPhone
            )

            Spacer(modifier = Modifier.height(12.dp))

            EnhancedInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = renterEmail
            )

        }
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EnhancedInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EnhancedBookingInfoSection(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Tiêu đề
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.schedule),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Thông tin đặt sân",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thông tin chi tiết
            EnhancedInfoRow(
                icon = Icons.Default.DateRange,
                label = "Ngày đặt",
                value = booking.date
            )

            Spacer(modifier = Modifier.height(12.dp))

            EnhancedInfoRow(
                icon = Icons.Default.Add,
                label = "Khung giờ",
                value = "${booking.startAt} - ${booking.endAt}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            val totalText = "${String.format("%,d", booking.totalPrice).replace(',', '.')} VND"
            EnhancedInfoRow(
                icon = Icons.Default.Check,
                label = "Giá tiền",
                value = totalText
            )

            Spacer(modifier = Modifier.height(12.dp))

            EnhancedInfoRow(
                icon = Icons.Default.Face,
                label = "Ghi chú",
                value = booking.notes ?: "—"
            )
        }
    }
}

@Composable
private fun EnhancedActionButtonsSection(
    booking: Booking,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onSuggestTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Chỉ hiển thị action buttons nếu là "Chờ xác nhận"
    if (booking.status == "Chờ xác nhận") {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Button xác nhận - giữ màu xanh cũ
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Xác nhận đặt sân",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Button hủy - giữ màu đỏ cũ
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF44336)
                    ),
                    border = BorderStroke(2.dp, Color(0xFFF44336)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hủy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Button gợi ý
                OutlinedButton(
                    onClick = onSuggestTime,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.schedule),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Gợi ý",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    } else {
        // Nếu không phải "Chờ xác nhận", hiển thị message
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (booking.status) {
                        "Đã xác nhận" -> "Đã xác nhận đặt sân thành công"
                        "Đã hủy" -> "Đặt sân đã được hủy"
                        else -> "Không có hành động khả dụng"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewBookingDetailManage() {
    val mockBooking = Booking(
        bookingId = "b1",
        renterId = "user_001",
        ownerId = "owner_001",
        fieldId = "1",
        date = "2024-01-15",
        startAt = "08:00",
        endAt = "09:00",
        slotsCount = 2,
        minutes = 60,
        basePrice = 150000,
        servicePrice = 0,
        totalPrice = 150000,
        status = "PENDING"
    )

    FBTP_CNTheme {
        BookingDetailManage(
            booking = mockBooking,
            onConfirm = {},
            onCancel = {},
            onSuggestTime = {},
            onBack = {}
        )
    }
}
