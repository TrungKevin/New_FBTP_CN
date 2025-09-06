package com.trungkien.fbtp_cn.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.PricingRule
import com.trungkien.fbtp_cn.model.FieldService
import com.trungkien.fbtp_cn.model.Slot
import com.trungkien.fbtp_cn.model.Service
import com.trungkien.fbtp_cn.repository.FieldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.trungkien.fbtp_cn.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

data class FieldUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val uploadProgress: Map<Int, Float> = emptyMap(),
    val fields: List<Field> = emptyList(),
    val currentField: Field? = null,
    val pricingRules: List<PricingRule> = emptyList(),
    val fieldServices: List<FieldService> = emptyList(),
    val slots: List<Slot> = emptyList()
)

sealed class FieldEvent {
    data class AddField(
        val field: Field,
        val images: List<Uri>,
        val pricingRules: List<PricingRule>,
        val fieldServices: List<FieldService>
    ) : FieldEvent()
    
    data class UpdateField(val field: Field) : FieldEvent()
    data class DeleteField(val fieldId: String) : FieldEvent()
    data class LoadFieldsByOwner(val ownerId: String) : FieldEvent()
    data class LoadFieldById(val fieldId: String) : FieldEvent()
    data class LoadPricingRules(val fieldId: String) : FieldEvent()
    data class LoadFieldServices(val fieldId: String) : FieldEvent()
    data class LoadPricingRulesByFieldId(val fieldId: String) : FieldEvent()
    data class LoadFieldServicesByFieldId(val fieldId: String) : FieldEvent()
    data class LoadSlotsByFieldIdAndDate(val fieldId: String, val date: String) : FieldEvent()
    data class AddPricingRule(val pricingRule: PricingRule) : FieldEvent()
    data class AddFieldService(val fieldService: FieldService) : FieldEvent()
    data class UpdateFieldPricingAndServices(
        val fieldId: String,
        val pricingRules: List<PricingRule>,
        val fieldServices: List<FieldService>
    ) : FieldEvent()
    
    data class UpdateFieldServices(
        val fieldId: String,
        val fieldServices: List<FieldService>
    ) : FieldEvent()
    object ClearError : FieldEvent()
    object ClearSuccess : FieldEvent()
}

