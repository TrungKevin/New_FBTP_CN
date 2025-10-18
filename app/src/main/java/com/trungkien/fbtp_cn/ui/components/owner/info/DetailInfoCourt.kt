package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.OpenHours
import com.trungkien.fbtp_cn.model.GeoLocation

@Composable
fun DetailInfoCourt(
    field: Field, 
    fieldViewModel: com.trungkien.fbtp_cn.viewmodel.FieldViewModel? = null,
    onEditClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEditMode by remember { mutableStateOf(false) }
    var editedField by remember { mutableStateOf(field) }
    
    // Cập nhật editedField khi field thay đổi
    LaunchedEffect(field) {
        editedField = field
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Thông tin cơ bản
        InfoCard(
            title = "Thông tin cơ bản",
            icon = Icons.Default.Info,
            showEditButton = fieldViewModel != null,
            isEditMode = isEditMode,
            onEditClick = { isEditMode = !isEditMode },
            onSaveClick = {
                // Validation
                if (editedField.name.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập tên sân", Toast.LENGTH_SHORT).show()
                    return@InfoCard
                }
                if (editedField.address.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
                    return@InfoCard
                }
                
                // Lưu thay đổi
                fieldViewModel?.handleEvent(com.trungkien.fbtp_cn.viewmodel.FieldEvent.UpdateField(editedField))
                isEditMode = false
                Toast.makeText(context, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show()
            },
            onCancelClick = {
                // Hủy thay đổi
                editedField = field
                isEditMode = false
            }
        ) {
            EditableInfoRowItem(
                icon = Icons.Default.SportsSoccer,
                label = "Loại sân",
                value = if (editedField.sports.contains("FOOTBALL") && editedField.footballFieldType != null) {
                    // ✅ FIX: Hiển thị loại sân bóng đá nếu có
                    val fieldTypeText = when (editedField.footballFieldType) {
                        "5_PLAYERS" -> "Sân 5 người"
                        "7_PLAYERS" -> "Sân 7 người"
                        "11_PLAYERS" -> "Sân 11 người"
                        else -> editedField.footballFieldType
                    }
                    "FOOTBALL - $fieldTypeText"
                } else {
                    editedField.sports.joinToString(", ").uppercase()
                },
                valueColor = MaterialTheme.colorScheme.primary,
                isEditMode = isEditMode,
                onValueChange = { /* Loại sân không thể chỉnh sửa */ }
            )
            InfoRowItem(
                icon = Icons.Default.Star,
                label = "Điểm đánh giá",
                value = "${String.format("%.1f", editedField.averageRating)}/5.0 (${editedField.totalReviews} đánh giá)",
                valueColor = Color(0xFFFFB800)
            )
            InfoRowItem(
                icon = Icons.Default.Schedule,
                label = "Thời gian slot",
                value = "${editedField.slotMinutes} phút"
            )
            EditableInfoRowItem(
                icon = Icons.Default.Description,
                label = "Mô tả",
                value = editedField.description,
                isEditMode = isEditMode,
                onValueChange = { editedField = editedField.copy(description = it) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Thông tin liên hệ
        InfoCard(
            title = "Thông tin liên hệ",
            icon = Icons.Default.ContactPhone,
            showEditButton = fieldViewModel != null,
            isEditMode = isEditMode,
            onEditClick = { isEditMode = !isEditMode },
            onSaveClick = {
                // Validation
                if (editedField.name.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập tên sân", Toast.LENGTH_SHORT).show()
                    return@InfoCard
                }
                if (editedField.address.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
                    return@InfoCard
                }
                
                // Lưu thay đổi
                fieldViewModel?.handleEvent(com.trungkien.fbtp_cn.viewmodel.FieldEvent.UpdateField(editedField))
                isEditMode = false
                Toast.makeText(context, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show()
            },
            onCancelClick = {
                // Hủy thay đổi
                editedField = field
                isEditMode = false
            }
        ) {
            EditableInfoRowItem(
                icon = Icons.Default.LocationOn,
                label = "Địa chỉ",
                value = editedField.address,
                isEditMode = isEditMode,
                onValueChange = { editedField = editedField.copy(address = it) }
            )
            
            // Giờ hoạt động - hiển thị trên cùng một hàng
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Giờ hoạt động",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    if (isEditMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editedField.openHours.start,
                                onValueChange = { 
                                    editedField = editedField.copy(
                                        openHours = editedField.openHours.copy(start = it)
                                    )
                                },
                                label = { Text("Giờ mở") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editedField.openHours.end,
                                onValueChange = { 
                                    editedField = editedField.copy(
                                        openHours = editedField.openHours.copy(end = it)
                                    )
                                },
                                label = { Text("Giờ đóng") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    } else {
                        Text(
                            text = "${editedField.openHours.start} - ${editedField.openHours.end}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            
            EditableInfoRowItem(
                icon = Icons.Default.Phone,
                label = "Số điện thoại",
                value = editedField.contactPhone,
                valueColor = MaterialTheme.colorScheme.primary,
                isEditMode = isEditMode,
                onValueChange = { editedField = editedField.copy(contactPhone = it) }
            )
            
            if (editedField.geo.lat != 0.0 && editedField.geo.lng != 0.0) {
                InfoRowItem(
                    icon = Icons.Default.MyLocation,
                    label = "Tọa độ",
                    value = "${String.format("%.4f", editedField.geo.lat)}, ${String.format("%.4f", editedField.geo.lng)}"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Tiện ích và dịch vụ
        if (field.amenities.isNotEmpty()) {
            InfoCard(
                title = "Tiện ích & Dịch vụ",
                icon = Icons.Default.LocalOffer
            ) {
                field.amenities.forEach { amenity ->
                    InfoRowItem(
                        icon = getAmenityIcon(amenity),
                        label = "Tiện ích",
                        value = getAmenityDisplayName(amenity),
                        valueColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Trạng thái hoạt động
        InfoCard(
            title = "Trạng thái",
            icon = Icons.Default.Circle
        ) {
            InfoRowItem(
                icon = if (field.active) Icons.Default.CheckCircle else Icons.Default.Cancel,
                label = "Trạng thái hoạt động",
                value = if (field.active) "Đang hoạt động" else "Tạm ngưng",
                valueColor = if (field.active) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            if (field.active) {
                InfoRowItem(
                    icon = Icons.Default.Visibility,
                    label = "Hiển thị công khai",
                    value = "Có",
                    valueColor = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    showEditButton: Boolean = false,
    isEditMode: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    onSaveClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Buttons chỉnh sửa/lưu/hủy
                if (showEditButton) {
                    if (isEditMode) {
                        // Edit mode: hiển thị button Lưu và Hủy
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onCancelClick ?: {},
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hủy",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = onSaveClick ?: {},
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Lưu",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // View mode: hiển thị button Chỉnh sửa
                        IconButton(
                            onClick = onEditClick ?: {},
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun EditableInfoRowItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isEditMode: Boolean = false,
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        when {
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            painter != null -> {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            else -> {
                Text(
                    text = "💰",
                    fontSize = 16.sp,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            if (isEditMode) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = valueColor,
                        unfocusedTextColor = valueColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = valueColor,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
fun InfoRowItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isPrice: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        when {
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            painter != null -> {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            else -> {
                Text(
                    text = "💰",
                    fontSize = 16.sp,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = if (isPrice) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

private fun getAmenityIcon(amenity: String): ImageVector {
    return when (amenity.uppercase()) {
        "PARKING" -> Icons.Default.LocalParking
        "SHOWER" -> Icons.Default.Shower
        "EQUIPMENT" -> Icons.Default.Sports
        "WIFI" -> Icons.Default.Wifi
        "AC" -> Icons.Default.AcUnit
        "FOOD" -> Icons.Default.Restaurant
        "DRINKS" -> Icons.Default.LocalCafe
        "LOCKER" -> Icons.Default.Lock
        else -> Icons.Default.Star
    }
}

private fun getAmenityDisplayName(amenity: String): String {
    return when (amenity.uppercase()) {
        "PARKING" -> "Bãi đỗ xe"
        "SHOWER" -> "Phòng tắm"
        "EQUIPMENT" -> "Thiết bị thể thao"
        "WIFI" -> "WiFi miễn phí"
        "AC" -> "Điều hòa"
        "FOOD" -> "Dịch vụ ăn uống"
        "DRINKS" -> "Nước giải khát"
        "LOCKER" -> "Tủ khóa"
        else -> amenity
    }
}

@Preview
@Composable
fun DetailInfoCourtPreview() {
    MaterialTheme {
        DetailInfoCourt(
            field = Field(
                fieldId = "1",
                ownerId = "owner123",
                name = "Sân bóng đá ABC",
                address = "123 Đường ABC, Quận 1, TP.HCM",
                geo = GeoLocation(lat = 10.7829, lng = 106.6992),
                sports = listOf("FOOTBALL", "BADMINTON"),
                images = com.trungkien.fbtp_cn.model.FieldImages(),
                slotMinutes = 30,
                openHours = OpenHours(start = "08:00", end = "22:00", open24h = false),
                amenities = listOf("PARKING", "SHOWER", "EQUIPMENT", "WIFI"),
                description = "Sân bóng đá mini chất lượng cao với đầy đủ tiện ích hiện đại, phù hợp cho các trận đấu giao hữu và tập luyện.",
                contactPhone = "0123456789",
                averageRating = 4.5f,
                totalReviews = 128,
                active = true,
                // ✅ FIX: Thêm footballFieldType cho preview
                footballFieldType = "5_PLAYERS"
            ),
            onEditClick = {},
            onBackClick = {}
        )
    }
}


