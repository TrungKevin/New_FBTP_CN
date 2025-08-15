package com.trungkien.fbtp_cn.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shadow elevation constants for consistency between preview and runtime
 * This ensures that shadows appear the same in both preview and actual device
 */
object ShadowElevation {
    val None: Dp = 0.dp
    val Small: Dp = 2.dp
    val Medium: Dp = 4.dp
    val Large: Dp = 8.dp
    val ExtraLarge: Dp = 16.dp // là của Dialog, thể hiện các hộp thoại lớn
}

/**
 * Common shadow elevation values used throughout the app
 */
object CommonShadows {
    val Card = ShadowElevation.None          // 0.dp (giảm từ 2.dp xuống 0.dp)
    val Button = ShadowElevation.Medium     // 4.dp
    val Badge = ShadowElevation.None        // 0.dp (giảm từ 2.dp xuống 0.dp)
    val NavigationBar = ShadowElevation.Large // 8.dp
    val BarElevation = ShadowElevation.Large  // 8.dp
    val ItemElevation = ShadowElevation.Medium // 4.dp
    val FloatingActionButton = ShadowElevation.Medium // 4.dp
    val Dialog = ShadowElevation.ExtraLarge   // 16.dp
}
