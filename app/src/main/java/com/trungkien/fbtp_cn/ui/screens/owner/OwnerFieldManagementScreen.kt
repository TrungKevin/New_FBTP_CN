package com.trungkien.fbtp_cn.ui.screens.owner // Package màn hình phía owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.components.owner.FieldCard
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@OptIn(ExperimentalMaterial3Api::class) // Cho phép dùng API experimental của Material3
@Composable // Định nghĩa một composable function
fun OwnerFieldManagementScreen( // Màn hình quản lý sân của chủ sở hữu
    onFieldClick: (String) -> Unit, // Callback khi click vào sân
    modifier: Modifier = Modifier // Modifier truyền từ ngoài vào
) {
    var fields by remember { mutableStateOf(getMockFields()) } // State lưu danh sách sân (dữ liệu mẫu)

    Column(modifier = modifier) {
        // Header với tiêu đề và nút tìm kiếm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quản lý sân",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* Tìm kiếm */ }) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
            }
        }

        // Nội dung chính
        if (fields.isEmpty()) { // Không có dữ liệu
            Box( // Hộp căn giữa
                modifier = Modifier
                    .fillMaxSize() // Chiếm toàn bộ màn hình
                    .padding(16.dp), // Áp dụng padding cố định
                contentAlignment = Alignment.Center // Căn giữa
            ) {
                Text(
                    text = "Chưa có sân nào", // Thông báo rỗng
                    style = MaterialTheme.typography.bodyLarge, // Kiểu chữ
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Màu chữ phụ
                )
            }
        } else { // Có dữ liệu
            LazyColumn( // Danh sách dọc
                modifier = Modifier.fillMaxWidth(), // Chiếm toàn bộ chiều rộng
                contentPadding = PaddingValues(16.dp), // Padding xung quanh 16dp
                verticalArrangement = Arrangement.spacedBy(16.dp) // Khoảng cách giữa các thẻ 16dp
            ) {
                items(fields) { field -> // Mỗi phần tử là một sân
                    FieldCard( // Thẻ sân
                        field = field, // Truyền dữ liệu sân
                        onClick = { clickedField -> // Khi nhấn thẻ
                            onFieldClick(clickedField.id) // Gọi callback thay vì navigate trực tiếp
                        },
                        onViewDetailsClick = { // Khi nhấn button XEM CHI TIẾT
                            onFieldClick(field.id) // Gọi callback để chuyển đến chi tiết sân
                        }
                    )
                }
            }
        }
        
        // Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { /* Thêm sân mới */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm sân")
            }
        }
    }
}

// Mock data cho testing - Dữ liệu mẫu để kiểm thử
private fun getMockFields(): List<Field> { // Tạo danh sách sân mẫu để hiển thị
    return listOf( // Trả về danh sách các phần tử Field
        Field( // Phần tử 1
            id = "1", // Mã sân
            name = "POC Pickleball", // Tên sân
            type = "Pickleball", // Loại sân
            price = 150000, // Giá theo giờ
            imageUrl = "https://via.placeholder.com/150/0000FF/FFFFFF?text=PBL", // Ảnh minh họa
            status = "Available", // Trạng thái
            isAvailable = true, // Có sẵn
            address = "25 Tú Xương, P. Tăng Nhơn Phú B, TP. Thủ Đức", // Địa chỉ
            operatingHours = "05:00 - 23:00", // Giờ mở cửa
            contactPhone = "0926666357", // SĐT liên hệ
            distance = "835.3m" // Khoảng cách
        ),
        Field( // Phần tử 2
            id = "2",
            name = "Sân Cầu Lông ABC",
            type = "Cầu Lông",
            price = 120000,
            imageUrl = "https://via.placeholder.com/150/FF0000/FFFFFF?text=CBL",
            status = "Booked",
            isAvailable = false,
            address = "123 Đường XYZ, Quận 1, TP.HCM",
            operatingHours = "06:00 - 22:00",
            contactPhone = "0901234567",
            distance = "1.2km"
        ),
        Field( // Phần tử 3
            id = "3",
            name = "Sân Bóng Đá Mini",
            type = "Bóng Đá",
            price = 300000,
            imageUrl = "https://via.placeholder.com/150/00FF00/FFFFFF?text=BĐ",
            status = "Available",
            isAvailable = true,
            address = "456 Đường QWE, Quận 7, TP.HCM",
            operatingHours = "07:00 - 23:00",
            contactPhone = "0987654321",
            distance = "2.5km"
        )
    )
}

@Preview // Đánh dấu đây là hàm preview
@Composable // Đánh dấu đây là một composable function
fun OwnerFieldManagerPreview() { // Hàm xem trước UI màn hình quản lý sân
    FBTP_CNTheme { // Áp dụng theme
        OwnerFieldManagementScreen( // Gọi composable chính
            onFieldClick = { /* Preview callback */ },
            modifier = Modifier.fillMaxSize() // Chiếm toàn bộ diện tích
        )
    }
}