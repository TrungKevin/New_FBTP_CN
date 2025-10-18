package com.trungkien.fbtp_cn.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.trungkien.fbtp_cn.model.*
import java.time.LocalDateTime
import java.time.LocalTime

object MockData {
    
    // Mock Users
    val mockUsers = listOf(
        User(
            userId = "user_001",
            role = "RENTER",
            name = "Nguyễn Văn A",
            email = "nguyenvana@gmail.com",
            phone = "0123456789",
            avatarUrl = "",
            address = "123 ABC Street, District 1, HCMC",
            dateOfBirth = "1990-01-01",
            gender = "MALE",
            isVerified = true,
            createdAt = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000),
            updatedAt = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
        ),
        User(
            userId = "owner_001",
            role = "OWNER",
            name = "Tennis Club HCMC",
            email = "tennisclub@gmail.com",
            phone = "0987654321",
            avatarUrl = "",
            address = "123 Tennis Street, District 1, HCMC",
            dateOfBirth = "",
            gender = "",
            isVerified = true,
            createdAt = System.currentTimeMillis() - (500 * 24 * 60 * 60 * 1000),
            updatedAt = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000)
        ),
        User(
            userId = "owner_002",
            role = "OWNER",
            name = "Badminton Center",
            email = "badmintoncenter@gmail.com",
            phone = "0555666777",
            avatarUrl = "",
            address = "456 Badminton Avenue, District 3, HCMC",
            dateOfBirth = "",
            gender = "",
            isVerified = true,
            createdAt = System.currentTimeMillis() - (400 * 24 * 60 * 60 * 1000),
            updatedAt = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000)
        )
    )
    
    // Mock Fields
    val mockFields = listOf(
        Field(
            fieldId = "field_001",
            ownerId = "owner_001",
            name = "Court 1 - Tennis",
            address = "123 Tennis Street, District 1, HCMC",
            geo = GeoLocation(
                lat = 10.7769,
                lng = 106.7009,
                geohash = "w3gx4"
            ),
            sports = listOf("TENNIS"),
            images = FieldImages(
                mainImage = "https://example.com/court1_main.jpg",
                image1 = "https://example.com/court1_1.jpg",
                image2 = "https://example.com/court1_2.jpg",
                image3 = "https://example.com/court1_3.jpg"
            ),
            slotMinutes = 30,
            openHours = OpenHours(
                start = "05:00",
                end = "23:00",
                open24h = false
            ),
            amenities = listOf("PARKING", "SHOWER", "EQUIPMENT", "LIGHTING"),
            description = "Sân tennis chất lượng cao với mặt sân cứng, có đèn chiếu sáng ban đêm",
            contactPhone = "0123456789",
            averageRating = 4.5f,
            totalReviews = 128,
            active = true
        ),
        Field(
            fieldId = "field_002",
            ownerId = "owner_002",
            name = "Court 2 - Badminton",
            address = "456 Badminton Avenue, District 3, HCMC",
            geo = GeoLocation(
                lat = 10.7829,
                lng = 106.6992,
                geohash = "w3gx5"
            ),
            sports = listOf("BADMINTON"),
            images = FieldImages(
                mainImage = "https://example.com/court2_main.jpg",
                image1 = "https://example.com/court2_1.jpg",
                image2 = "https://example.com/court2_2.jpg",
                image3 = "https://example.com/court2_3.jpg"
            ),
            slotMinutes = 30,
            openHours = OpenHours(
                start = "06:00",
                end = "22:00",
                open24h = false
            ),
            amenities = listOf("PARKING", "AIR_CONDITIONING", "EQUIPMENT"),
            description = "Sân cầu lông chuyên nghiệp với sàn gỗ cao cấp",
            contactPhone = "0987654321",
            averageRating = 4.2f,
            totalReviews = 89,
            active = true
        ),
        Field(
            fieldId = "field_003",
            ownerId = "owner_001",
            name = "Court 3 - Tennis Premium",
            address = "789 Premium Road, District 7, HCMC",
            geo = GeoLocation(
                lat = 10.7308,
                lng = 106.7263,
                geohash = "w3gx6"
            ),
            sports = listOf("TENNIS"),
            images = FieldImages(
                mainImage = "https://example.com/court3_main.jpg",
                image1 = "https://example.com/court3_1.jpg",
                image2 = "https://example.com/court3_2.jpg",
                image3 = "https://example.com/court3_3.jpg"
            ),
            slotMinutes = 30,
            openHours = OpenHours(
                start = "05:00",
                end = "24:00",
                open24h = false
            ),
            amenities = listOf("PREMIUM_PARKING", "LOCKER_ROOM", "PRO_SHOP", "RESTAURANT", "LIGHTING"),
            description = "Sân tennis cao cấp với mặt sân đất nện, có phòng thay đồ riêng biệt",
            contactPhone = "0555666777",
            averageRating = 4.8f,
            totalReviews = 256,
            active = true
        ),
        Field(
            fieldId = "field_004",
            ownerId = "owner_002",
            name = "Court 4 - Multi-Sport",
            address = "321 Sports Complex, District 2, HCMC",
            geo = GeoLocation(
                lat = 10.7870,
                lng = 106.7490,
                geohash = "w3gx7"
            ),
            sports = listOf("TENNIS", "BADMINTON", "PICKLEBALL"),
            images = FieldImages(
                mainImage = "https://example.com/court4_main.jpg",
                image1 = "https://example.com/court4_1.jpg",
                image2 = "https://example.com/court4_2.jpg",
                image3 = "https://example.com/court4_3.jpg"
            ),
            slotMinutes = 30,
            openHours = OpenHours(
                start = "06:00",
                end = "23:00",
                open24h = false
            ),
            amenities = listOf("PARKING", "EQUIPMENT", "CHANGING_ROOM"),
            description = "Sân đa năng có thể chuyển đổi giữa tennis, badminton và pickleball",
            contactPhone = "0333444555",
            averageRating = 4.0f,
            totalReviews = 67,
            active = true
        )
    )
    
    // Mock Services (Master Services)
    val mockServices = listOf(
        Service(
            serviceId = "service_001",
            name = "Thuê vợt tennis",
            defaultBillingType = "PER_UNIT",
            defaultAllowQuantity = true,
            description = "Vợt tennis chuyên nghiệp Wilson",
            category = "EQUIPMENT",
            active = true
        ),
        Service(
            serviceId = "service_002",
            name = "Thuê vợt cầu lông",
            defaultBillingType = "PER_UNIT",
            defaultAllowQuantity = true,
            description = "Vợt cầu lông Yonex chất lượng cao",
            category = "EQUIPMENT",
            active = true
        ),
        Service(
            serviceId = "service_003",
            name = "Hộp banh tennis",
            defaultBillingType = "PER_UNIT",
            defaultAllowQuantity = true,
            description = "Banh tennis Wilson chính hãng",
            category = "EQUIPMENT",
            active = true
        ),
        Service(
            serviceId = "service_004",
            name = "Nước suối",
            defaultBillingType = "PER_UNIT",
            defaultAllowQuantity = true,
            description = "Nước suối tinh khiết 500ml",
            category = "BEVERAGE",
            active = true
        ),
        Service(
            serviceId = "service_005",
            name = "Red Bull",
            defaultBillingType = "PER_UNIT",
            defaultAllowQuantity = true,
            description = "Nước tăng lực Red Bull 250ml",
            category = "BEVERAGE",
            active = true
        ),
        Service(
            serviceId = "service_006",
            name = "Hướng dẫn viên",
            defaultBillingType = "PER_HOUR",
            defaultAllowQuantity = false,
            description = "HLV chuyên nghiệp 1-1",
            category = "TRAINING",
            active = true
        )
    )
    
    // Mock Field Services (Services specific to each field)
    val mockFieldServices = listOf(
        FieldService(
            fieldServiceId = "fs_001",
            fieldId = "field_001",
            serviceId = "service_001",
            name = "Thuê vợt tennis",
            price = 20000,
            billingType = "PER_UNIT",
            allowQuantity = true,
            description = "Vợt tennis chuyên nghiệp Wilson",
            imageUrl = "https://example.com/racket.jpg",
            isAvailable = true,
            stockQuantity = 10
        ),
        FieldService(
            fieldServiceId = "fs_002",
            fieldId = "field_001",
            serviceId = "service_003",
            name = "Hộp banh tennis",
            price = 180000,
            billingType = "PER_UNIT",
            allowQuantity = true,
            description = "Banh tennis Wilson chính hãng",
            imageUrl = "https://example.com/tennis_balls.jpg",
            isAvailable = true,
            stockQuantity = 5
        ),
        FieldService(
            fieldServiceId = "fs_003",
            fieldId = "field_001",
            serviceId = "service_004",
            name = "Nước suối",
            price = 10000,
            billingType = "PER_UNIT",
            allowQuantity = true,
            description = "Nước suối tinh khiết 500ml",
            imageUrl = "https://example.com/water.jpg",
            isAvailable = true,
            stockQuantity = 50
        ),
        FieldService(
            fieldServiceId = "fs_004",
            fieldId = "field_001",
            serviceId = "service_005",
            name = "Red Bull",
            price = 25000,
            billingType = "PER_UNIT",
            allowQuantity = true,
            description = "Nước tăng lực Red Bull 250ml",
            imageUrl = "https://example.com/redbull.jpg",
            isAvailable = true,
            stockQuantity = 20
        ),
        FieldService(
            fieldServiceId = "fs_005",
            fieldId = "field_001",
            serviceId = "service_006",
            name = "Hướng dẫn viên",
            price = 300000,
            billingType = "PER_HOUR",
            allowQuantity = false,
            description = "HLV chuyên nghiệp 1-1",
            imageUrl = "https://example.com/coach.jpg",
            isAvailable = true
        ),
        FieldService(
            fieldServiceId = "fs_006",
            fieldId = "field_002",
            serviceId = "service_002",
            name = "Thuê vợt cầu lông",
            price = 15000,
            billingType = "PER_UNIT",
            allowQuantity = true,
            description = "Vợt cầu lông Yonex chất lượng cao",
            imageUrl = "https://example.com/badminton_racket.jpg",
            isAvailable = true,
            stockQuantity = 15
        )
    )
    
    // Mock Pricing Rules
    val mockPricingRules = listOf(
        PricingRule(
            ruleId = "rule_001",
            fieldId = "field_001",
            dayType = "WEEKDAY",
            slots = 2,
            minutes = 60,
            price = 120000,
            calcMode = "CEIL_TO_RULE",
            description = "Giá ngày thường - 1 giờ",
            active = true
        ),
        PricingRule(
            ruleId = "rule_002",
            fieldId = "field_001",
            dayType = "WEEKDAY",
            slots = 4,
            minutes = 120,
            price = 220000,
            calcMode = "CEIL_TO_RULE",
            description = "Giá ngày thường - 2 giờ",
            active = true
        ),
        PricingRule(
            ruleId = "rule_003",
            fieldId = "field_001",
            dayType = "WEEKEND",
            slots = 2,
            minutes = 60,
            price = 170000,
            calcMode = "CEIL_TO_RULE",
            description = "Giá cuối tuần - 1 giờ",
            active = true
        ),
        PricingRule(
            ruleId = "rule_004",
            fieldId = "field_002",
            dayType = "WEEKDAY",
            slots = 2,
            minutes = 60,
            price = 80000,
            calcMode = "CEIL_TO_RULE",
            description = "Giá ngày thường - 1 giờ",
            active = true
        ),
        PricingRule(
            ruleId = "rule_005",
            fieldId = "field_002",
            dayType = "WEEKEND",
            slots = 2,
            minutes = 60,
            price = 120000,
            calcMode = "CEIL_TO_RULE",
            description = "Giá cuối tuần - 1 giờ",
            active = true
        )
    )
    
    // Mock Slots
    val mockSlots = listOf(
        Slot(
            slotId = "slot_001",
            fieldId = "field_001",
            date = "2024-01-15",
            startAt = "06:00",
            endAt = "06:30",
            isAvailable = true,
            price = 60000,
            isBooked = false
        ),
        Slot(
            slotId = "slot_002",
            fieldId = "field_001",
            date = "2024-01-15",
            startAt = "06:30",
            endAt = "07:00",
            isAvailable = true,
            price = 60000,
            isBooked = false
        ),
        Slot(
            slotId = "slot_003",
            fieldId = "field_001",
            date = "2024-01-15",
            startAt = "07:00",
            endAt = "07:30",
            isAvailable = true,
            price = 60000,
            isBooked = false
        ),
        Slot(
            slotId = "slot_004",
            fieldId = "field_001",
            date = "2024-01-15",
            startAt = "18:00",
            endAt = "18:30",
            isAvailable = false,
            price = 85000,
            isBooked = true,
            bookingId = "booking_001"
        ),
        Slot(
            slotId = "slot_005",
            fieldId = "field_001",
            date = "2024-01-15",
            startAt = "18:30",
            endAt = "19:00",
            isAvailable = false,
            price = 85000,
            isBooked = true,
            bookingId = "booking_001"
        )
    )
    
    // Mock Matches
    val mockMatches = listOf(
        Match(
            rangeKey = "field_0012024011518001900",
            fieldId = "field_001",
            date = "2024-01-15",
            startAt = "18:00",
            endAt = "19:00",
            capacity = 2,
            occupiedCount = 1,
            participants = listOf(
                MatchParticipant(
                    bookingId = "booking_001",
                    renterId = "user_001",
                    side = "A"
                )
            ),
            price = 170000,
            totalPrice = 195000,
            status = "WAITING_OPPONENT",
            matchType = "SINGLE"
        )
    )
    
    // Mock Bookings
    val mockBookings = listOf(
        Booking(
            bookingId = "booking_001",
            renterId = "user_001",
            ownerId = "owner_001",
            fieldId = "field_001",
            date = "2024-01-15",
            startAt = "18:00",
            endAt = "19:00",
            slotsCount = 2,
            minutes = 60,
            matchId = "field_0012024011518001900",
            matchSide = "A",
            opponentMode = "WAITING_OPPONENT",
            basePrice = 170000,
            serviceLines = listOf(
                ServiceLine(
                    serviceId = "service_005",
                    name = "Red Bull",
                    billingType = "PER_UNIT",
                    price = 25000,
                    quantity = 1,
                    lineTotal = 25000
                )
            ),
            servicePrice = 25000,
            totalPrice = 195000,
            status = "PAID",
            notes = "Cần chuẩn bị sẵn nước uống",
            paymentStatus = "PAID",
            paymentMethod = "MOMO"
        ),
        Booking(
            bookingId = "booking_002",
            renterId = "user_001",
            ownerId = "owner_002",
            fieldId = "field_002",
            date = "2024-01-10",
            startAt = "20:00",
            endAt = "21:00",
            slotsCount = 2,
            minutes = 60,
            basePrice = 120000,
            serviceLines = emptyList(),
            servicePrice = 0,
            totalPrice = 120000,
            status = "DONE",
            notes = "",
            paymentStatus = "PAID",
            paymentMethod = "CASH"
        )
    )
    
    // Mock Reviews
    val mockReviews = listOf(
        Review(
            reviewId = "review_001",
            fieldId = "field_001",
            renterId = "user_001",
            renterName = "Nguyễn Văn A",
            renterAvatar = "",
            rating = 5,
            comment = "Sân tennis rất tốt! Mặt sân bằng phẳng, đèn chiếu sáng rõ ràng. Nhân viên phục vụ nhiệt tình. Sẽ quay lại đặt sân thường xuyên!",
            images = listOf("https://example.com/review1_1.jpg"),
            likes = 12,
            likedBy = listOf("user_002", "user_003"),
            shares = 3,
            reportCount = 0,
            replies = listOf(
                Reply(
                    replyId = "reply_001",
                    userId = "owner_001",
                    userName = "Tennis Club HCMC",
                    userAvatar = "",
                    userRole = "OWNER",
                    comment = "Cảm ơn bạn đã đánh giá tích cực! Chúng tôi luôn cố gắng mang đến trải nghiệm tốt nhất cho khách hàng.",
                    images = emptyList(),
                    likes = 5,
                    likedBy = listOf("user_001"),
                    owner = true
                )
            ),
            tags = listOf("CHẤT LƯỢNG", "DỊCH VỤ"),
            helpfulCount = 8,
            helpfulBy = listOf("user_002", "user_003", "user_004"),
            anonymous = false,
            status = "ACTIVE",
            verified = false
        ),
        Review(
            reviewId = "review_002",
            fieldId = "field_002",
            renterId = "user_001",
            renterName = "Nguyễn Văn A",
            renterAvatar = "",
            rating = 4,
            comment = "Sân cầu lông ổn, sàn gỗ tốt. Tuy nhiên cần cải thiện hệ thống điều hòa.",
            images = emptyList(),
            likes = 5,
            likedBy = listOf("user_002"),
            shares = 1,
            reportCount = 0,
            replies = listOf(
                Reply(
                    replyId = "reply_002",
                    userId = "owner_002",
                    userName = "Badminton Center",
                    userAvatar = "",
                    userRole = "OWNER",
                    comment = "Cảm ơn phản hồi của bạn. Chúng tôi sẽ kiểm tra và cải thiện hệ thống điều hòa.",
                    images = emptyList(),
                    likes = 3,
                    likedBy = listOf("user_001"),
                    owner = true
                )
            ),
            tags = listOf("CHẤT LƯỢNG", "GIÁ CẢ"),
            helpfulCount = 4,
            helpfulBy = listOf("user_002", "user_003"),
            anonymous = false,
            status = "ACTIVE",
            verified = false
        )
    )
    
    // Mock Notifications
    val mockNotifications = listOf(
        Notification(
            notificationId = "notif_001",
            toUserId = "user_001",
            type = "BOOKING_CREATED",
            title = "Đặt sân thành công",
            body = "Bạn đã đặt sân Court 1 - Tennis thành công cho ngày 15/01/2024",
            data = NotificationData(
                bookingId = "booking_001",
                fieldId = "field_001"
            ),
            read = false,
            channel = "IN_APP",
            priority = "NORMAL"
        ),
        Notification(
            notificationId = "notif_002",
            toUserId = "user_001",
            type = "OPPONENT_JOINED",
            title = "Có đối thủ tham gia",
            body = "Đã có người tham gia vào trận đấu của bạn",
            data = NotificationData(
                bookingId = "booking_001",
                fieldId = "field_001",
                matchId = "field_0012024011518001900"
            ),
            read = true,
            channel = "IN_APP",
            priority = "HIGH"
        )
    )
    
    // Mock User Devices
    val mockUserDevices = listOf(
        UserDevice(
            deviceId = "device_001",
            userId = "user_001",
            fcmToken = "fcm_token_user_001",
            platform = "ANDROID",
            lastSeenAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000),
            deviceModel = "Samsung Galaxy S21",
            appVersion = "1.0.0",
            active = true
        ),
        UserDevice(
            deviceId = "device_002",
            userId = "owner_001",
            fcmToken = "fcm_token_owner_001",
            platform = "ANDROID",
            lastSeenAt = System.currentTimeMillis() - (1 * 60 * 60 * 1000),
            deviceModel = "iPhone 13",
            appVersion = "1.0.0",
            active = true
        )
    )
    
    // Mock Public Price Board
    val mockPublicPriceBoard = listOf(
        PublicPriceBoard(
            boardId = "board_001",
            fieldId = "field_001",
            previewRules = listOf(
                PricePreviewRule(
                    slots = 2,
                    minutes = 60,
                    price = 120000
                ),
                PricePreviewRule(
                    slots = 4,
                    minutes = 120,
                    price = 220000
                )
            ),
            previewServices = listOf(
                PricePreviewService(
                    name = "Thuê vợt tennis",
                    price = 20000,
                    billingType = "PER_UNIT"
                ),
                PricePreviewService(
                    name = "Nước suối",
                    price = 10000,
                    billingType = "PER_UNIT"
                )
            )
        )
    )
    
    // Mock Payments
    val mockPayments = listOf(
        Payment(
            paymentId = "payment_001",
            bookingId = "booking_001",
            userId = "user_001",
            amount = 195000,
            currency = "VND",
            paymentMethod = "MOMO",
            status = "SUCCESS",
            transactionId = "momo_txn_001",
            paymentDate = System.currentTimeMillis() - (24 * 60 * 60 * 1000),
            gatewayResponse = "Success"
        ),
        Payment(
            paymentId = "payment_002",
            bookingId = "booking_002",
            userId = "user_001",
            amount = 120000,
            currency = "VND",
            paymentMethod = "CASH",
            status = "SUCCESS",
            paymentDate = System.currentTimeMillis() - (6 * 24 * 60 * 60 * 1000)
        )
    )
    
    // Mock Field Operating Schedule
    val mockFieldOperatingSchedule = listOf(
        FieldOperatingSchedule(
            scheduleId = "schedule_001",
            fieldId = "field_001",
            dayOfWeek = 1, // Chủ nhật
            isOpen = true,
            openTime = "06:00",
            closeTime = "23:00",
            isSpecialDay = false
        ),
        FieldOperatingSchedule(
            scheduleId = "schedule_002",
            fieldId = "field_001",
            dayOfWeek = 2, // Thứ 2
            isOpen = true,
            openTime = "05:00",
            closeTime = "23:00",
            isSpecialDay = false
        ),
        FieldOperatingSchedule(
            scheduleId = "schedule_003",
            fieldId = "field_001",
            dayOfWeek = 1, // Chủ nhật
            isOpen = true,
            openTime = "06:00",
            closeTime = "23:00",
            isSpecialDay = true,
            specialNote = "Ngày lễ - Giảm giá 20%"
        )
    )
    
    // Helper functions
    fun getUserById(userId: String): User? {
        return mockUsers.find { it.userId == userId }
    }
    
    fun getFieldById(fieldId: String): Field? {
        return mockFields.find { it.fieldId == fieldId }
    }
    
    fun getServicesByFieldId(fieldId: String): List<FieldService> {
        return mockFieldServices.filter { it.fieldId == fieldId }
    }
    
    fun getPricingRulesByFieldId(fieldId: String): List<PricingRule> {
        return mockPricingRules.filter { it.fieldId == fieldId }
    }
    
    fun getSlotsByFieldId(fieldId: String, date: String): List<Slot> {
        return mockSlots.filter { it.fieldId == fieldId && it.date == date }
    }
    
    fun getBookingsByUserId(userId: String): List<Booking> {
        return mockBookings.filter { it.renterId == userId }
    }
    
    fun getBookingsByOwnerId(ownerId: String): List<Booking> {
        return mockBookings.filter { it.ownerId == ownerId }
    }
    
    fun getReviewsByFieldId(fieldId: String): List<Review> {
        return mockReviews.filter { it.fieldId == fieldId }
    }
    
    fun getNotificationsByUserId(userId: String): List<Notification> {
        return mockNotifications.filter { it.toUserId == userId }
    }
    
    fun searchFields(query: String, sports: List<String>? = null, maxPrice: Int? = null): List<Field> {
        return mockFields.filter { field ->
            val matchesQuery = field.name.contains(query, ignoreCase = true) ||
                    field.address.contains(query, ignoreCase = true)
            val matchesSports = sports == null || sports.isEmpty() || 
                    sports.any { sport -> field.sports.contains(sport) }
            
            matchesQuery && matchesSports
        }
    }
}
