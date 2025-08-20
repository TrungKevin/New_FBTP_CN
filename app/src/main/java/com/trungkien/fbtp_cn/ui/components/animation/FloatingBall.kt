package com.trungkien.fbtp_cn.ui.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FloatingBall(
    modifier: Modifier = Modifier,
    color: Color,
    size: Float = 20f,
    orbitRadius: Float = 150f,
    speed: Float = 1f,
    centerX: Float = 0f, // Custom center X position
    centerY: Float = 0f  // Custom center Y position
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floatingBall")
    
    // Random orbit parameters for more variety
    val randomOrbitRadius = remember { orbitRadius + Random.nextFloat() * 100f }
    val randomSpeed = remember { speed + Random.nextFloat() * 0.5f }
    val initialAngle = remember { Random.nextFloat() * 360f }
    
    // Orbital movement
    val angle by infiniteTransition.animateFloat(
        initialValue = initialAngle,
        targetValue = initialAngle + 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (10000 / randomSpeed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "orbit"
    )
    
    // Multi-layered floating effect
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Random.nextInt(1500, 2500),
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "verticalFloat"
    )
    
    // Secondary orbital motion (figure-8 pattern)
    val secondaryAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (15000 / randomSpeed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "secondaryOrbit"
    )
    
    // Size pulsing with random timing
    val ballSize by infiniteTransition.animateFloat(
        initialValue = size * 0.7f,
        targetValue = size * 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Random.nextInt(1200, 2000),
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sizePulse"
    )
    
    Canvas(modifier = modifier) {
        // Use custom center or default to screen center
        val orbitalCenterX = if (centerX != 0f) centerX else this.size.width / 2
        val orbitalCenterY = if (centerY != 0f) centerY else this.size.height / 2
        
        // Primary orbital motion
        val primaryX = cos(Math.toRadians(angle.toDouble())).toFloat() * randomOrbitRadius
        val primaryY = sin(Math.toRadians(angle.toDouble())).toFloat() * randomOrbitRadius

        // Secondary orbital motion (smaller radius, different speed)
        val secondaryX = cos(Math.toRadians(secondaryAngle.toDouble())).toFloat() * (randomOrbitRadius * 0.3f)
        val secondaryY = sin(Math.toRadians(secondaryAngle.toDouble() * 2)).toFloat() * (randomOrbitRadius * 0.2f)

        // Final position combining all movements
        val finalX = orbitalCenterX + primaryX + secondaryX
        val finalY = orbitalCenterY + primaryY + secondaryY + yOffset
        
        // Draw main ball
        drawCircle(
            color = color.copy(alpha = 0.8f),
            radius = ballSize,
            center = Offset(finalX, finalY)
        )
        
        // Add inner glow
        drawCircle(
            color = color.copy(alpha = 0.6f),
            radius = ballSize * 0.7f,
            center = Offset(finalX, finalY)
        )
        
        // Add outer glow effect
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = ballSize * 2f,
            center = Offset(finalX, finalY)
        )
        
        // Add sparkle effect
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = ballSize * 0.3f,
            center = Offset(finalX - ballSize * 0.3f, finalY - ballSize * 0.3f)
        )
    }
}

@Preview
@Composable
fun FloatingBallPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Preview multiple floating balls with different centers
        repeat(5) { index ->
            FloatingBall(
                modifier = Modifier.fillMaxSize(),
                color = when (index % 3) {
                    0 -> Color(0xFF6200EE) // Purple
                    1 -> Color(0xFF03DAC6) // Teal
                    else -> Color(0xFFFF6200) // Orange
                },
                size = 18f,
                orbitRadius = 80f + (index * 30f),
                speed = 0.8f + (index * 0.3f),
                centerX = (index * 150f) + 100f,
                centerY = (index * 100f) + 300f
            )
        }
    }
}
