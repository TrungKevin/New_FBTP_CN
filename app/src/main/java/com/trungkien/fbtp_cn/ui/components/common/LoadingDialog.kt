package com.trungkien.fbtp_cn.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingDialog(
    message: String = "Loading...",
    onDismiss: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            dismissOnBackPress = onDismiss != null,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFC7F5C5)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SportsOrbitLoader(modifier = Modifier.size(80.dp))
                Text(
                    text = message,
                    color = Color(0xFF1B5E20), // xanh đậm để tương phản với nền #C7F5C5
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SportsOrbitLoader(
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp = 28.dp
) {
    val transition = rememberInfiniteTransition(label = "orbit")
    val angle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1600, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "angle"
    )

    val emojis = listOf("⚽", "🏸", "🎾", "🏓") //

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        emojis.forEachIndexed { index, emoji ->
            val theta = Math.toRadians((angle.value + index * 90f).toDouble())
            val x = (radius * cos(theta).toFloat())
            val y = (radius * sin(theta).toFloat())
            Text(
                text = emoji,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(x = x, y = y)
            )
        }
    }
}
@Preview
@Composable
fun LoadingDialogPreview() {
    LoadingDialog(message = "Đang tải dữ liệu...")
}


