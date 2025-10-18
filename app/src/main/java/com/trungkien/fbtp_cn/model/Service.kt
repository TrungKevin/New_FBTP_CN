package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

/**
 * DỊCH VỤ MASTER - Template cho các dịch vụ, dùng để tạo FieldService cho từng sân
 * 
 * LIÊN KẾT QUAN TRỌNG:
 * - serviceId: Được sử dụng trong FieldService.serviceId để liên kết
 * - defaultBillingType: Cách tính giá mặc định cho dịch vụ này
 * - defaultAllowQuantity: Cho phép chọn số lượng mặc định
 * - category: Phân loại dịch vụ (THIẾT BỊ, NƯỚC UỐNG, HUẤN LUYỆN)
 * 
 * CÁCH SỬ DỤNG:
 * 1. Owner tạo dịch vụ master trong bảng SERVICES
 * 2. Khi thêm dịch vụ cho sân → tạo FieldService với serviceId liên kết
 * 3. FieldService kế thừa các thuộc tính mặc định từ Service
 * 4. Owner có thể tùy chỉnh giá và thuộc tính khác cho từng sân
 */
@Keep
data class Service(
    val serviceId: String,                 // ID duy nhất của dịch vụ master
    val name: String,                      // Tên dịch vụ
    val defaultBillingType: String,        // Cách tính giá mặc định: "FLAT_PER_BOOKING" | "PER_HOUR" | "PER_SLOT" | "PER_MINUTE" | "PER_UNIT"
    val defaultAllowQuantity: Boolean,     // Cho phép chọn số lượng mặc định
    val description: String = "",          // Mô tả dịch vụ
    val category: String = "",             // Phân loại: ["EQUIPMENT", "BEVERAGE", "TRAINING"] (THIẾT BỊ, NƯỚC UỐNG, HUẤN LUYỆN)
    val active: Boolean = true           // Trạng thái hoạt động
)

@Keep
data class ServiceOrder(
    val id: String,
    val serviceId: String,
    val serviceName: String,
    val quantity: Int,
    val price: Int,
    val totalPrice: Int = quantity * price
)
