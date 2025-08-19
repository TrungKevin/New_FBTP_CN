package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

data class RenterReview(val user: String, val rating: Int, val comment: String)

@Composable
fun RenterReviewsSection(
    reviews: List<RenterReview>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Đánh giá", style = MaterialTheme.typography.titleMedium)
            reviews.forEach { r ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(text = r.user, fontWeight = FontWeight.SemiBold)
                        Text(text = "\"${r.comment}\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(r.rating) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun RenterReviewsSectionPreview() {
    FBTP_CNTheme {
        RenterReviewsSection(
            reviews = listOf(
                RenterReview("User A", 5, "Sân rất tốt!"),
                RenterReview("User B", 4, "Ổn áp")
            )
        )
    }
}


