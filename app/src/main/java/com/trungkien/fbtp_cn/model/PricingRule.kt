package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

/**
 * BẢNG GIÁ THEO THỜI LƯỢNG - Dùng để tính tiền sân khi khách chọn khe giờ
 * 
 * LIÊN KẾT QUAN TRỌNG:
 * - fieldId: Liên kết với bảng FIELDS (sân)
 * - dayType: Phân biệt giá ngày thường/cuối tuần/lễ để tính giá chính xác
 * - slots: Số khe giờ (ví dụ: 2 khe = 1 giờ)
 * - minutes: Thời gian mỗi khe (phút)
 * - price: Giá tiền cho thời gian này
 * - calcMode: Cách tính giá (làm tròn lên theo quy tắc hoặc tính tuyến tính)
 * 
 * CÁCH TÍNH TIỀN SÂN:
 * 1. Khách chọn khe giờ → xác định thời gian sử dụng
 * 2. Dựa vào ngày (WEEKDAY/WEEKEND) → chọn PricingRule tương ứng
 * 3. Tính số khe giờ cần thiết: (thời gian sử dụng) / (minutes * slots)
 * 4. Áp dụng calcMode để tính tổng tiền
 */
@Keep
data class PricingRule(
    val ruleId: String = "",                    // ID duy nhất của quy tắc giá
    val fieldId: String = "",                   // ID sân (liên kết với bảng FIELDS)
    val dayType: String = "",                   // Loại ngày: "WEEKDAY" | "WEEKEND" | "HOLIDAY"
    val slots: Int = 1,                        // Số khe giờ (ví dụ: 2 khe = 1 giờ)
    val minutes: Int = 30,                     // Thời gian mỗi khe (phút)
    val price: Long = 0L,                      // Giá tiền/30' trong khoảng  thời gian này (VNĐ)
    val calcMode: String = "CEIL_TO_RULE",     // Cách tính: "CEIL_TO_RULE" | "LINEAR"
    val effectiveFrom: Long? = null,           // Thời điểm có hiệu lực từ (timestamp)
    val effectiveTo: Long? = null,             // Thời điểm hết hiệu lực (timestamp)
    
    // Thông tin bổ sung
    val description: String = "",          // Mô tả quy tắc giá
    @PropertyName("active")
    val active: Boolean = true           // Trạng thái hoạt động
)
