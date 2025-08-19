package com.trungkien.fbtp_cn.ui.components.renter.profile

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

data class MyReview(
	val fieldName: String,
	val rating: Int,
	val comment: String
)

@Composable
fun ProfileReviewsCard(
	reviews: List<MyReview>,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(16.dp),
		color = MaterialTheme.colorScheme.surface,
		shadowElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Đánh giá của tôi",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface
			)

			reviews.forEach { review ->
				Surface(
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(12.dp),
					color = MaterialTheme.colorScheme.surface,
					shadowElevation = 1.dp
				) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = Icons.Default.Star,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(8.dp))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = review.fieldName,
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.SemiBold
							)
							Text(
								text = "⭐${review.rating} \"${review.comment}\"",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
			}
		}
	}
}

@Preview
@Composable
private fun ProfileReviewsCardPreview() {
	FBTP_CNTheme {
		ProfileReviewsCard(
			reviews = listOf(
				MyReview("Court 1", 5, "Sân rất tốt!"),
				MyReview("Court 2", 4, "Sân ổn")
			)
		)
	}
}


