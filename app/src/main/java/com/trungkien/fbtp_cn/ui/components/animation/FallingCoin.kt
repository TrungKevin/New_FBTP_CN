package com.trungkien.fbtp_cn.ui.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun FallingCoin( // chức năng: tạo hiệu ứng đồng xu rơi xuống với các hiệu ứng xoay, lắc và phát sáng
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFD700),
    size: Float = 15f // Kích thước đồng xu, có thể điều chỉnh
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fallingCoin") // Tạo một chuyển động vô hạn cho đồng xu
    
    // Falling animation
    val yOffset by infiniteTransition.animateFloat( // chức năng: tạo hiệu ứng rơi xuống của đồng xu
        initialValue = -100f, // Vị trí ban đầu của đồng xu
        targetValue = 1000f,// Vị trí cuối cùng của đồng xu
        animationSpec = infiniteRepeatable(// chức năng: lặp lại hiệu ứng rơi xuống vô hạn
            animation = tween( // chức năng: tạo hiệu ứng rơi xuống với thời gian ngẫu nhiên
                durationMillis = Random.nextInt(3000, 6000), // Thời gian rơi xuống ngẫu nhiên từ 3 đến 6 giây
                easing = LinearEasing// chức năng: sử dụng easing tuyến tính để tạo hiệu ứng rơi xuống mượt mà
            )
        ),
        label = "falling" // Nhãn để xác định hiệu ứng này trong quá trình gỡ lỗi
    )
    
    // Horizontal swaying
    val xOffset by infiniteTransition.animateFloat( // chức năng: tạo hiệu ứng lắc ngang đồng xu
        initialValue = -20f, // Vị trí ban đầu của đồng xu
        targetValue = 20f,// Vị trí cuối cùng của đồng xu
        animationSpec = infiniteRepeatable( // chức năng: lặp lại hiệu ứng lắc ngang vô hạn
            animation = tween(2000, easing = EaseInOut), // chức năng: tạo hiệu ứng lắc ngang với thời gian ngẫu nhiên
            repeatMode = RepeatMode.Reverse
        ),
        label = "swaying"
    )
    
    // Rotation
    val rotation by infiniteTransition.animateFloat( // chức năng: tạo hiệu ứng xoay đồng xu
        initialValue = 0f,// Vị trí ban đầu của đồng xu
        targetValue = 360f, // Vị trí cuối cùng của đồng xu
        animationSpec = infiniteRepeatable( // chức năng: lặp lại hiệu ứng xoay vô hạn
            animation = tween(2000, easing = LinearEasing) // chức năng: tạo hiệu ứng xoay với thời gian ngẫu nhiên
        ),
        label = "rotation"
    )
    
    // Glow effect
    val glowAlpha by infiniteTransition.animateFloat( // chức năng: tạo hiệu ứng phát sáng cho đồng xu
        initialValue = 0.3f, // Độ mờ ban đầu của hiệu ứng phát sáng
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(//  chức năng: lặp lại hiệu ứng phát sáng vô hạn
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Canvas(modifier = modifier) { // chức năng: vẽ đồng xu trên canvas
        val centerX = this.size.width / 2 + xOffset// Tính toán vị trí trung tâm theo chiều ngang, cộng với độ lệch ngang
        val centerY = yOffset // Tính toán vị trí trung tâm theo chiều dọc, sử dụng giá trị yOffset đã tính toán
        
        // Draw coin with rotation
        rotate(rotation) { // chức năng: xoay đồng xu theo góc đã tính toán
            // Main coin
            drawCircle( // chức năng: vẽ đồng xu chính
                color = color,
                radius = size,
                center = Offset(centerX, centerY)
            )
            
            // Coin border
            drawCircle( // chức năng: vẽ viền đồng xu
                color = color.copy(alpha = 0.8f),
                radius = size * 0.9f,
                center = Offset(centerX, centerY)
            )
            
            // Inner circle (coin face)
            drawCircle( // chức năng: vẽ mặt đồng xu
                color = color.copy(alpha = 0.6f),
                radius = size * 0.7f,
                center = Offset(centerX, centerY)
            )
            
            // Glow effect
            drawCircle( // chức năng: vẽ hiệu ứng phát sáng xung quanh đồng xu
                color = color.copy(alpha = glowAlpha * 0.3f),
                radius = size * 1.5f,
                center = Offset(centerX, centerY)
            )
        }
    }
}

@Preview
@Composable
fun FallingCoinPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FallingCoin(
            modifier = Modifier.size(50.dp),
            color = Color(0xFFFFD700),
            size = 15f
        )
    }
}
