package com.trungkien.fbtp_cn.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.trungkien.fbtp_cn.model.*
import java.time.LocalDateTime
import java.time.LocalTime

object MockData {
    
    // Mock User (Renter)
    val mockUser = User(
        id = "user_001",
        name = "Nguyễn Văn A",
        email = "nguyenvana@email.com",
        phone = "0123456789",
        avatar = "",
        address = "123 ABC Street, District 1, HCMC",
        totalBookings = 15,
        totalReviews = 8,
        averageRating = 4.2f
    )
    
    // Mock Fields
    val mockFields = listOf(
        Field(
            id = "field_001",
            name = "Court 1 - Tennis",
            type = "Tennis",
            price = 120000,
            imageUrl = "https://example.com/court1.jpg",
            status = "Available",
            isAvailable = true,
            address = "123 Tennis Street, District 1, HCMC",
            operatingHours = "5:00 - 23:00",
            contactPhone = "0123456789",
            distance = "2.5km",
            latitude = 10.7769,
            longitude = 106.7009,
            averageRating = 4.5f,
            totalReviews = 128,
            description = "Sân tennis chất lượng cao với mặt sân cứng, có đèn chiếu sáng ban đêm",
            facilities = listOf("Parking", "Shower", "Equipment", "Lighting"),
            images = listOf(
                "https://example.com/court1_1.jpg",
                "https://example.com/court1_2.jpg",
                "https://example.com/court1_3.jpg"
            ),
            ownerId = "owner_001",
            ownerName = "Tennis Club HCMC",
            priceRange = PriceRange(
                weekday = PriceDetail(morning = 120000, afternoon = 120000, evening = 170000),
                weekend = PriceDetail(morning = 120000, afternoon = 120000, evening = 170000)
            ),
            isFavorite = false
        ),
        Field(
            id = "field_002",
            name = "Court 2 - Badminton",
            type = "Badminton",
            price = 80000,
            imageUrl = "https://example.com/court2.jpg",
            status = "Available",
            isAvailable = true,
            address = "456 Badminton Avenue, District 3, HCMC",
            operatingHours = "6:00 - 22:00",
            contactPhone = "0987654321",
            distance = "3.2km",
            latitude = 10.7829,
            longitude = 106.6992,
            averageRating = 4.2f,
            totalReviews = 89,
            description = "Sân cầu lông chuyên nghiệp với sàn gỗ cao cấp",
            facilities = listOf("Parking", "Air Conditioning", "Equipment"),
            images = listOf(
                "https://example.com/court2_1.jpg",
                "https://example.com/court2_2.jpg"
            ),
            ownerId = "owner_002",
            ownerName = "Badminton Center",
            priceRange = PriceRange(
                weekday = PriceDetail(morning = 80000, afternoon = 80000, evening = 120000),
                weekend = PriceDetail(morning = 80000, afternoon = 80000, evening = 120000)
            ),
            isFavorite = true
        ),
        Field(
            id = "field_003",
            name = "Court 3 - Tennis Premium",
            type = "Tennis",
            price = 200000,
            imageUrl = "https://example.com/court3.jpg",
            status = "Available",
            isAvailable = true,
            address = "789 Premium Road, District 7, HCMC",
            operatingHours = "5:00 - 24:00",
            contactPhone = "0555666777",
            distance = "5.8km",
            latitude = 10.7308,
            longitude = 106.7263,
            averageRating = 4.8f,
            totalReviews = 256,
            description = "Sân tennis cao cấp với mặt sân đất nện, có phòng thay đồ riêng biệt",
            facilities = listOf("Premium Parking", "Locker Room", "Pro Shop", "Restaurant", "Lighting"),
            images = listOf(
                "https://example.com/court3_1.jpg",
                "https://example.com/court3_2.jpg",
                "https://example.com/court3_3.jpg",
                "https://example.com/court3_4.jpg"
            ),
            ownerId = "owner_003",
            ownerName = "Premium Tennis Club",
            priceRange = PriceRange(
                weekday = PriceDetail(morning = 200000, afternoon = 200000, evening = 250000),
                weekend = PriceDetail(morning = 200000, afternoon = 200000, evening = 250000)
            ),
            isFavorite = false
        ),
        Field(
            id = "field_004",
            name = "Court 4 - Multi-Sport",
            type = "Multi-Sport",
            price = 150000,
            imageUrl = "https://example.com/court4.jpg",
            status = "Available",
            isAvailable = true,
            address = "321 Sports Complex, District 2, HCMC",
            operatingHours = "6:00 - 23:00",
            contactPhone = "0333444555",
            distance = "4.1km",
            latitude = 10.7870,
            longitude = 106.7490,
            averageRating = 4.0f,
            totalReviews = 67,
            description = "Sân đa năng có thể chuyển đổi giữa tennis, badminton và bóng bàn",
            facilities = listOf("Parking", "Equipment", "Changing Room"),
            images = listOf(
                "https://example.com/court4_1.jpg",
                "https://example.com/court4_2.jpg"
            ),
            ownerId = "owner_004",
            ownerName = "Multi-Sport Complex",
            priceRange = PriceRange(
                weekday = PriceDetail(morning = 150000, afternoon = 150000, evening = 180000),
                weekend = PriceDetail(morning = 150000, afternoon = 150000, evening = 180000)
            ),
            isFavorite = false
        )
    )
    
