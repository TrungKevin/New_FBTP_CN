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
import kotlin.math.*
import kotlin.random.Random

enum class BallType {
    FOOTBALL, TENNIS, BADMINTON, PICKLEBALL
}

@Composable
fun FallingBall(
    modifier: Modifier = Modifier,
    ballType: BallType = BallType.FOOTBALL,
    size: Float = 20f,
    delay: Long = 0L,
    startX: Float = 0f // Starting X position
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fallingBall")
    
    // Random trajectory parameters
    val trajectoryAmplitude = remember { Random.nextFloat() * 200f + 100f }
    val trajectoryFrequency = remember { Random.nextFloat() * 0.01f + 0.005f }
    val windDirection = remember { if (Random.nextBoolean()) 1f else -1f }
    
    // Falling animation with delay
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Random.nextInt(5000, 8000),
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
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "time"
    )
    
    // Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Random.nextInt(2000, 4000),
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )
    
    // Bounce effect
    val bounce by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Delay effect
    LaunchedEffect(Unit) {
        delay(delay)
    }
    
    Canvas(modifier = modifier) {
        // Calculate complex trajectory
        val sineWave = sin(yOffset * trajectoryFrequency) * trajectoryAmplitude
        val windEffect = (yOffset / 1200f) * windDirection * 150f
        val centerX = startX + sineWave + windEffect
        val centerY = yOffset
        val ballSize = size * bounce
        
        // Draw ball based on type
        when (ballType) {
            BallType.FOOTBALL -> drawFootball(centerX, centerY, ballSize, rotation)
            BallType.TENNIS -> drawTennis(centerX, centerY, ballSize, rotation)
            BallType.BADMINTON -> drawBadminton(centerX, centerY, ballSize, rotation)
            BallType.PICKLEBALL -> drawPickleball(centerX, centerY, ballSize, rotation)
        }
    }
}

private fun DrawScope.drawFootball(x: Float, y: Float, size: Float, rotation: Float) {
    rotate(rotation, Offset(x, y)) {
        // Main ball (black and white pattern)
        drawCircle(
            color = Color.White,
            radius = size,
            center = Offset(x, y)
        )
        
        // Black pentagons pattern
        drawCircle(
            color = Color.Black,
            radius = size * 0.3f,
            center = Offset(x, y)
        )
        
        // Pentagon pattern
        for (i in 0..4) {
            val angle = (i * 72f + rotation) * PI / 180f
            val px = x + cos(angle).toFloat() * size * 0.6f
            val py = y + sin(angle).toFloat() * size * 0.6f
            drawCircle(
                color = Color.Black,
                radius = size * 0.15f,
                center = Offset(px, py)
            )
        }
    }
}

private fun DrawScope.drawTennis(x: Float, y: Float, size: Float, rotation: Float) {
    rotate(rotation, Offset(x, y)) {
        // Main ball (bright green/yellow)
        drawCircle(
            color = Color(0xFFCCFF00),
            radius = size,
            center = Offset(x, y)
        )
        
        // Tennis ball curved lines
        val path = Path()
        path.moveTo(x - size, y)
        path.quadraticTo(x, y - size * 0.5f, x + size, y)
        path.moveTo(x - size, y)
        path.quadraticTo(x, y + size * 0.5f, x + size, y)
        
        drawPath(
            path = path,
            color = Color.White
        )
    }
}

private fun DrawScope.drawBadminton(x: Float, y: Float, size: Float, rotation: Float) {
    rotate(rotation, Offset(x, y)) {
        // Shuttlecock body (white)
        drawCircle(
            color = Color.White,
            radius = size * 0.4f,
            center = Offset(x, y + size * 0.3f)
        )
        
        // Feathers (white lines)
        for (i in 0..7) {
            val angle = i * 45f * PI / 180f
            val startX = x + cos(angle).toFloat() * size * 0.4f
            val startY = y - size * 0.5f
            val endX = x + cos(angle).toFloat() * size * 0.8f
            val endY = y - size * 1.2f
            
            drawLine(
                color = Color.White,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
        
        // Cork base (brown)
        drawCircle(
            color = Color(0xFF8D6E63),
            radius = size * 0.3f,
            center = Offset(x, y + size * 0.4f)
        )
    }
}

private fun DrawScope.drawPickleball(x: Float, y: Float, size: Float, rotation: Float) {
    rotate(rotation, Offset(x, y)) {
        // Main ball (bright color)
        drawCircle(
            color = Color(0xFFFF6B35),
            radius = size,
            center = Offset(x, y)
        )
        
        // Holes pattern (like wiffle ball)
        val holes = 12
        for (i in 0 until holes) {
            val ring = i / 6
            val angleInRing = (i % 6) * 60f
            val angle = (angleInRing + rotation) * PI / 180f
            val radius = size * (0.3f + ring * 0.3f)
            val holeX = x + cos(angle).toFloat() * radius
            val holeY = y + sin(angle).toFloat() * radius
            
            drawCircle(
                color = Color.Black,
                radius = size * 0.08f,
                center = Offset(holeX, holeY)
            )
        }
    }
}

@Preview
@Composable
fun FallingBallPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Preview các loại bóng khác nhau
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FallingBall(
                modifier = Modifier.size(100.dp),
                ballType = BallType.FOOTBALL,
                size = 30f,
                startX = 50f
            )
            FallingBall(
                modifier = Modifier.size(100.dp),
                ballType = BallType.TENNIS,
                size = 30f,
                startX = 50f
            )
            FallingBall(
                modifier = Modifier.size(100.dp),
                ballType = BallType.BADMINTON,
                size = 30f,
                startX = 50f
            )
            FallingBall(
                modifier = Modifier.size(100.dp),
                ballType = BallType.PICKLEBALL,
                size = 30f,
                startX = 50f
            )
        }
    }
}
