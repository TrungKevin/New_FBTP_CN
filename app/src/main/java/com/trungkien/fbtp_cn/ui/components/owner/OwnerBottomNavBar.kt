package com.trungkien.fbtp_cn.ui.components.owner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.theme.CommonShadows

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OwnerBottomNavBar(
    currentScreen: OwnerNavScreen,
    onTabSelected: (OwnerNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val barHeight = 88.dp
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Bottom)
            .background(Color.Transparent)
            .navigationBarsPadding()
    ) {
        // Enhanced glass-morphism background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    shadowElevation = 24.dp.toPx()
                },
            color = Color.White.copy(alpha = 0.98f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 16.dp
        ) {
            // Gradient overlay for depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent,
                                Color(0xFFE8F5E8).copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            // Enhanced top glow effect
            Box(//  vẽ đường viền trên cùng của navigation bar
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        brush = Brush
                            .horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF), // bắt đầu từ trong suốt
                                Color(0xFFFFFFFF), // màu xanh lá nhạt
                                Color(0xFFFFFFFF),
                                Color(0xFFFFFFFF),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
            )
        }

        // Navigation items with enhanced spacing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OwnerNavScreen.values().forEach { item ->
                val selected = currentScreen == item

                // Enhanced animation values
                val offsetY by animateDpAsState(
                    targetValue = if (selected) (-8).dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "offsetY"
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "iconScale"
                )

                val backgroundScale by animateFloatAsState(// hiệu ứng phóng to của background
                    targetValue = if (selected) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "backgroundScale"
                )

                val backgroundAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    ),
                    label = "backgroundAlpha"
                )

                val glowIntensity by animateFloatAsState( // hiệu ứng glow của icon
                    targetValue = if (selected) 0.6f else 0f,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    ),
                    label = "glowIntensity"
                )

                NavItem( // là dùng để vẽ item của navigation bar
                    item = item,
                    selected = selected,
                    offsetY = offsetY,
                    iconScale = iconScale,
                    backgroundScale = backgroundScale,
                    backgroundAlpha = backgroundAlpha,
                    glowIntensity = glowIntensity,
                    onClick = {
                        if (!selected) { // chỉ thực hiện khi item chưa được chọn ,hiệu ứng nổi
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(item)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun NavItem(
    item: OwnerNavScreen,
    selected: Boolean,
    offsetY: androidx.compose.ui.unit.Dp,
    iconScale: Float,
    backgroundScale: Float,
    backgroundAlpha: Float,
    glowIntensity: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .width(72.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Enhanced icon container with glow effect
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .offset(y = offsetY)
        ) {
            // Outer glow effect
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer {
                            alpha = glowIntensity * 0.4f // độ mờ của glow
                            scaleX = backgroundScale * 1.1f // phóng to nhẹ
                            scaleY = backgroundScale * 1.1f // phóng to nhẹ
                        }
                        .background(// hiệu ứng glow của icon
                            brush = Brush // vẽ gradient cho glow, glow là hiệu ứng sáng xung quanh icon
                                .radialGradient(
                                colors = listOf(
                                    Color(0xFF00C853).copy(alpha = 0.3f),
                                    Color(0xFF00E676).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            // Enhanced background circle với gradient đẹp hơn
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) togetherWith scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                },
                label = "background_anim"
            ) { isSelected ->
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .graphicsLayer {
                                scaleX = backgroundScale
                                scaleY = backgroundScale
                                alpha = backgroundAlpha
                            }
                    ) {
                        Box( // dùng để tạo hiệu ứng inner shadow của icon
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf( // màu này của background của icon
                                            Color(0xFFAAE8AC),
                                            Color(0xFFAAE8AC),
                                            Color(0xFFAAE8AC),
                                            Color(0xFFAAE8AC)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .drawBehind {// vẽ inner shadow của icon
                                    // Inner shadow effect
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.6f),
                                                Color.Transparent
                                            ),
                                            radius = size.minDimension * 0.3f
                                        ),
                                        radius = size.minDimension * 0.5f,
                                        center = Offset(size.width * 0.3f, size.height * 0.3f)
                                    )
                                }
                        )
                    }
                }
            }

            // Enhanced icon with better animations
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = {
                        (fadeIn(
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + scaleIn(
                            initialScale = 0.85f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )) togetherWith (fadeOut(
                            animationSpec = tween(150)
                        ) + scaleOut(
                            targetScale = 1.1f,
                            animationSpec = tween(150)
                        ))
                    },
                    label = "icon_anim"
                ) { isSelected ->
                    Icon(
                        painter = painterResource(id = getIconForNavItem(item)),
                        contentDescription = getTitleForNavItem(item),
                        tint = if (isSelected)
                                    Color(0xFF00C853)
                                else
                                    Color(0xFF6B6B6B),// màu icon
                        modifier = Modifier
                            .size(if (isSelected) 20.dp else 18.dp)
                            .graphicsLayer {
                                // Add subtle glow to selected icon
                                if (isSelected) {
                                    alpha = 1f
                                } else {
                                    alpha = 0.85f
                                }
                            }
                    )
                }
            }
        }

        // Enhanced label với typography và animation tối ưu
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = 100,
                        easing = FastOutSlowInEasing
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(180)
                )
            },
            label = "text_anim"
        ) { isSelected ->
            Text(
                text = getTitleForNavItem(item),
                fontSize = if (isSelected) 10.sp else 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                            Color(0xFF00C853)
                        else
                            Color(0xFF3A543C), // màu chữ
                maxLines = 1,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = if (isSelected) 1f else 0.75f
                        scaleX = if (isSelected) 1f else 0.95f
                        scaleY = if (isSelected) 1f else 0.95f
                    }
                    .wrapContentHeight(),
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
private fun getIconForNavItem(item: OwnerNavScreen): Int {
    return when (item) {
        OwnerNavScreen.Home -> R.drawable.menu
        OwnerNavScreen.Booking -> R.drawable.event
        OwnerNavScreen.Field -> R.drawable.stadium
        OwnerNavScreen.Stats -> R.drawable.bartchar
        OwnerNavScreen.Profile -> R.drawable.hoso
    }
}

@Composable
private fun getTitleForNavItem(item: OwnerNavScreen): String {
    return when (item) {
        OwnerNavScreen.Home -> "Trang chủ"
        OwnerNavScreen.Booking -> "Đặt sân"
        OwnerNavScreen.Field -> "Quản lý sân"
        OwnerNavScreen.Stats -> "Thống kê"
        OwnerNavScreen.Profile -> "Hồ sơ"
    }
}

enum class OwnerNavScreen {
    Home,
    Booking,
    Field,
    Stats,
    Profile
}

@Preview
@Composable
fun OwnerBottomAppBarPreview() {
    FBTP_CNTheme {
        OwnerBottomNavBar(
            currentScreen = OwnerNavScreen.Home,
            onTabSelected = {}
        )
    }
}