    // Mock Services
    val mockServices = listOf(
        Service(
            id = "service_001",
            name = "Thuê vợt tennis",
            description = "Vợt tennis chuyên nghiệp Wilson",
            price = 20000,
            unit = "Cái",
            category = "Equipment",
            isAvailable = true,
            imageUrl = "https://example.com/racket.jpg",
            fieldId = "field_001"
        ),
        Service(
            id = "service_002",
            name = "Thuê vợt cầu lông",
            description = "Vợt cầu lông Yonex chất lượng cao",
            price = 15000,
            unit = "Cái",
            category = "Equipment",
            isAvailable = true,
            imageUrl = "https://example.com/badminton_racket.jpg",
            fieldId = "field_002"
        ),
        Service(
            id = "service_003",
            name = "Hộp banh tennis",
            description = "Banh tennis Wilson chính hãng",
            price = 180000,
            unit = "Hộp",
            category = "Equipment",
            isAvailable = true,
            imageUrl = "https://example.com/tennis_balls.jpg",
            fieldId = "field_001"
        ),
        Service(
            id = "service_004",
            name = "Nước suối",
            description = "Nước suối tinh khiết 500ml",
            price = 10000,
            unit = "Chai",
            category = "Beverage",
            isAvailable = true,
            imageUrl = "https://example.com/water.jpg",
            fieldId = "field_001"
        ),
        Service(
            id = "service_005",
            name = "Red Bull",
            description = "Nước tăng lực Red Bull 250ml",
            price = 25000,
            unit = "Chai",
            category = "Beverage",
            isAvailable = true,
            imageUrl = "https://example.com/redbull.jpg",
            fieldId = "field_001"
        ),
        Service(
            id = "service_006",
            name = "Hướng dẫn viên",
            description = "HLV chuyên nghiệp 1-1",
            price = 300000,
            unit = "Giờ",
            category = "Training",
            isAvailable = true,
            imageUrl = "https://example.com/coach.jpg",
            fieldId = "field_001"
        )
    )
    
    // Mock Time Slots
    @RequiresApi(Build.VERSION_CODES.O)
    val mockTimeSlots = listOf(
        TimeSlot(
            id = "slot_001",
            fieldId = "field_001",
            startTime = LocalTime.of(6, 0),
            endTime = LocalTime.of(7, 0),
            isAvailable = true,
            price = 120000,
            isBooked = false
        ),
        TimeSlot(
            id = "slot_002",
            fieldId = "field_001",
            startTime = LocalTime.of(7, 0),
            endTime = LocalTime.of(8, 0),
            isAvailable = true,
            price = 120000,
            isBooked = false
        ),
        TimeSlot(
            id = "slot_003",
            fieldId = "field_001",
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(9, 0),
            isAvailable = true,
            price = 120000,
            isBooked = false
        ),
        TimeSlot(
            id = "slot_004",
            fieldId = "field_001",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            isAvailable = true,
            price = 120000,
            isBooked = false
        ),
        TimeSlot(
            id = "slot_005",
            fieldId = "field_001",
            startTime = LocalTime.of(17, 0),
            endTime = LocalTime.of(18, 0),
            isAvailable = true,
            price = 170000,
            isBooked = false
        ),
        TimeSlot(
            id = "slot_006",
            fieldId = "field_001",
            startTime = LocalTime.of(18, 0),
            endTime = LocalTime.of(19, 0),
            isAvailable = false,
            price = 170000,
            isBooked = true,
            bookingId = "booking_001"
        ),
        TimeSlot(
            id = "slot_007",
            fieldId = "field_001",
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(20, 0),
            isAvailable = true,
            price = 170000,
            isBooked = false
        )
    )
    
