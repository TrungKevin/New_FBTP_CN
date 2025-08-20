package com.trungkien.fbtp_cn.ui.components.animation

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import com.trungkien.fbtp_cn.R

@Composable
fun LogoBreathing(
    modifier: Modifier = Modifier,
    onLogoClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoBreathing")

    // Breathing scale effect
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Glow effect
    val glowAlpha by infiniteTransition.animateFloat( // chức năng: tạo hiệu ứng phát sáng cho logo
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Color breathing effect
    val colorTransition by infiniteTransition.animateColor(
        initialValue = Color(0xFF2E7D32), // GreenPrimary //
        targetValue = Color(0xFF388E3C), // GreenContainer
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut), // chức năng: tạo hiệu ứng chuyển màu từ xanh lá cây sang xanh đậm
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorBreathing"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .alpha(glowAlpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App image logo with breathing effect
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 260.dp, height = 180.dp)
                .clip(RoundedCornerShape(16.dp))
                .graphicsLayer {
                    shape = RoundedCornerShape(16.dp)
                    clip = true
                }
                .padding(bottom = 8.dp)
        )

        // Subtitle
        Text(
            text = "Football Badminton Tennis Pickleball",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575), // Gray
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Sports icons row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text("⚽", fontSize = 24.sp) // Football
            Text("🏸", fontSize = 24.sp) // Badminton (cầu lông)
            Text("🎾", fontSize = 24.sp) // Tennis
            Text("🏓", fontSize = 24.sp) // Pickleball
        }
    }
}

@Preview
@Composable
fun LogoBreathingPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LogoBreathing(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        )
    }
}
