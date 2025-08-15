package com.trungkien.fbtp_cn.ui.components.owner.info // Package cho các component thông tin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background // Import background
import androidx.compose.foundation.clickable // Import clickable
import androidx.compose.foundation.layout.* // Import layout
import androidx.compose.foundation.lazy.LazyColumn // Import LazyColumn
import androidx.compose.foundation.lazy.LazyRow // Import LazyRow
import androidx.compose.foundation.lazy.grid.GridCells // Import GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Import LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Import items
import androidx.compose.foundation.lazy.items // Import items
import androidx.compose.foundation.shape.RoundedCornerShape // Import RoundedCornerShape
import androidx.compose.material3.* // Import Material3
import androidx.compose.runtime.* // Import runtime
import androidx.compose.ui.Alignment // Import Alignment
import androidx.compose.ui.Modifier // Import Modifier
import androidx.compose.ui.draw.clip // Import clip
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.text.font.FontWeight // Import FontWeight
import androidx.compose.ui.text.style.TextAlign // Import TextAlign
import androidx.compose.ui.tooling.preview.Preview // Import Preview
import androidx.compose.ui.unit.dp // Import dp
import androidx.compose.ui.unit.sp // Import sp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme // Import theme
import java.time.LocalDate // Import LocalDate
import java.time.format.TextStyle // Import TextStyle
import java.util.Locale // Import Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable // Đánh dấu đây là một composable function
fun TimeSlots( // Component hiển thị khung giờ
    modifier: Modifier = Modifier // Modifier tùy chỉnh
) {
    // State cho ngày được chọn
    var selectedDate by remember { mutableStateOf(LocalDate.now()) } // Ngày hiện tại được chọn mặc định
    
    // State cho giờ bắt đầu và kết thúc (có thể để owner tùy chỉnh)
    val startHour = 6 // Giờ bắt đầu: 6:00
    val endHour = 22 // Giờ kết thúc: 22:00
    
    // Tạo danh sách 7 ngày liên tiếp (hôm nay + 6 ngày tiếp theo)
    val dates = remember {
        (0..6).map { LocalDate.now().plusDays(it.toLong()) }
    }
    
    // Tạo danh sách các khe thời gian cách nhau 30 phút
    val timeSlots = remember {
        (startHour * 2..endHour * 2).map { halfHour ->
            val hour = halfHour / 2
            val minute = if (halfHour % 2 == 0) 0 else 30
            String.format("%02d:%02d", hour, minute)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp) // Padding xung quanh
    ) {
        // Tiêu đề
        Text(
            text = "Khung giờ hoạt động", // Tiêu đề chính
            style = MaterialTheme.typography.headlineSmall, // Kiểu chữ tiêu đề
            fontWeight = FontWeight.Bold, // Chữ đậm
            color = MaterialTheme.colorScheme.onSurface, // Màu chữ
            modifier = Modifier.padding(bottom = 16.dp) // Khoảng cách dưới
        )
        
        // Phần chọn ngày - 7 ngày liên tiếp trên một dòng
        Text(
            text = "Chọn ngày", // Tiêu đề phần chọn ngày
            style = MaterialTheme.typography.titleMedium, // Kiểu chữ
            fontWeight = FontWeight.SemiBold, // Chữ đậm vừa
            color = MaterialTheme.colorScheme.onSurface, // Màu chữ
            modifier = Modifier.padding(bottom = 12.dp) // Khoảng cách dưới
        )
        
        // Dòng ngày với 7 ngày liên tiếp
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Khoảng cách giữa các ngày
            contentPadding = PaddingValues(vertical = 8.dp) // Padding trên dưới
        ) {
            items(dates) { date ->
                DateSelector(
                    date = date, // Ngày
                    isSelected = date == selectedDate, // Có được chọn không
                    onDateClick = { selectedDate = date } // Callback khi click
                )
            }
        }
        
        // Phần hiển thị giờ - chỉ hiển thị khi có ngày được chọn
        if (selectedDate != null) {
            Spacer(modifier = Modifier.height(20.dp)) // Khoảng cách giữa ngày và giờ
            
            Text(
                text = "Khung giờ ngày ${selectedDate.dayOfMonth}/${selectedDate.monthValue}", // Tiêu đề phần giờ
                style = MaterialTheme.typography.titleMedium, // Kiểu chữ
                fontWeight = FontWeight.SemiBold, // Chữ đậm vừa
                color = MaterialTheme.colorScheme.primary, // Màu xanh lá
                modifier = Modifier.padding(bottom = 12.dp) // Khoảng cách dưới
            )
            
            // Grid giờ với 4 cột
            TimeGrid(
                timeSlots = timeSlots, // Danh sách khe thời gian
                startHour = startHour, // Giờ bắt đầu
                endHour = endHour // Giờ kết thúc
            )
        }
        
        // Thông tin bổ sung
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp), // Khoảng cách trên
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) // Màu nền nhạt
            ),
            shape = RoundedCornerShape(12.dp) // Bo góc
        ) {
            Column(
                modifier = Modifier.padding(16.dp) // Padding bên trong
            ) {
                Text(
                    text = "Thông tin khung giờ", // Tiêu đề thông tin
                    style = MaterialTheme.typography.titleMedium, // Kiểu chữ
                    fontWeight = FontWeight.SemiBold, // Chữ đậm vừa
                    color = MaterialTheme.colorScheme.primary, // Màu xanh lá
                    modifier = Modifier.padding(bottom = 8.dp) // Khoảng cách dưới
                )
                
                Text(
                    text = "• Giờ hoạt động: $startHour:00 - $endHour:00", // Giờ hoạt động
                    style = MaterialTheme.typography.bodyMedium, // Kiểu chữ
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Màu chữ phụ
                    modifier = Modifier.padding(bottom = 4.dp) // Khoảng cách dưới
                )
                
                Text(
                    text = "• Khoảng cách giữa các khe: 30 phút", // Khoảng cách khe
                    style = MaterialTheme.typography.bodyMedium, // Kiểu chữ
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Màu chữ phụ
                    modifier = Modifier.padding(bottom = 4.dp) // Khoảng cách dưới
                )
                
                Text(
                    text = "• Có thể tùy chỉnh giờ bắt đầu và kết thúc", // Tùy chỉnh
                    style = MaterialTheme.typography.bodyMedium, // Kiểu chữ
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Màu chữ phụ
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable // Đánh dấu đây là một composable function
private fun DateSelector( // Component chọn ngày
    date: LocalDate, // Ngày
    isSelected: Boolean, // Có được chọn không
    onDateClick: () -> Unit, // Callback khi click
    modifier: Modifier = Modifier // Modifier tùy chỉnh
) {
    Box(
        modifier = modifier
            .width(60.dp) // Chiều rộng cố định
            .height(80.dp) // Chiều cao cố định
            .clip(RoundedCornerShape(12.dp)) // Bo góc
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) // Màu xanh lá đậm khi được chọn
                else 
                    MaterialTheme.colorScheme.surface // Màu nền bình thường
            )
            .clickable { onDateClick() } // Có thể click
            .padding(8.dp), // Padding bên trong
        contentAlignment = Alignment.Center // Căn giữa nội dung
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Căn giữa theo chiều ngang
        ) {
            // Tên ngày trong tuần
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("vi")), // Tên ngày tiếng Việt
                style = MaterialTheme.typography.labelSmall, // Kiểu chữ nhỏ
                fontWeight = FontWeight.Medium, // Chữ đậm vừa
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary // Màu xanh lá khi được chọn
                else 
                    MaterialTheme.colorScheme.onSurface, // Màu chữ bình thường
                textAlign = TextAlign.Center // Căn giữa chữ
            )
            
            // Số ngày
            Text(
                text = date.dayOfMonth.toString(), // Số ngày
                style = MaterialTheme.typography.titleMedium, // Kiểu chữ
                fontWeight = FontWeight.Bold, // Chữ đậm
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary // Màu xanh lá khi được chọn
                else 
                    MaterialTheme.colorScheme.onSurface, // Màu chữ bình thường
                textAlign = TextAlign.Center // Căn giữa chữ
            )
        }
    }
}

