# Logic Tính Toán và Hiển thị trong OpponentDetailSheet

## 📋 Tổng quan

`OpponentDetailSheet` hiển thị thông tin chi tiết về một đối thủ dựa trên **lịch sử thi đấu tại sân hiện tại** (`fieldId`). Tất cả các tính toán đều dựa trên dữ liệu từ `match_results` của sân đó.

---

## 🔍 Bước 1: Lấy dữ liệu từ Database

### Query Match Results theo 3 loại:

```kotlin
// 1. Lấy các trận đối thủ THẮNG (tại sân hiện tại)
val winnerQuery = db.collection("match_results")
    .whereEqualTo("winnerRenterId", renterId)
    .whereEqualTo("fieldId", fieldId)  // ✅ CHỈ sân hiện tại

// 2. Lấy các trận đối thủ THUA (tại sân hiện tại)
val loserQuery = db.collection("match_results")
    .whereEqualTo("loserRenterId", renterId)
    .whereEqualTo("fieldId", fieldId)  // ✅ CHỈ sân hiện tại

// 3. Lấy các trận HÒA (tại sân hiện tại)
val drawQuery = db.collection("match_results")
    .whereEqualTo("isDraw", true)
    .whereEqualTo("fieldId", fieldId)  // ✅ CHỈ sân hiện tại
```

### Combine và loại bỏ duplicate:

- Merge 3 list: winner, loser, draw
- Dùng `Set<String>` để tránh trận đấu bị đếm 2 lần
- Kết quả: `matchResults` - danh sách TẤT CẢ các trận đấu của đối thủ tại sân này

---

## 📊 Bước 2: Tính toán các chỉ số

### 2.1. Đếm số trận thắng/thua/hòa

```kotlin
var wins = 0      // Số trận thắng
var losses = 0    // Số trận thua
var draws = 0     // Số trận hòa

matchResults.forEach { match ->
    when {
        match.isDraw && (match.winnerRenterId == renterId || match.loserRenterId == renterId) -> {
            draws++  // Trận hòa
        }
        match.winnerRenterId == renterId -> {
            wins++   // Đối thủ thắng
        }
        match.loserRenterId == renterId -> {
            losses++ // Đối thủ thua
        }
    }
}
```

**Ví dụ:**
- Đối thủ đã chơi 4 trận tại sân này
- 4 trận đều thắng → `wins = 4`, `losses = 0`, `draws = 0`

---

### 2.2. Tính Total Matches (Tổng số trận)

```kotlin
calculatedTotalMatches = matchResults.size
```

**Kết quả:** Hiển thị "Trận 4" trong UI

---

### 2.3. Tính Win Rate (Tỷ lệ thắng)

```kotlin
val total = wins + losses + draws  // Tổng số trận
val winRateCalculated = wins.toFloat() / total.toFloat()
```

**Công thức:** `Win Rate = (Số trận thắng / Tổng số trận) * 100%`

**Ví dụ:**
- `wins = 4`, `total = 4`
- `winRate = 4 / 4 = 1.0 = 100%`
- Hiển thị: "Win 100.0%"

---

### 2.4. Tính AI Score (Weighted Win Rate)

```kotlin
val C = 10f  // Hằng số điều chỉnh
val weightedWinRate = winRate * (total / (total + C))
calculatedAiScore = (weightedWinRate * 100).toInt()
```

**Công thức chi tiết:**
```
weightedWinRate = winRate × (N / (N + C))
- winRate: Tỷ lệ thắng (0.0 - 1.0)
- N: Tổng số trận đã chơi (totalMatches)
- C: Hằng số = 10

AI Score = weightedWinRate × 100
```

**Tại sao dùng công thức này?**
- **Vấn đề:** Người chơi mới có thể thắng 1/1 trận = 100%, nhưng không đáng tin bằng người thắng 8/10 trận = 80%
- **Giải pháp:** Công thức điều chỉnh dựa trên số trận đã chơi
  - Nhiều trận hơn → tin cậy hơn → điểm cao hơn
  - Ít trận hơn → ít tin cậy → điểm thấp hơn

**Ví dụ cụ thể:**

| Trường hợp | Wins | Total | WinRate | Weighted | AI Score |
|------------|------|-------|---------|----------|----------|
| Mới chơi | 1 | 1 | 100% | 100% × (1/11) = 9.1% | **9/100** |
| Đã chơi nhiều | 8 | 10 | 80% | 80% × (10/20) = 40% | **40/100** |
| Pro player | 8 | 8 | 100% | 100% × (8/18) = 44.4% | **44/100** |
| Trường hợp trong ảnh | 4 | 4 | 100% | 100% × (4/14) = 28.6% | **28/100** ✅ |

