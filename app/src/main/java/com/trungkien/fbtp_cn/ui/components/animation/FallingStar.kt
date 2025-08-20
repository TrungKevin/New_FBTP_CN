package com.trungkien.fbtp_cn.ui.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FallingStar(
    modifier: Modifier = Modifier,
    color: Color,
    size: Float = 15f,
    delay: Long = 0L,
    startX: Float = 0f // Starting X position
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fallingStar")
    
    // Random trajectory parameters
    val trajectoryAmplitude = remember { Random.nextFloat() * 150f + 80f }
    val trajectoryFrequency = remember { Random.nextFloat() * 0.008f + 0.003f }
    val windDirection = remember { if (Random.nextBoolean()) 1f else -1f }
    
    // Falling animation with delay
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Random.nextInt(4000, 7000),
                easing = EaseIn
            )
        ),
        label = "falling"
    )
    
    // Complex horizontal movement (sine wave + wind effect)
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
        ),
        label = "time"
    )
    
    // Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "rotation"
    )
    
    // Scale pulsing
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Twinkling effect
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    // Delay effect
    LaunchedEffect(Unit) {
        delay(delay)
    }
    
    Canvas(modifier = modifier) {
        // Calculate complex trajectory
        val sineWave = sin(yOffset * trajectoryFrequency) * trajectoryAmplitude
        val windEffect = (yOffset / 1200f) * windDirection * 120f
        val centerX = startX + sineWave + windEffect
        val centerY = yOffset
        val scaledSize = size * scale
        
        // Draw star with rotation
        rotate(rotation, Offset(centerX, centerY)) {
            drawStar(
                color = color.copy(alpha = twinkle),
                center = Offset(centerX, centerY),
                size = scaledSize
            )
            
            // Glow effect
            drawStar(
                color = color.copy(alpha = twinkle * 0.4f),
                center = Offset(centerX, centerY),
                size = scaledSize * 1.8f
            )
            
            // Sparkle effect
            drawStar(
                color = color.copy(alpha = twinkle * 0.6f),
                center = Offset(centerX, centerY),
                size = scaledSize * 0.6f
            )
        }
    }
}

private fun DrawScope.drawStar(color: Color, center: Offset, size: Float) {
    val path = Path()
    val outerRadius = size
    val innerRadius = size * 0.4f
    val points = 5
    
    for (i in 0 until points * 2) {
        val angle = (i * Math.PI / points).toFloat()
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    
    drawPath(path, color)
}

@Preview
@Composable
fun FallingStarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // Preview ngôi sao rơi với quỹ đạo phức tạp
        repeat(5) { index -> // chức năng: tạo nhiều ngôi sao rơi với các vị trí bắt đầu khác nhau
            FallingStar(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFFD700),
                size = 15f,
                delay = index * 200L,
                startX = (index * 100f) + 50f
            )
        }
    }
}
