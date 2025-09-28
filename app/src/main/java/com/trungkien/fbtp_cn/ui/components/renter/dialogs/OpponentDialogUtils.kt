package com.trungkien.fbtp_cn.ui.components.renter.dialogs

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Utility functions for opponent matching dialogs
 */
object OpponentDialogUtils {
    
    /**
     * Hiển thị toast khi renter chọn lại khung giờ đã đặt của chính mình
     */
    fun showOwnSlotToast(context: Context) {
        Toast.makeText(context, "Khung giờ này bạn đã đặt", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Hiển thị toast khi khung giờ đã được đặt hoàn toàn
     */
    fun showSlotBookedToast(context: Context) {
        Toast.makeText(context, "Khung giờ này đã được đặt", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Hiển thị toast khi đặt lịch thành công
     */
    fun showBookingSuccessToast(context: Context) {
        Toast.makeText(context, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Hiển thị toast khi có lỗi
     */
    fun showErrorToast(context: Context, error: String) {
        Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Composable wrapper để sử dụng toast trong Compose
 */
@Composable
fun rememberOpponentDialogUtils(context: Context): OpponentDialogUtils {
    return remember { OpponentDialogUtils }
}
