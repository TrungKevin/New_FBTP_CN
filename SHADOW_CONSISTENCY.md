# Shadow Consistency Guide

## Tổng quan
File này giải thích cách đảm bảo độ đổ bóng nhất quán giữa preview và khi chạy thực tế trong ứng dụng FBTP_CN.

## Vấn đề
Độ đổ bóng có thể hiển thị khác nhau giữa:
- **Preview**: Sử dụng CPU render với độ chính xác thấp
- **Emulator/Device**: Sử dụng GPU render với độ chính xác cao

## Giải pháp
Sử dụng **Surface elevation** thay vì **shadow modifier** để đảm bảo nhất quán.

### 1. Sử dụng Surface với shadowElevation
```kotlin
// ✅ Đúng - Sử dụng Surface elevation
Surface(
    shadowElevation = CommonShadows.Button,
    shape = CircleShape,
    color = Color.White
) {
    // content
}

// ❌ Sai - Sử dụng shadow modifier
Box(
    modifier = Modifier.shadow(elevation = 4.dp)
) {
    // content
}
```

### 2. Sử dụng Card với elevation
```kotlin
// ✅ Đúng - Sử dụng Card elevation
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = CommonShadows.Card),
    // ... other properties
) {
    // content
}
```

## Constants được định nghĩa

### ShadowElevation
```kotlin
object ShadowElevation {
    val None: Dp = 0.dp
    val Small: Dp = 2.dp      // Badge, small elements
    val Medium: Dp = 4.dp     // Button, floating elements
    val Large: Dp = 8.dp      // Card, navigation bar
    val ExtraLarge: Dp = 16.dp // Dialog, modal
}
```

### CommonShadows
```kotlin
object CommonShadows {
    val Card = ShadowElevation.Large           // 8.dp
    val Button = ShadowElevation.Medium       // 4.dp
    val Badge = ShadowElevation.Small         // 2.dp
    val NavigationBar = ShadowElevation.Large // 8.dp
    val BarElevation = ShadowElevation.Large  // 8.dp
    val ItemElevation = ShadowElevation.Medium // 4.dp
    val FloatingActionButton = ShadowElevation.Medium // 4.dp
    val Dialog = ShadowElevation.ExtraLarge   // 16.dp
}
```

## Cách sử dụng

### Import
```kotlin
import com.trungkien.fbtp_cn.ui.theme.CommonShadows
```

### Áp dụng
```kotlin
// Surface components
Surface(
    shadowElevation = CommonShadows.Button,
    // ... other properties
) {
    // content
}

// Card components
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = CommonShadows.Card),
    // ... other properties
) {
    // content
}

// Button components
Button(
    elevation = ButtonDefaults.buttonElevation(defaultElevation = CommonShadows.Button),
    // ... other properties
) {
    // content
}
```

## Lợi ích
1. **Nhất quán**: Độ đổ bóng giống nhau giữa preview và runtime
2. **Dễ bảo trì**: Thay đổi giá trị shadow ở một nơi duy nhất
3. **Hiệu suất**: Surface elevation được tối ưu hóa tốt hơn shadow modifier
4. **Material Design**: Tuân thủ Material Design guidelines

## Lưu ý
- Không sử dụng `.shadow()` modifier
- Luôn sử dụng `shadowElevation` cho Surface
- Luôn sử dụng `elevation` cho Card và Button
- Sử dụng constants từ `CommonShadows` thay vì hardcode values
