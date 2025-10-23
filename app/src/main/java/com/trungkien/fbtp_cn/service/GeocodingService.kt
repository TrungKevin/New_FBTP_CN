package com.trungkien.fbtp_cn.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Service để chuyển đổi địa chỉ thành tọa độ GPS sử dụng Nominatim API
 */
class GeocodingService {
    private val client = OkHttpClient()
    private val baseUrl = "https://nominatim.openstreetmap.org/search"
    
    /**
     * Chuyển đổi địa chỉ thành tọa độ GPS với độ chính xác cao
     * @param address Địa chỉ cần chuyển đổi
     * @return GeoLocation với lat, lng hoặc null nếu không tìm thấy
     */
    suspend fun geocodeAddress(address: String): com.trungkien.fbtp_cn.model.GeoLocation? {
        return withContext(Dispatchers.IO) {
            try {
                
                
                // Chuẩn hóa địa chỉ trước khi geocoding
                val normalizedAddress = normalizeAddress(address)
                
                // Thử nhiều cách geocoding để có kết quả chính xác nhất
                val attempts = generateGeocodingAttempts(normalizedAddress)
                
                for ((index, attempt) in attempts.withIndex()) {
                    
                    val result = performGeocodingRequest(attempt)
                    if (result != null) {
                        // Kiểm tra độ chính xác của kết quả
                        if (isAccurateResult(result, normalizedAddress)) {
                            return@withContext result
                        } else {
                        }
                    }
                    
                    // Delay giữa các lần thử để tránh rate limit
                    kotlinx.coroutines.delay(500)
                }
                
                
                return@withContext null
                
            } catch (e: Exception) {
                
                return@withContext null
            }
        }
    }
    
    /**
     * Chuẩn hóa địa chỉ để tăng độ chính xác
     */
    private fun normalizeAddress(address: String): String {
        return address.trim()
            .replace(Regex("\\s+"), " ") // Loại bỏ khoảng trắng thừa
            .replace("Đường", "Đ.")
            .replace("Phường", "P.")
            .replace("Quận", "Q.")
            .replace("Thành phố", "TP.")
            .replace("Tỉnh", "T.")
            .replace("Huyện", "H.")
            .replace("Xã", "X.")
            .replace("Thị xã", "TX.")
            .replace("Thị trấn", "TT.")
    }
    
    /**
     * Tạo danh sách các cách thử geocoding khác nhau
     */
    private fun generateGeocodingAttempts(normalizedAddress: String): List<String> {
        val attempts = mutableListOf<String>()
        
        // 1. Địa chỉ gốc đã chuẩn hóa
        attempts.add(normalizedAddress)
        
        // 2. Thêm quốc gia
        attempts.add("$normalizedAddress, Việt Nam")
        
        // 3. Thử không dấu cho các từ khóa quan trọng
        val noAccentAddress = removeVietnameseAccents(normalizedAddress)
        attempts.add(noAccentAddress)
        
        // 4. Địa chỉ đơn giản hóa với từ khóa chính
        attempts.add(extractKeyAddressComponents(normalizedAddress))
        
        // 5. Thử với tên đường và phường cụ thể
        attempts.add(buildSpecificAddress(normalizedAddress))
        
        // 6. Thử với mã bưu điện nếu có thể
        val postalCode = extractPostalCode(normalizedAddress)
        if (postalCode.isNotEmpty()) {
            attempts.add("$normalizedAddress, $postalCode")
        }
        
        // 7. Thử với địa chỉ cụ thể có số nhà (ưu tiên cao)
        val specificAddressWithNumber = buildSpecificAddressWithNumber(normalizedAddress)
        if (specificAddressWithNumber.isNotEmpty()) {
            attempts.add(specificAddressWithNumber)
            attempts.add("$specificAddressWithNumber, Vietnam")
        }
        
        // 7. Thử cụ thể cho từng tỉnh/thành phố
        val province = extractProvince(normalizedAddress)
        if (province.isNotEmpty()) {
            // Thử với tên tỉnh không dấu
            val noAccentProvince = removeVietnameseAccents(province)
            attempts.add("${extractStreetAndNumber(normalizedAddress)}, ${extractWard(normalizedAddress)}, $noAccentProvince, Vietnam")
            
            // Thử với tên tỉnh có dấu
            attempts.add("${extractStreetAndNumber(normalizedAddress)}, ${extractWard(normalizedAddress)}, $province, Vietnam")
            
            // Thử với mã bưu điện của tỉnh
            val provincePostalCode = getProvincePostalCode(province)
            if (provincePostalCode.isNotEmpty()) {
                attempts.add("${extractStreetAndNumber(normalizedAddress)}, ${extractWard(normalizedAddress)}, $province, $provincePostalCode, Vietnam")
            }
        }
        
        // 8. Thử với các biến thể tên đường phổ biến
        val streetVariants = generateStreetVariants(normalizedAddress)
        attempts.addAll(streetVariants)
        
        return attempts.filter { it.isNotBlank() && it != normalizedAddress }.distinct()
    }
    