@Composable // Đánh dấu đây là một composable function
private fun TimeGrid( // Grid hiển thị các khe thời gian
    timeSlots: List<String>, // Danh sách khe thời gian
    startHour: Int, // Giờ bắt đầu
    endHour: Int, // Giờ kết thúc
    modifier: Modifier = Modifier // Modifier tùy chỉnh
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4), // 4 cột cố định
        horizontalArrangement = Arrangement.spacedBy(8.dp), // Khoảng cách ngang giữa các cột
        verticalArrangement = Arrangement.spacedBy(8.dp), // Khoảng cách dọc giữa các dòng
        modifier = modifier.fillMaxWidth() // Chiếm toàn bộ chiều rộng
    ) {
        items(timeSlots) { timeSlot ->
            TimeSlotItem(
                time = timeSlot, // Thời gian
                isAvailable = isTimeSlotAvailable(timeSlot, startHour, endHour) // Có khả dụng không
            )
        }
    }
}

@Composable // Đánh dấu đây là một composable function
private fun TimeSlotItem( // Item hiển thị một khe thời gian
    time: String, // Thời gian
    isAvailable: Boolean, // Có khả dụng không
    modifier: Modifier = Modifier // Modifier tùy chỉnh
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp) // Chiều cao cố định
            .clip(RoundedCornerShape(8.dp)) // Bo góc
            .background(
                if (isAvailable) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // Màu xanh lá nhạt khi khả dụng
                else 
                    Color.Gray.copy(alpha = 0.3f) // Màu xám khi không khả dụng
            )
            .padding(horizontal = 8.dp, vertical = 4.dp), // Padding
        contentAlignment = Alignment.Center // Căn giữa nội dung
    ) {
        Text(
            text = time, // Thời gian
            style = MaterialTheme.typography.labelMedium, // Kiểu chữ
            fontWeight = FontWeight.Medium, // Chữ đậm vừa
            color = if (isAvailable) 
                MaterialTheme.colorScheme.primary // Màu xanh lá khi khả dụng
            else 
                Color.Gray, // Màu xám khi không khả dụng
            textAlign = TextAlign.Center // Căn giữa chữ
        )
    }
}

// Hàm helper để kiểm tra khe thời gian có khả dụng không
private fun isTimeSlotAvailable(timeSlot: String, startHour: Int, endHour: Int): Boolean {
    val hour = timeSlot.split(":")[0].toInt() // Lấy giờ từ chuỗi thời gian
    return hour in startHour until endHour // Trả về true nếu giờ nằm trong khoảng hoạt động
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview // Đánh dấu đây là hàm preview
@Composable // Đánh dấu đây là một composable function
fun TimeSlotsPreview() { // Hàm preview cho component TimeSlots
    FBTP_CNTheme { // Sử dụng theme tùy chỉnh
        TimeSlots() // Gọi component TimeSlots
    }
}