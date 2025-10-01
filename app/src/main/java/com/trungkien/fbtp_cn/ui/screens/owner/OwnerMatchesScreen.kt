package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.ListenerRegistration
import com.trungkien.fbtp_cn.model.Match
import com.trungkien.fbtp_cn.repository.BookingRepository
import com.trungkien.fbtp_cn.ui.components.owner.booking.OwnerMatchCard

@Composable
fun OwnerMatchesScreen(
    fieldId: String,
    date: String,
    modifier: Modifier = Modifier
) {
    val repo = remember { BookingRepository() }
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var listener by remember { mutableStateOf<ListenerRegistration?>(null) }

    DisposableEffect(fieldId, date) {
        listener?.remove()
        listener = repo.listenMatchesByFieldDate(
            fieldId = fieldId,
            date = date,
            onChange = { list -> matches = list.sortedWith(compareBy({ it.date }, { it.startAt })) },
            onError = { _ -> }
        )
        onDispose { listener?.remove(); listener = null }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (matches.isEmpty()) {
            Text("Chưa có trận đấu", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(matches, key = { it.rangeKey }) { m ->
                    OwnerMatchCard(match = m)
                }
            }
        }
    }
}