    /**
     * Loại bỏ dấu tiếng Việt
     */
    private fun removeVietnameseAccents(text: String): String {
        val vietnameseChars = mapOf(
            "à" to "a", "á" to "a", "ạ" to "a", "ả" to "a", "ã" to "a",
            "â" to "a", "ầ" to "a", "ấ" to "a", "ậ" to "a", "ẩ" to "a", "ẫ" to "a",
            "ă" to "a", "ằ" to "a", "ắ" to "a", "ặ" to "a", "ẳ" to "a", "ẵ" to "a",
            "è" to "e", "é" to "e", "ẹ" to "e", "ẻ" to "e", "ẽ" to "e",
            "ê" to "e", "ề" to "e", "ế" to "e", "ệ" to "e", "ể" to "e", "ễ" to "e",
            "ì" to "i", "í" to "i", "ị" to "i", "ỉ" to "i", "ĩ" to "i",
            "ò" to "o", "ó" to "o", "ọ" to "o", "ỏ" to "o", "õ" to "o",
            "ô" to "o", "ồ" to "o", "ố" to "o", "ộ" to "o", "ổ" to "o", "ỗ" to "o",
            "ơ" to "o", "ờ" to "o", "ớ" to "o", "ợ" to "o", "ở" to "o", "ỡ" to "o",
            "ù" to "u", "ú" to "u", "ụ" to "u", "ủ" to "u", "ũ" to "u",
            "ư" to "u", "ừ" to "u", "ứ" to "u", "ự" to "u", "ử" to "u", "ữ" to "u",
            "ỳ" to "y", "ý" to "y", "ỵ" to "y", "ỷ" to "y", "ỹ" to "y",
            "đ" to "d"
        )
        
        var result = text
        for ((vietnamese, latin) in vietnameseChars) {
            result = result.replace(vietnamese, latin)
            result = result.replace(vietnamese.uppercase(), latin.uppercase())
        }
        return result
    }
    
    /**
     * Trích xuất tỉnh/thành phố từ địa chỉ
     */
    private fun extractProvince(address: String): String {
        val provincePatterns = listOf(
            Regex("(TP\\.|Thành phố)\\s*([A-Za-zÀ-ỹ\\s]+)"),
            Regex("(T\\.|Tỉnh)\\s*([A-Za-zÀ-ỹ\\s]+)"),
            Regex("(Q\\.|Quận)\\s*([A-Za-zÀ-ỹ\\s]+)"),
            Regex("(H\\.|Huyện)\\s*([A-Za-zÀ-ỹ\\s]+)")
        )
        
        for (pattern in provincePatterns) {
            val match = pattern.find(address)
            if (match != null) {
                return match.groupValues[2].trim()
            }
        }
        
        // Tìm các tỉnh/thành phố phổ biến
        val commonProvinces = listOf(
            "Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
            "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
            "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
            "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông",
            "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang",
            "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình",
            "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
            "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định",
            "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên",
            "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị",
            "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên",
            "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang",
            "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
        )
        
        for (province in commonProvinces) {
            if (address.contains(province, ignoreCase = true)) {
                return province
            }
        }
        
        return ""
    }
    
    /**
     * Trích xuất phường/xã từ địa chỉ
     */
    private fun extractWard(address: String): String {
        val wardPatterns = listOf(
            Regex("(P\\.|Phường)\\s*([A-Za-zÀ-ỹ\\s]+)"),
            Regex("(X\\.|Xã)\\s*([A-Za-zÀ-ỹ\\s]+)"),
            Regex("(TT\\.|Thị trấn)\\s*([A-Za-zÀ-ỹ\\s]+)")
        )
        
        for (pattern in wardPatterns) {
            val match = pattern.find(address)
            if (match != null) {
                return match.groupValues[2].trim()
            }
        }
        
        return ""
    }
    
