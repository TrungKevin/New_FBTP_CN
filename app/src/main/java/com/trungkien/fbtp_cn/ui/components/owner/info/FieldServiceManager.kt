package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.FieldService
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent

/**
 * Model cho dịch vụ sân - Dễ hiển thị và chỉnh sửa
 */
data class FieldServiceItem(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val isActive: Boolean = true
)

/**
 * Composable quản lý bảng dịch vụ bổ sung
 */
@Composable
fun FieldServiceManager(
    fieldId: String,
    fieldViewModel: FieldViewModel,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false
) {
    // State cho danh sách dịch vụ
    var services by remember { mutableStateOf(emptyList<FieldServiceItem>()) }
    
    // State để force refresh UI khi cần thiết
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // State cho validation
    var validationErrors by remember { mutableStateOf(listOf<String>()) }
    
    // Khởi tạo dữ liệu ban đầu
    LaunchedEffect(fieldId, refreshTrigger) {
        println("🚀 DEBUG: FieldServiceManager - Bắt đầu load data cho field: $fieldId")
        loadFieldServices(fieldId, fieldViewModel)
    }
    
    // Observe UI state từ ViewModel
    val uiState by fieldViewModel.uiState.collectAsState()
    
    // Cập nhật dữ liệu khi có thay đổi từ Firebase
    LaunchedEffect(uiState.fieldServices, refreshTrigger) {
        println("🔄 DEBUG: FieldServiceManager - LaunchedEffect triggered - fieldServices: ${uiState.fieldServices.size}")
        
        // ✅ FIX: Lọc dịch vụ theo fieldId để đảm bảo chỉ hiển thị dịch vụ của sân hiện tại
        val fieldSpecificServices = uiState.fieldServices.filter { it.fieldId == fieldId }
        println("🏟️ DEBUG: FieldServiceManager - Dịch vụ của sân $fieldId: ${fieldSpecificServices.size} items")
        
        if (fieldSpecificServices.isNotEmpty()) {
            val mappedServices = mapFirebaseServicesToUI(fieldSpecificServices)
            services = mappedServices
            println("✅ DEBUG: FieldServiceManager - Đã map ${mappedServices.size} services từ Firebase cho sân $fieldId")
        } else {
            // Tạo mẫu trống nếu không có dữ liệu
            services = createEmptyServiceTemplate()
            println("⚠️ DEBUG: FieldServiceManager - Không có dữ liệu cho sân $fieldId, tạo mẫu trống")
        }
    }
    
    // Hiển thị thông báo thành công
    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            println("✅ DEBUG: FieldServiceManager - Firebase trả về thành công: $success")
            // Reload data từ Firebase để hiển thị dữ liệu mới
            refreshTrigger++
        }
    }
    
    // Hiển thị thông báo lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            println("❌ DEBUG: FieldServiceManager - Firebase trả về lỗi: $error")
            validationErrors = listOf("Lỗi Firebase: $error")
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "DỊCH VỤ BỔ SUNG",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Hiển thị validation errors
        if (validationErrors.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Vui lòng sửa các lỗi sau:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    validationErrors.forEach { error ->
                        Text(
                            text = "• $error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Danh sách dịch vụ theo 3 danh mục
        val serviceCategories = listOf("Nước đóng chai", "Thuê dụng cụ", "Dịch vụ khác")
        
        serviceCategories.forEach { category ->
            val categoryServices = services.filter { it.category == category }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header danh mục
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Hiển thị danh sách dịch vụ hiện có
                    if (categoryServices.isNotEmpty()) {
                        categoryServices.forEach { service ->
                            ServiceRow(
                                service = service,
                                isEditMode = isEditMode,
                                onServiceUpdated = { updatedService ->
                                    val index = services.indexOf(service)
                                    if (index != -1) {
                                        val updatedServices = services.toMutableList()
                                        updatedServices[index] = updatedService
                                        services = updatedServices
                                    }
                                },
                                onServiceDeleted = {
                                    services = services.filter { it != service }
                                }
                            )
                            
                            if (isEditMode) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    // Row để thêm dịch vụ mới (chỉ hiển thị trong edit mode)
                    if (isEditMode) {
                        AddServiceRow(
                            category = category,
                            onServiceAdded = { newService ->
                                services = services + newService
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Nút lưu (chỉ hiển thị trong edit mode)
        if (isEditMode) {
            Button(
                onClick = {
                    println("💾 DEBUG: FieldServiceManager - Save button được click!")
                    val errors = validateServices(services)
                    if (errors.isEmpty()) {
                        saveFieldServices(fieldId, services, fieldViewModel)
                        validationErrors = emptyList()
                    } else {
                        validationErrors = errors
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Lưu Dịch Vụ", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

/**
 * Composable hiển thị một dòng dịch vụ
 */
@Composable
private fun ServiceRow(
    service: FieldServiceItem,
    isEditMode: Boolean,
    onServiceUpdated: (FieldServiceItem) -> Unit,
    onServiceDeleted: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditMode) {
            // Edit mode: TextField để chỉnh sửa
                                                    BasicTextField(
                                            value = service.name,
                                            onValueChange = { newName ->
                                                onServiceUpdated(service.copy(name = newName))
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(8.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
            
            Spacer(modifier = Modifier.width(8.dp))
            
                                                    BasicTextField(
                                            value = service.price,
                                            onValueChange = { newPrice ->
                                                onServiceUpdated(service.copy(price = newPrice))
                                            },
                                            modifier = Modifier
                                                .weight(0.5f)
                                                .padding(8.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Nút xóa dịch vụ
            IconButton(
                onClick = onServiceDeleted
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        } else {
            // View mode: Hiển thị thông tin dịch vụ
            Text(
                text = service.name.ifEmpty { "Chưa có dịch vụ" },
                modifier = Modifier.weight(1f),
                color = if (service.name.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = if (service.price.isNotEmpty()) "${service.price} ₫" else "",
                modifier = Modifier.weight(0.5f),
                color = if (service.price.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Composable để thêm dịch vụ mới
 */
@Composable
private fun AddServiceRow(
    category: String,
    onServiceAdded: (FieldServiceItem) -> Unit
) {
    var newServiceName by remember { mutableStateOf("") }
    var newServicePrice by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TextField tên dịch vụ mới
                                    BasicTextField(
                                value = newServiceName,
                                onValueChange = { newName ->
                                    newServiceName = newName
                                    if (newName.isNotEmpty()) {
                                        val newService = FieldServiceItem(
                                            id = System.currentTimeMillis().toString(), // Tạo ID unique
                                            name = newName,
                                            price = "",
                                            category = category,
                                            isActive = true
                                        )
                                        onServiceAdded(newService)
                                        newServiceName = "" // Reset sau khi thêm
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // TextField giá dịch vụ mới
                                    BasicTextField(
                                value = newServicePrice,
                                onValueChange = { newPrice ->
                                    newServicePrice = newPrice
                                    // Tìm service vừa thêm và cập nhật giá
                                    // Logic này sẽ được xử lý trong onServiceAdded
                                },
                                modifier = Modifier
                                    .weight(0.5f)
                                    .padding(8.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Nút thêm (placeholder để cân bằng layout)
        Box(
            modifier = Modifier
                .size(48.dp)
                .weight(0.2f)
        )
    }
}

// ==================== HELPER FUNCTIONS ====================

/**
 * Load danh sách dịch vụ từ Firebase
 */
private fun loadFieldServices(fieldId: String, fieldViewModel: FieldViewModel) {
    println("🔄 DEBUG: FieldServiceManager - Loading field services for fieldId: $fieldId")
    fieldViewModel.handleEvent(FieldEvent.LoadFieldServicesByFieldId(fieldId))
}

/**
 * Map dữ liệu từ Firebase FieldService sang UI FieldServiceItem
 */
private fun mapFirebaseServicesToUI(firebaseServices: List<FieldService>): List<FieldServiceItem> {
    println("🔄 DEBUG: FieldServiceManager - Mapping ${firebaseServices.size} Firebase services to UI")
    
    val mappedServices = firebaseServices.map { service ->
        // Ưu tiên đọc danh mục từ description (nếu có)
        val mappedCategory = if (service.description.contains("Danh mục:")) {
            val categoryStart = service.description.indexOf("Danh mục:") + "Danh mục:".length
            val category = service.description.substring(categoryStart).trim()
            println("🔄 DEBUG: FieldServiceManager - Đọc danh mục từ description: $category")
            category
        } else {
            // Fallback: Sử dụng logic cũ nếu không có danh mục trong description
            when (service.billingType) {
                "PER_UNIT" -> when {
                    service.name.contains("Nước", ignoreCase = true) || 
                    service.name.contains("Sting", ignoreCase = true) || 
                    service.name.contains("Revie", ignoreCase = true) ||
                    service.name.contains("RedBull", ignoreCase = true) ||
                    service.name.contains("Red Bull", ignoreCase = true) ||
                    service.name.contains("Coca", ignoreCase = true) ||
                    service.name.contains("Pepsi", ignoreCase = true) ||
                    service.name.contains("Sprite", ignoreCase = true) ||
                    service.name.contains("Fanta", ignoreCase = true) ||
                    service.name.contains("7Up", ignoreCase = true) ||
                    service.name.contains("Milo", ignoreCase = true) ||
                    service.name.contains("Trà", ignoreCase = true) ||
                    service.name.contains("Cà phê", ignoreCase = true) ||
                    service.name.contains("Coffee", ignoreCase = true) ||
                    service.name.contains("Sữa", ignoreCase = true) ||
                    service.name.contains("Milk", ignoreCase = true) -> "Nước đóng chai"
                    service.name.contains("Vợt", ignoreCase = true) || 
                    service.name.contains("Dụng cụ", ignoreCase = true) || 
                    service.name.contains("Thuê", ignoreCase = true) ||
                    service.name.contains("Bóng", ignoreCase = true) ||
                    service.name.contains("Ball", ignoreCase = true) ||
                    service.name.contains("Áo", ignoreCase = true) ||
                    service.name.contains("Quần", ignoreCase = true) ||
                    service.name.contains("Giày", ignoreCase = true) ||
                    service.name.contains("Shoe", ignoreCase = true) -> "Thuê dụng cụ"
                    else -> "Dịch vụ khác"
                }
                "FLAT_PER_BOOKING" -> "Thuê dụng cụ"
                else -> "Dịch vụ khác"
            }
        }
        
        println("🔄 DEBUG: FieldServiceManager - Mapping service: ${service.name} -> category: $mappedCategory")
        
        FieldServiceItem(
            id = service.fieldServiceId,
            name = service.name,
            price = service.price.toString(),
            category = mappedCategory,
            isActive = service.isAvailable
        )
    }
    
    println("✅ DEBUG: FieldServiceManager - Đã map ${mappedServices.size} services thành công")
    return mappedServices
}

/**
 * Tạo template dịch vụ mẫu trống cho 3 danh mục
 */
private fun createEmptyServiceTemplate(): List<FieldServiceItem> {
    println("🔧 DEBUG: FieldServiceManager - Tạo template dịch vụ mẫu trống")
    
    val templateServices = mutableListOf<FieldServiceItem>()
    
    // Nước đóng chai - Mẫu có sẵn
    templateServices.add(FieldServiceItem(
        id = "1", 
        name = "Sting", 
        price = "12000", 
        category = "Nước đóng chai",
        isActive = true
    ))
    templateServices.add(FieldServiceItem(
        id = "2", 
        name = "Revie", 
        price = "15000", 
        category = "Nước đóng chai",
        isActive = true
    ))
    templateServices.add(FieldServiceItem(
        id = "3", 
        name = "RedBull", 
        price = "25000", 
        category = "Nước đóng chai",
        isActive = true
    ))
    templateServices.add(FieldServiceItem(
        id = "4", 
        name = "Coca Cola", 
        price = "18000", 
        category = "Nước đóng chai",
        isActive = true
    ))
    templateServices.add(FieldServiceItem(
        id = "5", 
        name = "", 
        price = "", 
        category = "Nước đóng chai",
        isActive = true
    ))
    
    // Thuê dụng cụ - Mẫu trống để owner điền
    templateServices.add(FieldServiceItem(
        id = "6", 
        name = "", 
        price = "", 
        category = "Thuê dụng cụ",
        isActive = true
    ))
    templateServices.add(FieldServiceItem(
        id = "7", 
        name = "", 
        price = "", 
        category = "Thuê dụng cụ",
        isActive = true
    ))
    
    // Dịch vụ khác - Mẫu trống để owner điền
    templateServices.add(FieldServiceItem(
        id = "8", 
        name = "", 
        price = "", 
        category = "Dịch vụ khác",
        isActive = true
    ))
    templateServices.add(FieldServiceItem(
        id = "9", 
        name = "", 
        price = "", 
        category = "Dịch vụ khác",
        isActive = true
    ))
    
    println("🔧 DEBUG: FieldServiceManager - Đã tạo ${templateServices.size} template services:")
    templateServices.forEachIndexed { index, service ->
        println("  - [$index] $service")
    }
    
    return templateServices
}

/**
 * Lưu danh sách dịch vụ vào Firebase
 */
private fun saveFieldServices(
    fieldId: String,
    services: List<FieldServiceItem>,
    fieldViewModel: FieldViewModel
) {
    println("💾 DEBUG: FieldServiceManager - Bắt đầu lưu dịch vụ vào Firebase")
    println("📊 Input services: ${services.size} items")
    println("🏟️ Field ID: $fieldId")
    
    // Lọc chỉ những service có tên và giá
    val servicesToSave = services.filter { 
        it.name.isNotEmpty() && it.price.isNotEmpty() && it.isActive 
    }
    
    println("💾 DEBUG: FieldServiceManager - Services sẽ lưu: ${servicesToSave.size} items")
    
    // Chuyển đổi sang Firebase FieldService
    val newFieldServices = servicesToSave.map { service ->
        FieldService(
            fieldServiceId = service.id.ifEmpty { "" }, // Sử dụng ID hiện tại nếu có
            fieldId = fieldId,
            name = service.name,
            price = service.price.toLongOrNull() ?: 0L,
            billingType = when (service.category) {
                "Nước đóng chai" -> "PER_UNIT"
                "Thuê dụng cụ" -> "FLAT_PER_BOOKING"
                "Dịch vụ khác" -> "PER_UNIT"
                else -> {}
            }.toString(),
            allowQuantity = true,
            description = "Dịch vụ: ${service.name} - Danh mục: ${service.category}", // Lưu danh mục vào description
            isAvailable = service.isActive
        )
    }
    
    // Debug: Kiểm tra dữ liệu trước khi gửi
    newFieldServices.forEachIndexed { index, service ->
        println("  [$index] FieldService:")
        println("    - fieldServiceId: ${service.fieldServiceId}")
        println("    - fieldId: ${service.fieldId}")
        println("    - name: ${service.name}")
        println("    - price: ${service.price}")
        println("    - billingType: ${service.billingType}")
        println("    - description: ${service.description}")
    }
    
    // Gửi lệnh lưu vào Firebase
    fieldViewModel.handleEvent(FieldEvent.UpdateFieldServices(fieldId, newFieldServices))
    
    println("✅ DEBUG: FieldServiceManager - Đã gửi lệnh lưu dịch vụ vào Firebase cho field: $fieldId")
}

/**
 * Validate danh sách dịch vụ trước khi lưu
 */
private fun validateServices(services: List<FieldServiceItem>): List<String> {
    val errors = mutableListOf<String>()
    
    // Validate chỉ những service có tên và đang active
    val servicesWithName = services.filter { it.name.isNotEmpty() && it.isActive }
    
    servicesWithName.forEach { service ->
        if (service.price.isEmpty()) {
            errors.add("Giá không được để trống cho dịch vụ: ${service.name}")
        } else if (service.price.toLongOrNull() == null) {
            errors.add("Giá không hợp lệ cho dịch vụ ${service.name}: ${service.price}")
        } else if (service.price.toLong() <= 0) {
            errors.add("Giá phải lớn hơn 0 cho dịch vụ: ${service.name}")
        }
    }
    
    // Kiểm tra xem có ít nhất một dịch vụ không
    if (servicesWithName.isEmpty()) {
        errors.add("Vui lòng nhập ít nhất một dịch vụ")
    }
    
    return errors
}
