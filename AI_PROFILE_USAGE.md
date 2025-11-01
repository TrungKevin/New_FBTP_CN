# AI Profile - Hướng dẫn sử dụng

## 1. Model AiProfile

### 1.1. Skill (Float)
**Mục đích**: Lưu `weightedWinRate` đã tính sẵn để AI Agent sử dụng nhanh

**Công thức tính**:
```kotlin
skill = winRate * (N / (N + C))
- winRate = wins / totalMatches
- N = totalMatches
- C = 10 (hằng số điều chỉnh)
```

**Lợi ích**:
- Tính 1 lần khi có match result mới → Cập nhật `ai_profiles/{renterId}` 
- AI Agent không cần query lại `match_results` mỗi lần
- Có thể cache theo `fieldId` (skill khác nhau ở các sân khác nhau)

**Ví dụ**:
- Renter có 8 win, 2 loss → winRate = 0.8, totalMatches = 10
- skill = 0.8 * (10 / (10 + 10)) = 0.4

---

### 1.2. FormRecent (List<String>)
**Mục đích**: Lưu phong độ 5 trận gần nhất để đánh giá form hiện tại

**Cấu trúc**:
```kotlin
formRecent = ["W", "W", "L", "W", "D"]
- W = Win (Thắng)
- L = Loss (Thua)  
- D = Draw (Hòa)
- Thứ tự: trận gần nhất ở cuối
```

**Tính toán**:
- `recentWins`: Đếm số "W" trong `formRecent`
- `recentTotal`: Độ dài `formRecent` (có thể < 5 nếu chưa đủ trận)
- `recentWinRate()`: recentWins / recentTotal

**Ví dụ**:
- formRecent = ["W", "W", "L", "W", "W"]
- recentWins = 4
- recentTotal = 5
- recentWinRate = 0.8 (80%)

---

## 2. UI Hiển thị

### 2.1. OpponentCard (Tab "Gợi ý")

**Hiện tại**:
```
[Avatar] Tên đối thủ
          AI 68/100 | pWin 65%
          AI dự đoán: Thắng 65% • Hòa 15% • Thua 20%
```

**Sau khi thêm AiProfile**:
```
[Avatar] Tên đối thủ
          AI 68/100 | pWin 65%
          Phong độ: ⚡⚡⚡⚡⚪ (4/5 thắng) | Tốt
          AI dự đoán: Thắng 65% • Hòa 15% • Thua 20%
```

**Giải thích ngắn** (dựa trên formRecent):
- "Phong độ tốt! Thắng 4/5 trận gần đây"
- "Phong độ ổn định, thắng 2/5 trận"
- "Cần cải thiện, thắng 1/5 trận gần đây"

---

### 2.2. OpponentDetailSheet

**Hiện tại**:
```
[Ảnh] Tên đối thủ
AI 68/100 | Win 72.5% • Trận 11
AI nhận định: Gần đây: thắng 4/5 trận
```

**Sau khi thêm AiProfile**:
```
[Ảnh] Tên đối thủ
AI 68/100 | Win 72.5% • Trận 11
Phong độ: W-W-W-L-W (Thắng 4/5)
AI nhận định: Phong độ tốt! Thắng 4/5 trận gần đây
```

---

## 3. Cập nhật AiProfile

### 3.1. Khi có Match Result mới

**Flow**:
1. Owner lưu `match_result` → Trigger Cloud Function hoặc local update
2. Tính lại `skill` từ `match_results` của renter
3. Lấy 5 trận gần nhất → Tạo `formRecent`
4. Cập nhật `ai_profiles/{renterId}` trong Firestore

**Ví dụ code** (pseudo):
```kotlin
fun updateAiProfile(renterId: String, fieldId: String?) {
    // 1. Tính skill từ match_results
    val matches = getMatchResults(renterId, fieldId)
    val skill = calculateWeightedWinRate(matches)
    
    // 2. Lấy 5 trận gần nhất
    val recent5 = matches.sortedByDescending { it.recordedAt }.take(5)
    val formRecent = recent5.map { match ->
        when {
            match.winnerRenterId == renterId -> "W"
            match.loserRenterId == renterId -> "L"
            match.isDraw -> "D"
            else -> "?"
        }
    }.reversed() // Trận gần nhất ở cuối
    
    // 3. Cập nhật ai_profiles
    val profile = AiProfile(
        renterId = renterId,
        fieldId = fieldId,
        skill = skill,
        formRecent = formRecent,
        recentWins = formRecent.count { it == "W" },
        recentTotal = formRecent.size,
        lastMatchAt = recent5.firstOrNull()?.recordedAt,
        updatedAt = System.currentTimeMillis()
    )
    saveAiProfile(profile)
}
```

---

## 4. Firestore Structure

### Collection: `ai_profiles`

**Document ID**: `{renterId}` (hoặc `{renterId}_{fieldId}` nếu theo sân)

**Ví dụ document**:
```json
{
  "renterId": "user123",
  "fieldId": "field_abc", // null nếu skill tổng thể
  "skill": 0.68,
  "formRecent": ["W", "W", "L", "W", "W"],
  "recentWins": 4,
  "recentTotal": 5,
  "lastMatchAt": 1704067200000,
  "updatedAt": 1704067200000,
  "version": 1
}
```

---

## 5. Tóm tắt

**Skill**:
- ✅ Lưu `weightedWinRate` đã tính sẵn
- ✅ AI Agent dùng nhanh, không cần query lại
- ✅ Có thể cache theo `fieldId`

**FormRecent**:
- ✅ Lưu 5 trận gần nhất: ["W", "W", "L", "W", "D"]
- ✅ Hiển thị trên UI: "Thắng 4/5 trận" + icon phong độ
- ✅ Giúp renter đánh giá đối thủ tốt hơn

**Lợi ích**:
- 🚀 Performance: Tính 1 lần, dùng nhiều lần
- 🎯 Accuracy: Phong độ gần đây phản ánh form hiện tại
- 💡 UX: Renter thấy được "form" của đối thủ trước khi quyết định
