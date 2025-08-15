package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Field

@Composable
fun CourtService(field: Field, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(18.dp)) {
        Text(
            text = "Dịch vụ",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("• Thuê dụng cụ (vợt/banh)")
                Text("• Nước uống & khăn lạnh")
                Text("• Hướng dẫn viên/HLV theo giờ")
            }
        }
    }
}