class FieldViewModel(
    private val repository: FieldRepository = FieldRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FieldUiState())
    val uiState: StateFlow<FieldUiState> = _uiState.asStateFlow()
    
    fun handleEvent(event: FieldEvent) {
        when (event) {
            is FieldEvent.AddField -> addField(event.field, event.images, event.pricingRules, event.fieldServices)
            is FieldEvent.UpdateField -> updateField(event.field)
            is FieldEvent.DeleteField -> deleteField(event.fieldId)
            is FieldEvent.LoadFieldsByOwner -> loadFieldsByOwner(event.ownerId)
            is FieldEvent.LoadFieldById -> loadFieldById(event.fieldId)
            is FieldEvent.LoadPricingRules -> loadPricingRules(event.fieldId)
            is FieldEvent.LoadFieldServices -> loadFieldServices(event.fieldId)
            is FieldEvent.LoadPricingRulesByFieldId -> loadPricingRulesByFieldId(event.fieldId)
            is FieldEvent.LoadFieldServicesByFieldId -> loadFieldServicesByFieldId(event.fieldId)
            is FieldEvent.LoadSlotsByFieldIdAndDate -> loadSlotsByFieldIdAndDate(event.fieldId, event.date)
            is FieldEvent.AddPricingRule -> addPricingRule(event.pricingRule)
            is FieldEvent.AddFieldService -> addFieldService(event.fieldService)
            is FieldEvent.UpdateFieldPricingAndServices -> updateFieldPricingAndServices(
                event.fieldId, 
                event.pricingRules, 
                event.fieldServices
            )
            is FieldEvent.UpdateFieldServices -> updateFieldServices(
                event.fieldId, 
                event.fieldServices
            )
            is FieldEvent.ClearError -> clearError()
            is FieldEvent.ClearSuccess -> clearSuccess()
        }
    }
    
    private fun addField(
        field: Field,
        images: List<Uri>,
        pricingRules: List<PricingRule>,
        fieldServices: List<FieldService>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = null
            )
            
            // Reset upload progress
            val initialProgress = images.indices.associateWith { 0f }
            _uiState.value = _uiState.value.copy(uploadProgress = initialProgress)
            
            try {
                val result = repository.addField(field, images, pricingRules, fieldServices)
                
                result.fold(
                    onSuccess = { fieldId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Thêm sân thành công! ID: $fieldId",
                            uploadProgress = emptyMap()
                        )
                        
                        // Reload fields list
                        field.ownerId?.let { loadFieldsByOwner(it) }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Thêm sân thất bại: ${exception.message}",
                            uploadProgress = emptyMap()
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi không xác định: ${e.message}",
                    uploadProgress = emptyMap()
                )
            }
        }
    }
    
    private fun updateField(field: Field) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = repository.updateField(field)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Cập nhật sân thành công!"
                        )
                        
                        // Reload current field
                        loadFieldById(field.fieldId)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cập nhật sân thất bại: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun deleteField(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = null
            )
            
            try {
                val result = repository.deleteField(fieldId)
                
                result.fold(
                    onSuccess = { _ ->
                        // Cập nhật local state ngay lập tức
                        val updatedFields = _uiState.value.fields.filter { it.fieldId != fieldId }
                        _uiState.value = _uiState.value.copy(
                            fields = updatedFields,
                            isLoading = false,
                            success = "Xóa sân thành công! Lịch sử đặt sân đã được giữ lại."
                        )
                        
                        // Đợi một chút để đảm bảo UI cập nhật hoàn toàn
                        delay(500)
                        
                        // Trigger reload để đồng bộ với Firebase
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            println("🔄 DEBUG: Reloading fields after deletion for user: ${currentUser.uid}")
                            handleEvent(FieldEvent.LoadFieldsByOwner(currentUser.uid))
                        } else {
                            println("❌ ERROR: Current user is null after deletion")
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Xóa sân thất bại"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun loadFieldsByOwner(ownerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = repository.getFieldsByOwnerId(ownerId)
                
                result.fold(
                    onSuccess = { fields ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            fields = fields
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Không thể tải danh sách sân: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun loadFieldById(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = repository.getFieldById(fieldId)
                
                result.fold(
                    onSuccess = { field ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentField = field
                        )
                        
                        // Load related data
                        field?.let {
                            loadPricingRules(it.fieldId)
                            loadFieldServices(it.fieldId)
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Không thể tải thông tin sân: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun loadPricingRules(fieldId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getPricingRulesByFieldId(fieldId)
                
                result.fold(
                    onSuccess = { rules ->
                        _uiState.value = _uiState.value.copy(pricingRules = rules)
                    },
                    onFailure = { exception ->
                        // Log error but don't show to user
                        println("Error loading pricing rules: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("Error loading pricing rules: ${e.message}")
            }
        }
    }
    
    private fun loadFieldServices(fieldId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getFieldServicesByFieldId(fieldId)
                
                result.fold(
                    onSuccess = { services ->
                        _uiState.value = _uiState.value.copy(fieldServices = services)
                    },
                    onFailure = { exception ->
                        // Log error but don't show to user
                        println("Error loading field services: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("Error loading field services: ${e.message}")
            }
        }
    }
    
    private fun loadPricingRulesByFieldId(fieldId: String) {
        viewModelScope.launch {
            println("🔄 DEBUG: FieldViewModel.loadPricingRulesByFieldId($fieldId)")
            try {
                val result = repository.getPricingRulesByFieldId(fieldId)
                
                result.fold(
                    onSuccess = { rules ->
                        println("✅ DEBUG: LoadPricingRulesByFieldId thành công: ${rules.size} rules")
                        rules.forEachIndexed { index, rule ->
                            println("  [$index] ruleId: '${rule.ruleId}', fieldId: '${rule.fieldId}', price: ${rule.price}, description: '${rule.description}'")
                        }
                        _uiState.value = _uiState.value.copy(pricingRules = rules)
                    },
                    onFailure = { exception ->
                        println("❌ ERROR: LoadPricingRulesByFieldId thất bại cho fieldId: $fieldId")
                        println("❌ ERROR: Exception: ${exception.message}")
                        exception.printStackTrace()
                        // Log error but don't show to user
                        println("Error loading pricing rules by field ID: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("❌ ERROR: Exception không xác định trong loadPricingRulesByFieldId: ${e.message}")
                e.printStackTrace()
                println("Error loading pricing rules by field ID: ${e.message}")
            }
        }
    }
    
    private fun loadFieldServicesByFieldId(fieldId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getFieldServicesByFieldId(fieldId)
                
                result.fold(
                    onSuccess = { services ->
                        _uiState.value = _uiState.value.copy(fieldServices = services)
                    },
                    onFailure = { exception ->
                        // Log error but don't show to user
                        println("Error loading field services by field ID: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("Error loading field services by field ID: ${e.message}")
            }
        }
    }
    
    private fun loadSlotsByFieldIdAndDate(fieldId: String, date: String) {
        viewModelScope.launch {
            println("🔄 DEBUG: FieldViewModel.loadSlotsByFieldIdAndDate($fieldId, $date)")
            try {
                val result = repository.getSlotsByFieldIdAndDate(fieldId, date)
                
                result.fold(
                    onSuccess = { slots ->
                        println("✅ DEBUG: LoadSlotsByFieldIdAndDate thành công: ${slots.size} slots")
                        slots.forEachIndexed { index, slot ->
                            println("  [$index] slotId: '${slot.slotId}', startAt: '${slot.startAt}', isBooked: ${slot.isBooked}")
                        }
                        _uiState.value = _uiState.value.copy(slots = slots)
                    },
                    onFailure = { exception ->
                        println("❌ ERROR: LoadSlotsByFieldIdAndDate thất bại cho fieldId: $fieldId, date: $date")
                        println("❌ ERROR: Exception: ${exception.message}")
                        exception.printStackTrace()
                        // Log error but don't show to user
                        println("Error loading slots by field ID and date: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("❌ ERROR: Exception không xác định trong loadSlotsByFieldIdAndDate: ${e.message}")
                e.printStackTrace()
                println("Error loading slots by field ID and date: ${e.message}")
            }
        }
    }
    
    private fun addPricingRule(pricingRule: PricingRule) {
        viewModelScope.launch {
            try {
                val result = repository.addPricingRule(pricingRule)
                
                result.fold(
                    onSuccess = { ruleId ->
                        _uiState.value = _uiState.value.copy(
                            success = "Thêm quy tắc giá thành công!"
                        )
                        // Reload pricing rules
                        pricingRule.fieldId?.let { loadPricingRulesByFieldId(it) }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Thêm quy tắc giá thất bại: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun addFieldService(fieldService: FieldService) {
        viewModelScope.launch {
            try {
                val result = repository.addFieldService(fieldService)
                
                result.fold(
                    onSuccess = { serviceId ->
                        _uiState.value = _uiState.value.copy(
                            success = "Thêm dịch vụ thành công!"
                        )
                        // Reload field services
                        fieldService.fieldId?.let { loadFieldServicesByFieldId(it) }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Thêm dịch vụ thất bại: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun updateFieldPricingAndServices(
        fieldId: String,
        pricingRules: List<PricingRule>,
        fieldServices: List<FieldService>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = null
            )
            
            try {
                val result = repository.updateFieldPricingAndServices(fieldId, pricingRules, fieldServices)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Cập nhật bảng giá và dịch vụ thành công!"
                        )
                        // Reload data để hiển thị dữ liệu mới
                        loadPricingRulesByFieldId(fieldId)
                        loadFieldServicesByFieldId(fieldId)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cập nhật thất bại: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun updateFieldServices(
        fieldId: String,
        fieldServices: List<FieldService>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = null
            )
            
            try {
                val result = repository.updateFieldServices(fieldId, fieldServices)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Cập nhật dịch vụ thành công!"
                        )
                        // Reload data để hiển thị dữ liệu mới
                        loadFieldServicesByFieldId(fieldId)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cập nhật dịch vụ thất bại: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }
    
    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = null)
    }
    
    // Helper functions for creating default pricing rules
    fun createDefaultPricingRules(
        weekdayPrice: Int,
        weekendPrice: Int
    ): List<PricingRule> {
        return listOf(
            PricingRule(
                ruleId = "",
                fieldId = "",
                dayType = "WEEKDAY",
                slots = 2,
                minutes = 60,
                price = weekdayPrice.toLong(),
                calcMode = "CEIL_TO_RULE",
                description = "Giá ngày thường - 1 giờ",
                isActive = true
            ),
            PricingRule(
                ruleId = "",
                fieldId = "",
                dayType = "WEEKEND",
                slots = 2,
                minutes = 60,
                price = weekendPrice.toLong(),
                calcMode = "CEIL_TO_RULE",
                description = "Giá cuối tuần - 1 giờ",
                isActive = true
            )
        )
    }
    
    // Helper function for creating field service from service template
    fun createFieldServiceFromTemplate(
        service: Service,
        price: Long,
        allowQuantity: Boolean = true
    ): FieldService {
        return FieldService(
            fieldServiceId = "",
            fieldId = "",
            serviceId = service.serviceId,
            name = service.name,
            price = price,
            billingType = service.defaultBillingType,
            allowQuantity = allowQuantity,
            description = service.description,
            isAvailable = true
        )
    }
}
