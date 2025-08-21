package com.trungkien.fbtp_cn.ui.components.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.R

@Composable
fun CircleLogoSpinner(
    modifier: Modifier = Modifier,
    sizeDp: Int = 72,// kích thước của logo hình tròn, đơn vị là dp
    speedMs: Int = 1800// tốc độ xoay của logo, đơn vị là mili giây
) {
    val transition = rememberInfiniteTransition(label = "circleSpin") // chức năng: tạo hiệu ứng xoay cho logo hình tròn
    val angle = transition.animateFloat(// chức năng: tạo hiệu ứng xoay cho logo hình tròn
        initialValue = 0f,// chức năng: tạo hiệu ứng xoay cho logo hình tròn
        targetValue = 360f,// chức năng: tạo hiệu ứng xoay cho logo hình tròn
        animationSpec = infiniteRepeatable(// chức năng: tạo hiệu ứng xoay cho logo hình tròn
            tween(durationMillis = speedMs, easing = LinearEasing)
        ),
        label = "angle"
    )

    Image(// chức năng: tạo hiệu ứng xoay cho logo hình tròn
        painter = painterResource(id = R.drawable.circlelogo2),
        contentDescription = "circle logo spinner",
        modifier = modifier
            .size(sizeDp.dp)
            .rotate(angle.value)
    )
}