    /**
     * Trích xuất mã bưu điện từ địa chỉ
     */
    private fun extractPostalCode(address: String): String {
        val postalMatch = Regex("(\\d{5,6})").find(address)
        return postalMatch?.value ?: ""
    }
    
    /**
     * Lấy mã bưu điện của tỉnh
     */
    private fun getProvincePostalCode(province: String): String {
        val postalCodes = mapOf(
            "Hồ Chí Minh" to "700000",
            "Hà Nội" to "100000",
            "Đà Nẵng" to "500000",
            "Hải Phòng" to "180000",
            "Cần Thơ" to "940000",
            "An Giang" to "880000",
            "Bà Rịa - Vũng Tàu" to "790000",
            "Bắc Giang" to "220000",
            "Bắc Kạn" to "230000",
            "Bạc Liêu" to "960000",
            "Bắc Ninh" to "160000",
            "Bến Tre" to "860000",
            "Bình Định" to "590000",
            "Bình Dương" to "750000",
            "Bình Phước" to "770000",
            "Bình Thuận" to "800000",
            "Cà Mau" to "970000",
            "Cao Bằng" to "270000",
            "Đắk Lắk" to "630000",
            "Đắk Nông" to "640000",
            "Điện Biên" to "320000",
            "Đồng Nai" to "760000",
            "Đồng Tháp" to "870000",
            "Gia Lai" to "600000",
            "Hà Giang" to "310000",
            "Hà Nam" to "400000",
            "Hà Tĩnh" to "480000",
            "Hải Dương" to "170000",
            "Hậu Giang" to "950000",
            "Hòa Bình" to "350000",
            "Hưng Yên" to "160000",
            "Khánh Hòa" to "650000",
            "Kiên Giang" to "920000",
            "Kon Tum" to "580000",
            "Lai Châu" to "390000",
            "Lâm Đồng" to "670000",
            "Lạng Sơn" to "240000",
            "Lào Cai" to "330000",
            "Long An" to "850000",
            "Nam Định" to "420000",
            "Nghệ An" to "460000",
            "Ninh Bình" to "430000",
            "Ninh Thuận" to "660000",
            "Phú Thọ" to "290000",
            "Phú Yên" to "620000",
            "Quảng Bình" to "510000",
            "Quảng Nam" to "560000",
            "Quảng Ngãi" to "570000",
            "Quảng Ninh" to "200000",
            "Quảng Trị" to "530000",
            "Sóc Trăng" to "960000",
            "Sơn La" to "360000",
            "Tây Ninh" to "840000",
            "Thái Bình" to "410000",
            "Thái Nguyên" to "250000",
            "Thanh Hóa" to "440000",
            "Thừa Thiên Huế" to "530000",
            "Tiền Giang" to "860000",
            "Trà Vinh" to "870000",
            "Tuyên Quang" to "300000",
            "Vĩnh Long" to "890000",
            "Vĩnh Phúc" to "280000",
            "Yên Bái" to "380000"
        )
        
        return postalCodes[province] ?: ""
    }
    
    /**
     * Tạo các biến thể tên đường
     */
    private fun generateStreetVariants(address: String): List<String> {
        val variants = mutableListOf<String>()
        val streetNumber = extractStreetAndNumber(address)
        
        if (streetNumber.isNotEmpty()) {
            // Thử với "Đường" thay vì "Đ."
            variants.add(address.replace("Đ.", "Đường"))
            
            // Thử với "Đ." thay vì "Đường"
            variants.add(address.replace("Đường", "Đ."))
            
            // Thử với "Street" (tiếng Anh)
            variants.add(address.replace("Đ.", "Street").replace("Đường", "Street"))
        }
        
        return variants
    }
    
