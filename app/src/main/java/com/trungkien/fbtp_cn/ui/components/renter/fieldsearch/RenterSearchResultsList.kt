package com.trungkien.fbtp_cn.ui.components.renter.fieldsearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun RenterSearchResultsList(
    searchResults: List<SearchResultField> = emptyList(),
    isLoading: Boolean = false,
    onFieldClick: (String) -> Unit = {},
    onFavoriteClick: (String) -> Unit = {},
    onBookClick: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isLoading && searchResults.isEmpty()) {
        // Loading state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Đang tìm kiếm sân...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else if (searchResults.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "No results",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Không tìm thấy sân nào",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Hãy thử thay đổi từ khóa tìm kiếm hoặc bộ lọc",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Results list
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Results count header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tìm thấy ${searchResults.size} sân",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Sort button
                    TextButton(
                        onClick = { /* TODO: Show sort options */ }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                         Icon(
                            painter = painterResource(id = R.drawable.filteralt),
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                            
                            Text(
                                text = "Sắp xếp",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Search result items
            items(searchResults) { field ->
                RenterSearchResultCard(
                    field = field,
                    onFieldClick = onFieldClick,
                    onFavoriteClick = onFavoriteClick,
                    onBookClick = onBookClick
                )
            }
            
            // Load more button
            if (searchResults.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Tải thêm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun RenterSearchResultsListPreview() {
    FBTP_CNTheme {
        RenterSearchResultsList(
            searchResults = listOf(
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
                )
            )
        )
    }
}

@Preview
@Composable
fun RenterSearchResultsListEmptyPreview() {
    FBTP_CNTheme {
        RenterSearchResultsList()
    }
}

@Preview
@Composable
fun RenterSearchResultsListLoadingPreview() {
    FBTP_CNTheme {
        RenterSearchResultsList(isLoading = true)
    }
}
