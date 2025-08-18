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

@Composable
fun RenterFieldSearchScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }

    val results by remember(selectedType, searchQuery) {
        mutableStateOf(
            listOf(
                SearchResultField(
                    id = "field1",
                    name = "Sân Tennis ABC",
                    type = "Tennis",
                    price = "120k/giờ",
                    location = "Quận 1, TP.HCM",
                    rating = 4.8f,
                    distance = "2.5km",
                    isAvailable = true
                ),
                SearchResultField(
                    id = "field2",
                    name = "Sân Cầu lông XYZ",
                    type = "Badminton",
                    price = "80k/giờ",
                    location = "Quận 2, TP.HCM",
                    rating = 4.6f,
                    distance = "3.2km",
                    isAvailable = true
                ),
                SearchResultField(
                    id = "field3",
                    name = "Sân Bóng đá 5 người",
                    type = "Football",
                    price = "200k/giờ",
                    location = "Quận 3, TP.HCM",
                    rating = 4.7f,
                    distance = "1.8km",
                    isAvailable = false
                )
            ).filter { field ->
                val matchType = selectedType == null || field.type.equals(selectedType, ignoreCase = true) || selectedType == "all"
                val matchQuery = searchQuery.isBlank() || field.name.contains(searchQuery, ignoreCase = true)
                matchType && matchQuery
            }
        )
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
            isLoading = false,
            onFieldClick = { },
            onFavoriteClick = { },
            onBookClick = { /* TODO: navigate to booking flow with fieldId */ },
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
