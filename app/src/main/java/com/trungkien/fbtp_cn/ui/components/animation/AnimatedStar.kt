package com.trungkien.fbtp_cn.ui.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedStar( // chức năng: tạo hiệu ứng ngôi sao lấp lánh và xoay tròn
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFD700),
    size: Float = 20f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "animatedStar")
    
    // Twinkling effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Random.nextInt(1000, 3000),
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    // Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Random.nextInt(5000, 10000),
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )
    
    // Scale pulsing
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Canvas(modifier = modifier) {
        val centerX = this.size.width / 2
        val centerY = this.size.height / 2
        val scaledSize = size * scale
        
        // Draw star
        rotate(rotation, Offset(centerX, centerY)) {
            drawStar(
                color = color.copy(alpha = alpha),
                center = Offset(centerX, centerY),
                size = scaledSize
            )
            
            // Glow effect
            drawStar(
                color = color.copy(alpha = alpha * 0.3f),
                center = Offset(centerX, centerY),
                size = scaledSize * 1.5f
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
fun AnimatedStarPreview() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(16.dp)
    ) {
        AnimatedStar(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFFD700),
            size = 20f
        )
    }
}