**Giải thích trường hợp trong ảnh:**
- 4 trận thắng / 4 trận = 100% winRate
- Nhưng chỉ có 4 trận nên độ tin cậy thấp
- `weightedWinRate = 1.0 × (4 / (4 + 10)) = 1.0 × (4/14) = 0.286 = 28.6%`
- `AI Score = 28.6 × 100 = 28` → Hiển thị "AI 28/100"

---

## 📈 Bước 3: Tính Recent Form (5 trận gần nhất)

### Lấy và sắp xếp:

```kotlin
// Sắp xếp theo thời gian ghi nhận (mới nhất trước)
val recent5 = matchResults
    .distinctBy { it.resultId }          // Loại bỏ duplicate
    .sortedByDescending { it.recordedAt } // Mới nhất lên đầu
    .take(5)                              // Lấy 5 trận gần nhất
```

### Chuyển đổi thành W/L/D:

```kotlin
recentForm = recent5.map { match ->
    when {
        match.isDraw -> "D"                                    // Hòa
        match.winnerRenterId == renterId -> "W"                // Thắng
        match.loserRenterId == renterId -> "L"                // Thua
        else -> "?"                                           // Unknown
    }
}.reversed()  // Đảo ngược để trận gần nhất ở cuối
```

**Kết quả:**
- Ví dụ: `["W", "W", "W", "W"]` → Hiển thị 4 vòng tròn màu xanh lá cây (W)

---

## 🎯 Tóm tắt Flow

```
1. Query match_results từ database (fieldId cụ thể)
   ↓
2. Combine winner + loser + draw matches
   ↓
3. Loại bỏ duplicate
   ↓
4. Đếm wins/losses/draws
   ↓
5. Tính totalMatches = matchResults.size
   ↓
6. Tính winRate = wins / total
   ↓
7. Tính weightedWinRate = winRate × (N / (N + C))
   ↓
8. Tính AI Score = weightedWinRate × 100
   ↓
9. Lấy 5 trận gần nhất → recentForm
   ↓
10. Hiển thị trên UI
```

---

## ✅ Đảm bảo tính chính xác

1. **Tất cả dữ liệu từ cùng 1 sân:**
   - Tất cả query đều có `whereEqualTo("fieldId", fieldId)`
   - Không mix dữ liệu từ các sân khác

2. **Không duplicate:**
   - Dùng `Set<String>` để tránh đếm trận 2 lần
   - Mỗi `resultId` chỉ được đếm 1 lần

3. **Xử lý draw matches:**
   - Chỉ thêm draw match nếu renter tham gia (là winner hoặc loser)

4. **Consistency:**
   - Total matches = số phần tử trong matchResults
   - Win rate = wins / total matches
   - AI score dựa trên cùng dữ liệu

---

## 📱 Hiển thị trên UI

```kotlin
// Chip 1: AI Score
Text("AI ${calculatedAiScore}/100")  // Ví dụ: "AI 28/100"

// Chip 2: Win Rate + Total Matches
Text("Win ${winRate * 100}% • Trận $totalMatches")  // Ví dụ: "Win 100.0% • Trận 4"

// Recent Form: Vòng tròn W/L/D
recentForm.forEach { result ->
    // W = xanh lá, L = đỏ, D = vàng
}
```

---

## 🔄 Khi nào dữ liệu được cập nhật?

- Dữ liệu được load khi mở `OpponentDetailSheet`
- Tự động tính lại từ `match_results` mới nhất
- Không cache, luôn tính toán realtime

---

## 🎓 Lưu ý quan trọng

1. **AI Score không phải là win rate đơn giản:**
   - AI Score thấp hơn win rate khi số trận ít
   - AI Score cao hơn khi có nhiều trận và win rate tốt

2. **Recent Form có thể < 5 trận:**
   - Nếu đối thủ chơi ít hơn 5 trận → chỉ hiển thị số trận có
   - Ví dụ: 3 trận → chỉ hiển thị 3 vòng tròn

3. **Tất cả tính toán theo sân:**
   - Một đối thủ có thể có AI Score khác nhau ở các sân khác nhau
   - Phù hợp vì kỹ năng có thể khác nhau giữa các sân

