package com.trungkien.fbtp_cn.ui.components.renter.reviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.trungkien.fbtp_cn.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.EvaluateCourtViewModel
import com.trungkien.fbtp_cn.viewmodel.EvaluateCourtEvent
import com.trungkien.fbtp_cn.viewmodel.EvaluateCourtState
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.model.Review
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.components.renter.reviews.RenterReviewCard
import com.trungkien.fbtp_cn.ui.components.owner.info.ReviewSummary
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun RenterReviewsSection(
    fieldId: String,
    modifier: Modifier = Modifier,
    evaluateVm: EvaluateCourtViewModel = viewModel(),
    authVm: AuthViewModel = viewModel()
) {
    val uiState by evaluateVm.uiState.collectAsState()
    val currentUser = authVm.currentUser.collectAsState().value

    LaunchedEffect(fieldId) {
        evaluateVm.handleEvent(EvaluateCourtEvent.LoadReviews(fieldId))
        evaluateVm.handleEvent(EvaluateCourtEvent.LoadReviewSummary(fieldId))
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Đánh giá", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Tổng quan đánh giá (giống Owner)
        ReviewSummary(summary = uiState.reviewSummary)
        Divider()

        // Reviews list (bên ngoài card)
        uiState.reviews.forEach { r ->
            RenterReviewCard(
                review = r,
                currentUserId = currentUser?.userId,
                onEdit = { reviewId, newRating, newComment ->
                    evaluateVm.handleEvent(
                        EvaluateCourtEvent.UpdateReview(
                            reviewId = reviewId,
                            updates = mapOf(
                                "rating" to newRating,
                                "comment" to newComment
                            )
                        )
                    )
                },
                onDelete = { reviewId ->
                    evaluateVm.handleEvent(EvaluateCourtEvent.DeleteReview(reviewId))
                }
            )
            Divider()
        }

        // Input dưới cùng trong Card gọn
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewInputRow(
                    onSubmit = { stars, comment ->
                        if (currentUser != null && stars in 1..5 && comment.isNotBlank()) {
                            val review = Review(
                                fieldId = fieldId,
                                renterId = currentUser.userId,
                                renterName = currentUser.name,
                                renterAvatar = currentUser.avatarUrl,
                                rating = stars,
                                comment = comment.trim()
                            )
                            evaluateVm.handleEvent(EvaluateCourtEvent.AddReview(review))
                            focusManager.clearFocus()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ReviewInputRow(onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                val starIndex = index + 1
                IconButton(onClick = { rating = starIndex }) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (starIndex <= rating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { onSubmit(rating, comment); comment = "" }) { Text("Gửi đánh giá") }
        }
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Chia sẻ cảm nhận của bạn...") }
        )
    }
}

// Moved to separate card component

@Preview
@Composable
private fun RenterReviewsSectionPreview() {
    FBTP_CNTheme {
        RenterReviewsSection(fieldId = "demo")
    }
}


