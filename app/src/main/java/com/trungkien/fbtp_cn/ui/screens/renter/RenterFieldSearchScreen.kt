package com.trungkien.fbtp_cn.ui.screens.renter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.components.renter.fieldsearch.RenterFieldTypeFilter
import com.trungkien.fbtp_cn.ui.components.renter.fieldsearch.RenterSearchHeader
import com.trungkien.fbtp_cn.ui.components.renter.fieldsearch.RenterSearchResultsList
import com.trungkien.fbtp_cn.ui.components.renter.fieldsearch.SearchResultField
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.model.User

@Composable
fun RenterFieldSearchScreen(
    modifier: Modifier = Modifier,
    onBookClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }

    val fieldViewModel: FieldViewModel = viewModel()
    val uiState = fieldViewModel.uiState.collectAsState().value
    LaunchedEffect(Unit) { 
        fieldViewModel.handleEvent(FieldEvent.LoadAllFields)
    }

    // State để lưu thông tin owner cho mỗi field
    var ownerInfoMap by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    val userRepository = UserRepository()
    
    // State để lưu pricing rules cho tất cả fields
    var fieldPricingMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    // Load pricing rules for all fields - improved approach
    LaunchedEffect(uiState.allFields) {
        if (uiState.allFields.isNotEmpty()) {
            println("🔄 DEBUG: Loading pricing rules for ${uiState.allFields.size} fields")
            val newPricingMap = mutableMapOf<String, Long>()
            
            // Load pricing rules for each field
            uiState.allFields.forEach { field ->
                fieldViewModel.handleEvent(FieldEvent.LoadPricingRulesByFieldId(field.fieldId))
                // Small delay to ensure pricing rules are loaded
                kotlinx.coroutines.delay(50)
                
                // Get pricing rules from current uiState
                val pricingRules = fieldViewModel.uiState.value.pricingRules
                val fieldPrice = pricingRules.firstOrNull()?.price ?: 0L
                newPricingMap[field.fieldId] = fieldPrice
                
                println("🔄 DEBUG: Field ${field.name} - price: $fieldPrice")
            }
            
            fieldPricingMap = newPricingMap.toMap()
            println("✅ DEBUG: Loaded pricing for ${fieldPricingMap.size} fields")
        }
    }

    // Load owner info for all fields - sử dụng getCurrentUserProfile thay vì getUserById
    LaunchedEffect(uiState.allFields) {
        val ownerIds = uiState.allFields.map { it.ownerId }.distinct().filter { it.isNotBlank() }
        println("🔄 DEBUG: Loading owner info for ${ownerIds.size} owners: $ownerIds")
        
        val newOwnerInfoMap = mutableMapOf<String, User>()
        
        if (ownerIds.isEmpty()) {
            ownerInfoMap = emptyMap()
            return@LaunchedEffect
        }
        
        // Fetch owner data thực từ Firebase (sau khi cập nhật security rules)
        var completedCount = 0
        val totalCount = ownerIds.size
        
        if (totalCount == 0) {
            ownerInfoMap = emptyMap()
            return@LaunchedEffect
        }
        
        ownerIds.forEach { ownerId ->
            userRepository.getUserById(
                userId = ownerId,
                onSuccess = { user ->
                    println("✅ DEBUG: Successfully loaded owner $ownerId: ${user.name}")
                    println("✅ DEBUG: Owner avatarUrl: ${user.avatarUrl?.take(50)}...")
                    newOwnerInfoMap[ownerId] = user
                    completedCount++
                    
                    // Update state after each successful fetch
                    ownerInfoMap = newOwnerInfoMap.toMap()
                    
                    if (completedCount == totalCount) {
                        println("✅ DEBUG: All owners loaded successfully!")
                    }
                },
                onError = { error ->
                    println("❌ DEBUG: Error loading owner info for $ownerId: ${error.message}")
                    
                    // Fallback: tạo dummy owner data nếu không fetch được
                    val dummyOwner = User(
                        userId = ownerId,
                        role = "OWNER",
                        name = "Chủ sân",
                        email = "",
                        phone = "",
                        avatarUrl = "",
                        address = "",
                        dateOfBirth = "",
                        gender = "",
                        isVerified = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    newOwnerInfoMap[ownerId] = dummyOwner
                    completedCount++
                    
                    // Update state after each attempt
                    ownerInfoMap = newOwnerInfoMap.toMap()
                    
                    if (completedCount == totalCount) {
                        println("✅ DEBUG: All owner fetch attempts completed!")
                    }
                }
            )
        }
    }

    val results = remember(uiState.allFields, selectedType, searchQuery, uiState.pricingRules, ownerInfoMap, fieldPricingMap) {
        uiState.allFields
            .filter { f ->
                val matchType = selectedType == null || f.sports.any { it.equals(selectedType, true) }
                val matchQuery = searchQuery.isBlank() || f.name.contains(searchQuery, true) || f.address.contains(searchQuery, true)
                matchType && matchQuery
            }
            .map { f ->
                val owner = ownerInfoMap[f.ownerId]
                val fieldPrice = fieldPricingMap[f.fieldId] ?: 0L
                val priceText = if (fieldPrice > 0) "${String.format("%,d", fieldPrice)} VND/giờ" else "Liên hệ"
                
                // Debug logs
                println("🔄 DEBUG: Field ${f.name}")
                println("🔄 DEBUG: - field.ownerId: ${f.ownerId}")
                println("🔄 DEBUG: - fieldPrice: $fieldPrice")
                println("🔄 DEBUG: - priceText: $priceText")
                println("🔄 DEBUG: - mainImage: ${f.images.mainImage.take(50)}...")
                println("🔄 DEBUG: - owner: ${owner?.name}")
                println("🔄 DEBUG: - owner?.userId: ${owner?.userId}")
                println("🔄 DEBUG: - ownerAvatarUrl: ${owner?.avatarUrl?.take(50)}...")
                println("🔄 DEBUG: - ownerAvatarUrl length: ${owner?.avatarUrl?.length}")
                println("🔄 DEBUG: - ownerInfoMap keys: ${ownerInfoMap.keys}")
                println("🔄 DEBUG: - ownerInfoMap size: ${ownerInfoMap.size}")
                
                SearchResultField(
                    id = f.fieldId,
                    name = f.name,
                    type = f.sports.firstOrNull() ?: "",
                    price = priceText,
                    location = f.address,
                    rating = f.averageRating,
                    distance = "", // TODO: tính theo geo nếu cần
                    isAvailable = f.isActive,
                    imageUrl = f.images.mainImage.ifEmpty { null },
                    ownerName = owner?.name ?: "Chủ sân",
                    ownerAvatarUrl = owner?.avatarUrl,
                    ownerPhone = owner?.phone ?: "",
                    fieldImages = f.images,
                    address = f.address,
                    openHours = "${f.openHours.start} - ${f.openHours.end}",
                    amenities = f.amenities,
                    totalReviews = f.totalReviews,
                    contactPhone = f.contactPhone,
                    description = f.description
                )
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        RenterSearchHeader(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onFilterClick = { },
            onLocationClick = { },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        RenterFieldTypeFilter(
            selectedType = selectedType,
            onTypeSelected = { type -> selectedType = if (type == "all") null else type },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        RenterSearchResultsList(
            searchResults = results,
            isLoading = uiState.isLoading,
            onFieldClick = { id -> onBookClick(id) },
            onFavoriteClick = { },
            onBookClick = onBookClick,
            onLoadMore = { },
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RenterFieldSearchScreenPreview() {
    FBTP_CNTheme {
        RenterFieldSearchScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )
    }
}
