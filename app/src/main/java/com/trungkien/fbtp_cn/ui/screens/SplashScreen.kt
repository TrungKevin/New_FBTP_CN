package com.trungkien.fbtp_cn.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.components.animation.AnimatedStar
import com.trungkien.fbtp_cn.ui.components.animation.BallType
import com.trungkien.fbtp_cn.ui.components.animation.FloatingBall
import com.trungkien.fbtp_cn.ui.components.animation.FallingCoin
import com.trungkien.fbtp_cn.ui.components.animation.FallingBall
import com.trungkien.fbtp_cn.ui.components.animation.FallingStar
import com.trungkien.fbtp_cn.ui.components.animation.LogoBreathing
import com.trungkien.fbtp_cn.ui.components.animation.CircleLogoSpinner
import com.trungkien.fbtp_cn.ui.theme.*
import kotlin.random.Random

@Composable
fun SplashScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onCustomerServiceClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    onComputerVersionClick: () -> Unit = {}
) {
    // Background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Background,
            Color(0xFFF0F8F0), // Light green tint
            Background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Animated stars background (lấp lánh)
        repeat(20) { index ->
            AnimatedStar(
                modifier = Modifier
                    .offset(
                        x = (index * 50).dp,
                        y = (index * 30).dp
                    )
                    .size(8.dp),
                color = Color(0xFFFFD700),
                size = 8f
            )
        }

        // Floating balls tản ra toàn màn hình với các tâm xoay khác nhau
        repeat(10) { index ->
            FloatingBall(
                modifier = Modifier.fillMaxSize(),
                color = when (index % 4) {
                    0 -> GreenPrimary
                    1 -> OrangeSecondary
                    2 -> BlueTertiary
                    else -> OrangeAccent
                },
                size = Random.nextFloat() * 8f + 12f, // 12-20f
                orbitRadius = Random.nextFloat() * 120f + 80f, // 80-200f
                speed = Random.nextFloat() * 1f + 0.5f, // 0.5-1.5f
                centerX = Random.nextFloat() * 600f + 100f, // Tâm X ngẫu nhiên
                centerY = Random.nextFloat() * 800f + 200f  // Tâm Y ngẫu nhiên
            )
        }

        // Falling coins (đồng xu rơi)
        repeat(12) { index ->
            FallingCoin(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFFD700),
                size = 12f
            )
        }

        // Thêm hiệu ứng các loại bóng rơi tản ra toàn màn hình
        repeat(8) { index ->
            FallingBall(
                modifier = Modifier.fillMaxSize(),
                ballType = when (index % 4) {
                    0 -> BallType.FOOTBALL
                    1 -> BallType.TENNIS
                    2 -> BallType.BADMINTON
                    else -> BallType.PICKLEBALL
                },
                size = Random.nextFloat() * 10f + 15f,
                delay = index * 400L,
                startX = Random.nextFloat() * 800f // Vị trí khởi đầu ngẫu nhiên trên trục X
            )
        }

        // Thêm hiệu ứng ngôi sao rơi tản ra toàn màn hình
        repeat(10) { index ->
            FallingStar(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFFD700),
                size = Random.nextFloat() * 8f + 8f, // 8-16f
                delay = index * 250L,
                startX = Random.nextFloat() * 800f // Vị trí khởi đầu ngẫu nhiên trên trục X
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo breathing + circle spinner overlay
            Box(contentAlignment = Alignment.Center) {
                LogoBreathing(
                    modifier = Modifier.padding(bottom = 40.dp),
                    onLogoClick = { /* Logo click */ }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Circle spinner ở giữa khoảng trắng
            CircleLogoSpinner(modifier = Modifier, sizeDp = 64, speedMs = 1600)

            // Thêm hiệu ứng trái bóng và ngôi sao rơi từ trên xuống
            Spacer(modifier = Modifier.height(40.dp))

            Spacer(modifier = Modifier.weight(1f))

            // Main action buttons
            Column( // chức năng: tạo các nút chính cho đăng nhập và đăng ký
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Click để đăng nhập",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Click để đăng ký",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Separator
            Text(
                text = "Or",
                color = Color(0xFF263238).copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Utility icons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                UtilityIcon(
                    label = "Listens",
                    iconEmoji = "🎧",
                    onClick = onCustomerServiceClick
                )
                UtilityIcon(
                    iconResId = R.drawable.googleicon,
                    label = "Google",
                    onClick = onGoogleLoginClick
                )
                UtilityIcon(
                    label = "Computer",
                    iconEmoji = "💻",
                    onClick = onComputerVersionClick
                )
            }

            // Preview page button
            OutlinedButton(
                onClick = { /* Preview page */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GreenPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GreenPrimary, OrangeSecondary)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Wellcome to FBTP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun UtilityIcon(
    label: String,
    onClick: () -> Unit,
    iconResId: Int? = null,
    iconEmoji: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        when {
            iconResId != null -> {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = label,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(bottom = 4.dp)
                )
            }
            iconEmoji != null -> {
                Text(
                    text = iconEmoji,
                    fontSize = 24.sp
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF263238).copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(
        onLoginClick = { /* Login action */ },
        onRegisterClick = { /* Register action */ },
        onCustomerServiceClick = { /* Customer service action */ },
        onGoogleLoginClick = { /* Google login action */ },
        onComputerVersionClick = { /* Computer version action */ }
    )
}