    /**
     * Trích xuất số nhà từ địa chỉ
     */
    private fun extractHouseNumber(address: String): String {
        // Tìm pattern số nhà phổ biến
        val patterns = listOf(
            Regex("(\\d+\\s*[A-Za-z]?\\s*[A-Za-zÀ-ỹ\\s]+)"), // 135/2 Đình Phong Phú
            Regex("(\\d+\\s*[A-Za-z]?\\s*[A-Za-zÀ-ỹ\\s]+)"), // 135 Đình Phong Phú
            Regex("(\\d+\\s*[A-Za-z]?\\s*[A-Za-zÀ-ỹ\\s]+)")  // 135A Đình Phong Phú
        )
        
        for (pattern in patterns) {
            val match = pattern.find(address)
            if (match != null) {
                val result = match.value.trim()
                // Trích xuất chỉ số nhà
                val numberMatch = Regex("(\\d+\\s*[A-Za-z]?)").find(result)
                if (numberMatch != null) {
                    return numberMatch.value.trim()
                }
            }
        }
        
        return ""
    }
    
    /**
     * Trích xuất số nhà và tên đường từ địa chỉ
     */
    private fun extractStreetAndNumber(address: String): String {
        // Tìm pattern số nhà/đường phổ biến
        val patterns = listOf(
            Regex("(\\d+\\s*[A-Za-z]?\\s*[A-Za-zÀ-ỹ\\s]+)"), // 135/2 Đình Phong Phú
            Regex("(\\d+\\s*[A-Za-z]?\\s*[A-Za-zÀ-ỹ\\s]+)"), // 135 Đình Phong Phú
            Regex("(\\d+\\s*[A-Za-z]?\\s*[A-Za-zÀ-ỹ\\s]+)")  // 135A Đình Phong Phú
        )
        
        for (pattern in patterns) {
            val match = pattern.find(address)
            if (match != null) {
                val result = match.value.trim()
                // Kiểm tra xem có chứa tên đường không
                if (result.contains("Đình", ignoreCase = true) || 
                    result.contains("Đường", ignoreCase = true) ||
                    result.contains("Phong", ignoreCase = true)) {
                    return result
                }
            }
        }
        
        // Fallback: lấy phần đầu tiên của địa chỉ
        return address.split(",")[0].trim()
    }
    
    /**
     * Trích xuất các thành phần chính của địa chỉ
     */
    private fun extractKeyAddressComponents(address: String): String {
        val components = mutableListOf<String>()
        
        // Tìm số nhà và tên đường
        val streetMatch = Regex("(\\d+\\s*[A-Za-z]?\\s*[A-Za-zÀ-ỹ\\s]+)").find(address)
        if (streetMatch != null) {
            components.add(streetMatch.value.trim())
        }
        
        // Tìm phường/xã
        val wardMatch = Regex("(P\\.|Phường|Xã)\\s*([A-Za-zÀ-ỹ\\s]+)").find(address)
        if (wardMatch != null) {
            components.add("${wardMatch.groupValues[1]} ${wardMatch.groupValues[2].trim()}")
        }
        
        // Tìm quận/huyện
        val districtMatch = Regex("(Q\\.|Quận|Huyện)\\s*([A-Za-zÀ-ỹ\\s]+)").find(address)
        if (districtMatch != null) {
            components.add("${districtMatch.groupValues[1]} ${districtMatch.groupValues[2].trim()}")
        }
        
        // Thêm thành phố và quốc gia
        components.add("Ho Chi Minh City, Vietnam")
        
        return components.joinToString(", ")
    }
    
    /**
     * Xây dựng địa chỉ cụ thể với số nhà để tăng độ chính xác
     */
    private fun buildSpecificAddressWithNumber(address: String): String {
        val parts = address.split(",").map { it.trim() }
        val specificParts = mutableListOf<String>()
        
        // Tìm và ưu tiên số nhà cụ thể
        val streetAndNumber = extractStreetAndNumber(address)
        if (streetAndNumber.isNotEmpty()) {
            specificParts.add(streetAndNumber)
        }
        
        // Lấy phường/xã
        val ward = parts.find { it.contains("P.", ignoreCase = true) || it.contains("Phường", ignoreCase = true) }
        if (ward != null) specificParts.add(ward)
        
        // Lấy quận/huyện
        val district = parts.find { it.contains("Q.", ignoreCase = true) || it.contains("Quận", ignoreCase = true) }
        if (district != null) specificParts.add(district)
        
        // Thêm thành phố và quốc gia
        specificParts.add("Ho Chi Minh City, Vietnam")
        
        return specificParts.joinToString(", ")
    }
    