    // Mock Bookings
    @RequiresApi(Build.VERSION_CODES.O)
    val mockBookings = listOf(
        Booking(
            id = "booking_001",
            fieldId = "field_001",
            fieldName = "Court 1 - Tennis",
            timeRange = "18:00 - 19:00",
            status = "Confirmed",
            userId = "user_001",
            userName = "Nguyễn Văn A",
            date = "2024-01-15",
            startTime = "18:00",
            endTime = "19:00",
            totalPrice = 195000,
            fieldPrice = 170000,
            servicesPrice = 25000,
            services = listOf(
                ServiceOrder(
                    id = "order_001",
                    serviceId = "service_005",
                    serviceName = "Red Bull",
                    quantity = 1,
                    price = 25000
                )
            ),
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now().minusDays(1),
            notes = "Cần chuẩn bị sẵn nước uống",
            fieldImage = "https://example.com/court1.jpg",
            fieldAddress = "123 Tennis Street, District 1, HCMC",
            fieldType = "Tennis"
        ),
        Booking(
            id = "booking_002",
            fieldId = "field_002",
            fieldName = "Court 2 - Badminton",
            timeRange = "20:00 - 21:00",
            status = "Completed",
            userId = "user_001",
            userName = "Nguyễn Văn A",
            date = "2024-01-10",
            startTime = "20:00",
            endTime = "21:00",
            totalPrice = 120000,
            fieldPrice = 120000,
            servicesPrice = 0,
            services = emptyList(),
            createdAt = LocalDateTime.now().minusDays(6),
            updatedAt = LocalDateTime.now().minusDays(6),
            notes = "",
            fieldImage = "https://example.com/court2.jpg",
            fieldAddress = "456 Badminton Avenue, District 3, HCMC",
            fieldType = "Badminton"
        )
    )
    
    // Mock Reviews
    @RequiresApi(Build.VERSION_CODES.O)
    val mockReviews = listOf(
        Review(
            id = "review_001",
            fieldId = "field_001",
            fieldName = "Court 1 - Tennis",
            userId = "user_001",
            userName = "Nguyễn Văn A",
            userAvatar = "",
            rating = 5.0f,
            comment = "Sân tennis rất tốt! Mặt sân bằng phẳng, đèn chiếu sáng rõ ràng. Nhân viên phục vụ nhiệt tình. Sẽ quay lại đặt sân thường xuyên!",
            createdAt = LocalDateTime.now().minusDays(2),
            images = listOf("https://example.com/review1_1.jpg"),
            likes = 12,
            replies = listOf(
                Reply(
                    id = "reply_001",
                    userId = "owner_001",
                    userName = "Tennis Club HCMC",
                    userAvatar = "",
                    comment = "Cảm ơn bạn đã đánh giá tích cực! Chúng tôi luôn cố gắng mang đến trải nghiệm tốt nhất cho khách hàng.",
                    createdAt = LocalDateTime.now().minusDays(1),
                    isOwner = true
                )
            )
        ),
        Review(
            id = "review_002",
            fieldId = "field_002",
            fieldName = "Court 2 - Badminton",
            userId = "user_001",
            userName = "Nguyễn Văn A",
            userAvatar = "",
            rating = 4.0f,
            comment = "Sân cầu lông ổn, sàn gỗ tốt. Tuy nhiên cần cải thiện hệ thống điều hòa.",
            createdAt = LocalDateTime.now().minusDays(7),
            images = emptyList(),
            likes = 5,
            replies = listOf(
                Reply(
                    id = "reply_002",
                    userId = "owner_002",
                    userName = "Badminton Center",
                    userAvatar = "",
                    comment = "Cảm ơn phản hồi của bạn. Chúng tôi sẽ kiểm tra và cải thiện hệ thống điều hòa.",
                    createdAt = LocalDateTime.now().minusDays(6),
                    isOwner = true
                )
            )
        )
    )
    
    // Helper functions
    fun getFieldById(id: String): Field? {
        return mockFields.find { it.id == id }
    }
    
    fun getServicesByFieldId(fieldId: String): List<Service> {
        return mockServices.filter { it.fieldId == fieldId }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeSlotsByFieldId(fieldId: String): List<TimeSlot> {
        return mockTimeSlots.filter { it.fieldId == fieldId }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun getBookingsByUserId(userId: String): List<Booking> {
        return mockBookings.filter { it.userId == userId }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun getReviewsByFieldId(fieldId: String): List<Review> {
        return mockReviews.filter { it.fieldId == fieldId }
    }
    
    fun searchFields(query: String, type: String? = null, maxPrice: Int? = null): List<Field> {
        return mockFields.filter { field ->
            val matchesQuery = field.name.contains(query, ignoreCase = true) ||
                    field.address.contains(query, ignoreCase = true)
            val matchesType = type == null || field.type == type
            val matchesPrice = maxPrice == null || field.price <= maxPrice
            
            matchesQuery && matchesType && matchesPrice
        }
    }
}
