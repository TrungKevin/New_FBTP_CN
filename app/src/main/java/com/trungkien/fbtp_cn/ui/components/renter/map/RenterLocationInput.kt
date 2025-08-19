package com.trungkien.fbtp_cn.ui.components.renter.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary

@Composable
fun RenterLocationInput(
    currentAddress: String,
    onAddressChange: (String) -> Unit,
    onGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1f), // Đặt zIndex để đảm bảo nó nằm trên các thành phần khác
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp //  dùng để tạo hiệu ứng đổ bóng
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), //
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        Text(
            text = "📍 Vị trí hiện tại:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GPS Button
            IconButton(
                onClick = onGpsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = GreenPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Vị trí hiện tại",
                    tint = GreenPrimary
                )
            }

            // Address Input Field
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                BasicTextField(
                    value = currentAddress,
                    onValueChange = onAddressChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    decorationBox = { innerTextField ->
                        if (currentAddress.isEmpty()) {
                            Text(
                                text = "Nhập địa chỉ...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}
}
@Preview
@Composable
 fun RenterLocationInputPreview() {
    FBTP_CNTheme {
        RenterLocationInput(
            currentAddress = "123 ABC Street, District 1, HCMC",
            onAddressChange = {},
            onGpsClick = {}
        )
    }
}