    /**
     * Xây dựng địa chỉ cụ thể dựa trên các thành phần
     */
    private fun buildSpecificAddress(address: String): String {
        val parts = address.split(",").map { it.trim() }
        val specificParts = mutableListOf<String>()
        
        // Lấy số nhà và tên đường
        if (parts.isNotEmpty()) {
            specificParts.add(parts[0])
        }
        
        // Lấy phường/xã
        val ward = parts.find { it.contains("P.", ignoreCase = true) || it.contains("Phường", ignoreCase = true) }
        if (ward != null) specificParts.add(ward)
        
        // Lấy quận/huyện
        val district = parts.find { it.contains("Q.", ignoreCase = true) || it.contains("Quận", ignoreCase = true) }
        if (district != null) specificParts.add(district)
        
        // Thêm thành phố và quốc gia
        specificParts.add("Ho Chi Minh City, Vietnam")
        
        return specificParts.joinToString(", ")
    }
    
    /**
     * Thực hiện request geocoding
     */
    private suspend fun performGeocodingRequest(attempt: String): com.trungkien.fbtp_cn.model.GeoLocation? {
        try {
            val encodedAddress = URLEncoder.encode(attempt, StandardCharsets.UTF_8.toString())
            val url = "$baseUrl?q=$encodedAddress&format=json&limit=5&countrycodes=vn&addressdetails=1&extratags=1"
            
            
            
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "FBTP_CN/1.0")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                
                if (!responseBody.isNullOrEmpty()) {
                    val jsonArray = org.json.JSONArray(responseBody)
                    
                    if (jsonArray.length() > 0) {
                        // Tìm kết quả tốt nhất
                        return findBestResult(jsonArray, attempt)
                    }
                }
            }
            
