package com.trungkien.fbtp_cn.ui.components.owner.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerMapHeader(
    field: Field,
    onBackClick: () -> Unit,
    onEditLocation: (() -> Unit)? = null,
    onResetLocation: (() -> Unit)? = null, // Callback để reset và geocoding lại
    onAdjustLocation: (() -> Unit)? = null, // Callback để điều chỉnh vị trí marker thủ công
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Vị trí sân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = field.name,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
                actions = {
                    // Nút reset và geocoding lại (luôn hiển thị khi có địa chỉ)
                    if (onResetLocation != null && field.address.isNotEmpty()) {
                        IconButton(onClick = onResetLocation) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Tìm lại vị trí từ địa chỉ",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Nút điều chỉnh vị trí marker thủ công (chỉ hiển thị khi có tọa độ)
                    if (onAdjustLocation != null && field.geo.lat != 0.0 && field.geo.lng != 0.0) {
                        IconButton(onClick = onAdjustLocation) {
                            Icon(
                                imageVector = Icons.Default.Adjust,
                                contentDescription = "Điều chỉnh vị trí marker",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    // Nút chỉnh sửa vị trí (chỉ hiển thị khi có tọa độ)
                    if (onEditLocation != null && field.geo.lat != 0.0 && field.geo.lng != 0.0) {
                        IconButton(onClick = onEditLocation) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa vị trí",
                                tint = GreenPrimary
                            )
                        }
                    }
                },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}
