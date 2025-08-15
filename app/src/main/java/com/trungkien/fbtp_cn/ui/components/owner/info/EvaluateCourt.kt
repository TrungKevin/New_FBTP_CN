package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
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
fun EvaluateCourt(field: Field, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(18.dp)) {
        Text(
            text = "Đánh giá",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("⭐ 5.0 · Rất hài lòng về chất lượng sân")
                Spacer(modifier = Modifier.height(8.dp))
                Text("⭐ 4.5 · Nhân viên hỗ trợ tốt, sẽ quay lại")
            }
        }
    }
}


