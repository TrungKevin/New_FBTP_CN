package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

/**
 * DỊCH VỤ CỦA TỪNG SÂN - Dùng để tính tiền dịch vụ bổ sung khi khách đặt sân
 * 
 * LIÊN KẾT QUAN TRỌNG:
 * - fieldId: Liên kết với bảng FIELDS (sân)
 * - serviceId: Liên kết với bảng SERVICES (dịch vụ master) - optional
 * - billingType: Cách tính giá dịch vụ (theo đặt sân, theo giờ, theo khe, theo phút, theo đơn vị)
 * - allowQuantity: Cho phép khách chọn số lượng
 * - stockQuantity: Số lượng tồn kho (nếu có)
 * 
 * CÁCH TÍNH TIỀN DỊCH VỤ:
 * 1. Khách chọn dịch vụ → xác định loại dịch vụ và số lượng
 * 2. Dựa vào billingType → tính giá:
 *    - FLAT_PER_BOOKING: Giá cố định cho mỗi lần đặt sân
 *    - PER_HOUR: Giá theo giờ sử dụng sân
 *    - PER_SLOT: Giá theo khe giờ
 *    - PER_MINUTE: Giá theo phút sử dụng
 *    - PER_UNIT: Giá theo đơn vị (ví dụ: 1 quả bóng, 1 chai nước)
 * 3. Nhân với số lượng (nếu allowQuantity = true)
 */
@Keep
data class FieldService(
    val fieldServiceId: String = "",            // ID duy nhất của dịch vụ sân
    val fieldId: String = "",                   // ID sân (liên kết với bảng FIELDS)
    val serviceId: String? = null,              // ID dịch vụ master (liên kết với bảng SERVICES) - optional
    val name: String = "",                      // Tên dịch vụ
    val price: Long = 0L,                       // Giá dịch vụ (VNĐ)
    val billingType: String = "PER_UNIT",       // Cách tính giá: "FLAT_PER_BOOKING" | "PER_HOUR" | "PER_SLOT" | "PER_MINUTE" | "PER_UNIT"
    val allowQuantity: Boolean = true,          // Cho phép khách chọn số lượng
    val description: String = "",               // Mô tả dịch vụ
    val imageUrl: String = "",                  // URL hình ảnh dịch vụ
    @PropertyName("available")
    val isAvailable: Boolean = true,            // Trạng thái khả dụng
    val stockQuantity: Int? = null              // Số lượng tồn kho (nếu có)
)