            return null
        } catch (e: Exception) {
            
            return null
        }
    }
    
    /**
     * Tìm kết quả tốt nhất từ danh sách kết quả
     */
    private fun findBestResult(jsonArray: org.json.JSONArray, originalAttempt: String): com.trungkien.fbtp_cn.model.GeoLocation? {
        var bestResult: com.trungkien.fbtp_cn.model.GeoLocation? = null
        var bestScore = 0
        
        for (i in 0 until jsonArray.length()) {
            val result = jsonArray.getJSONObject(i)
            val lat = result.getDouble("lat")
            val lng = result.getDouble("lon")
            val displayName = result.optString("display_name", "")
            val importance = result.optDouble("importance", 0.0)
            
            
            
            // Tính điểm dựa trên độ quan trọng và độ khớp với địa chỉ gốc
            val score = calculateResultScore(displayName, importance, originalAttempt)
            
            if (score > bestScore) {
                bestScore = score
                bestResult = com.trungkien.fbtp_cn.model.GeoLocation(
                    lat = lat,
                    lng = lng,
                    geohash = ""
                )
            }
        }
        
        
        return bestResult
    }
    
    /**
     * Tính điểm cho kết quả geocoding
     */
    private fun calculateResultScore(displayName: String, importance: Double, originalAttempt: String): Int {
        var score = 0
        
        // Điểm cơ bản từ importance
        score += (importance * 10).toInt()
        
        // Trích xuất tỉnh từ địa chỉ gốc
        val originalProvince = extractProvince(originalAttempt)
        val originalWard = extractWard(originalAttempt)
        
        // Bonus cao cho tỉnh/thành phố khớp
        if (originalProvince.isNotEmpty()) {
            val noAccentOriginalProvince = removeVietnameseAccents(originalProvince)
            val noAccentDisplayProvince = removeVietnameseAccents(displayName)
            
            if (displayName.contains(originalProvince, ignoreCase = true) || 
                noAccentDisplayProvince.contains(noAccentOriginalProvince, ignoreCase = true)) {
                score += 60 // Bonus cao cho tỉnh khớp
            }
        }
        
        // Bonus cao cho phường/xã khớp
        if (originalWard.isNotEmpty()) {
            val noAccentOriginalWard = removeVietnameseAccents(originalWard)
            val noAccentDisplayWard = removeVietnameseAccents(displayName)
            
            if (displayName.contains(originalWard, ignoreCase = true) || 
                noAccentDisplayWard.contains(noAccentOriginalWard, ignoreCase = true)) {
                score += 40 // Bonus cao cho phường khớp
            }
        }
        
        // Bonus cho địa chỉ cụ thể (có số nhà)
        if (Regex("\\d+").containsMatchIn(displayName)) {
            score += 15
        }
        
        // Bonus đặc biệt cho số nhà cụ thể khớp với địa chỉ gốc
        val originalNumber = extractHouseNumber(originalAttempt)
        val displayNumber = extractHouseNumber(displayName)
        if (originalNumber.isNotEmpty() && displayNumber.isNotEmpty()) {
            if (originalNumber == displayNumber) {
                score += 50 // Bonus rất cao cho số nhà khớp chính xác
            } else if (originalNumber.contains(displayNumber) || displayNumber.contains(originalNumber)) {
                score += 25 // Bonus cao cho số nhà tương tự
            }
        }
        
        // Penalty rất cao cho các tỉnh/thành phố sai
        if (originalProvince.isNotEmpty()) {
            val allProvinces = listOf(
                "Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
                "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
                "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
                "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông",
                "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang",
                "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình",
                "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
                "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định",
                "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên",
                "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị",
                "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên",
                "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang",
                "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
            )
            
            for (province in allProvinces) {
                if (province != originalProvince && displayName.contains(province, ignoreCase = true)) {
                    score -= 80 // Penalty cao cho tỉnh sai
                    break
                }
            }
        }
        
        // Bonus đặc biệt nếu địa chỉ gốc chứa tỉnh và kết quả cũng chứa tỉnh đó
        if (originalProvince.isNotEmpty() && 
            displayName.contains(originalProvince, ignoreCase = true)) {
            score += 30
        }
        
        // Bonus đặc biệt nếu địa chỉ gốc chứa phường và kết quả cũng chứa phường đó
        if (originalWard.isNotEmpty() && 
            displayName.contains(originalWard, ignoreCase = true)) {
            score += 25
        }
        
        // Penalty cho các địa điểm không liên quan (công nghiệp, khu vực khác)
        val irrelevantKeywords = listOf(
            "Khu công nghiệp", "Industrial", "Factory", "Warehouse",
            "Trường Chinh", "Truong Chinh", "District 1", "Quận 1"
        )
        for (keyword in irrelevantKeywords) {
            if (displayName.contains(keyword, ignoreCase = true)) {
                score -= 50
            }
        }
        
        
        return score
    }
    
    /**
     * Kiểm tra xem kết quả có chính xác không
     */
    private fun isAccurateResult(result: com.trungkien.fbtp_cn.model.GeoLocation, originalAddress: String): Boolean {
        // Kiểm tra xem tọa độ có nằm trong khu vực Việt Nam không
        val isInVietnam = result.lat in 8.0..23.0 && result.lng in 102.0..110.0
        
        if (!isInVietnam) {
            return false
        }
        
        // Trích xuất tỉnh từ địa chỉ gốc
        val originalProvince = extractProvince(originalAddress)
        
        // Kiểm tra reverse geocoding để xác nhận
        return try {
            val reverseAddress = kotlinx.coroutines.runBlocking { reverseGeocode(result.lat, result.lng) }
            val isAccurate = reverseAddress?.let { 
                if (originalProvince.isNotEmpty()) {
                    // Kiểm tra tỉnh khớp
                    it.contains(originalProvince, ignoreCase = true) || 
                    it.contains(removeVietnameseAccents(originalProvince), ignoreCase = true)
                } else {
                    // Nếu không có tỉnh cụ thể, chỉ cần trong Việt Nam
                    true
                }
            } ?: false
            
            isAccurate
        } catch (e: Exception) {
            true // Giả sử chính xác nếu không thể kiểm tra
        }
    }
    
    /**
     * Chuyển đổi tọa độ thành địa chỉ (reverse geocoding)
     * @param lat Vĩ độ
     * @param lng Kinh độ
     * @return Địa chỉ hoặc null nếu không tìm thấy
     */
    suspend fun reverseGeocode(lat: Double, lng: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                
                val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json"
                
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "FBTP_CN/1.0")
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    
                    if (!responseBody.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseBody)
                        val displayName = jsonObject.optString("display_name", "")
                        return@withContext displayName
                    }
                }
                
                null
            } catch (e: Exception) {
                null
            }
        }
    }
}

