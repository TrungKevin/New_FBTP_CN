package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.repository.BookingRepository
import com.trungkien.fbtp_cn.repository.FieldRepository
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.ui.components.owner.match.BookingInfoCard
import com.trungkien.fbtp_cn.ui.components.owner.match.RenterInfoCard
import com.trungkien.fbtp_cn.ui.components.owner.match.MatchResultNoteCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerMatchDetailScreen(
    matchId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bookingRepo = remember { BookingRepository() }
    val fieldRepo = remember { FieldRepository() }
    val userRepo = remember { UserRepository() }
    
    var match by remember { mutableStateOf<Match?>(null) }
    var field by remember { mutableStateOf<Field?>(null) }
    var renterA by remember { mutableStateOf<User?>(null) }
    var renterB by remember { mutableStateOf<User?>(null) }
    var bookingA by remember { mutableStateOf<Booking?>(null) }
    var bookingB by remember { mutableStateOf<Booking?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    // State cho việc chọn đội thắng
    var selectedWinner by remember { mutableStateOf<String?>(null) }
    var isSavingResult by remember { mutableStateOf(false) }
    var existingResult by remember { mutableStateOf<MatchResult?>(null) }
    
    // Load và lắng nghe realtime theo matchId
    DisposableEffect(matchId) {
        isLoading = true
        var registration = bookingRepo.listenMatchById(
            matchId = matchId,
            onChange = { matchData ->
                match = matchData
                if (matchData != null) {
                    // Load field khi thay đổi match
                    scope.launch {
                        fieldRepo.getFieldById(matchData.fieldId).onSuccess { fieldData ->
                            field = fieldData
                        }
                    }
                    // Load kết quả nếu đã có để chặn lưu lại
                    scope.launch {
                        val res = bookingRepo.getMatchResult(matchData.rangeKey)
                        if (res.isSuccess) {
                            existingResult = res.getOrNull()
                            selectedWinner = existingResult?.winnerSide
                        }
                    }
                    // Load participants khi thay đổi match
                    matchData.participants.forEach { participant ->
                        userRepo.getUserById(participant.renterId,
                            onSuccess = { user ->
                                if (participant.side == "A") {
                                    renterA = user
                                } else {
                                    renterB = user
                                }
                            },
                            onError = { /* ignore */ }
                        )
                        scope.launch {
                            bookingRepo.getBookingById(participant.bookingId).onSuccess { booking ->
                                if (participant.side == "A") {
                                    bookingA = booking
                                } else {
                                    bookingB = booking
                                }
                            }
                        }
                    }
                }
                isLoading = false
            },
            onError = { e ->
                error = e.message
                isLoading = false
            }
        )
        onDispose {
            try { registration.remove() } catch (_: Exception) {}
        }
    }
    
    // Kiểm tra trận đấu đã kết thúc chưa
    val isMatchFinished = match?.let { matchData ->
        try {
            val matchDate = LocalDate.parse(matchData.date)
            val endTime = LocalTime.parse(matchData.endAt)
            val today = LocalDate.now()
            val now = LocalTime.now()
            
            matchDate.isBefore(today) || (matchDate.isEqual(today) && endTime.isBefore(now))
        } catch (_: Exception) { false }
    } ?: false
    
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Chi tiết trận đấu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lỗi: $error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            match == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy trận đấu",
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Thông tin đặt sân
                    BookingInfoCard(
                        field = field,
                        match = match!!,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Thông tin người đặt (đồng bộ style với BookingDetailManage)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Thông tin người đặt sân",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Renter A
                    renterA?.let { user ->
                        RenterInfoCard(
                            renter = user,
                            side = "A",
                            isSelected = selectedWinner == "A",
                            isMatchFinished = isMatchFinished,
                            onWinnerSelected = { 
                                if (isMatchFinished && existingResult == null) {
                                    selectedWinner = if (selectedWinner == "A") null else "A"
                                } else {
                                    toastMessage = if (!isMatchFinished) "Trận đấu chưa kết thúc" else "Kết quả đã được lưu, không thể thay đổi"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Renter B
                    renterB?.let { user ->
                        RenterInfoCard(
                            renter = user,
                            side = "B", 
                            isSelected = selectedWinner == "B",
                            isMatchFinished = isMatchFinished,
                            onWinnerSelected = { 
                                if (isMatchFinished && existingResult == null) {
                                    selectedWinner = if (selectedWinner == "B") null else "B"
                                } else {
                                    toastMessage = if (!isMatchFinished) "Trận đấu chưa kết thúc" else "Kết quả đã được lưu, không thể thay đổi"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Lưu ý
                    if (isMatchFinished) {
                        MatchResultNoteCard(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Nút lưu thông tin
                    Button(
                        onClick = {
                            if (!isMatchFinished) {
                                toastMessage = "Trận đấu chưa kết thúc"
                                return@Button
                            }
                            if (existingResult != null) {
                                toastMessage = "Kết quả đã được lưu trước đó"
                                return@Button
                            }
                            if (selectedWinner != null && match != null) {
                                isSavingResult = true
                                scope.launch {
                                    val result = saveMatchResult(
                                        match = match!!,
                                        winnerSide = selectedWinner!!,
                                        renterA = renterA,
                                        renterB = renterB,
                                        bookingRepo = bookingRepo
                                    )
                                    isSavingResult = false
                                    if (result.isSuccess) {
                                        toastMessage = "Lưu kết quả trận đấu thành công"
                                        existingResult = MatchResult(
                                            resultId = "", // không cần dùng lại
                                            matchId = match!!.rangeKey,
                                            fieldId = match!!.fieldId,
                                            date = match!!.date,
                                            startAt = match!!.startAt,
                                            endAt = match!!.endAt,
                                            winnerSide = selectedWinner
                                        )
                                        navController.popBackStack()
                                    } else {
                                        toastMessage = "Lỗi: ${result.exceptionOrNull()?.message ?: "Không thể lưu kết quả"}"
                                    }
                                }
                            }
                        },
                        enabled = selectedWinner != null && !isSavingResult && existingResult == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSavingResult) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Lưu thông tin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = msg)
            toastMessage = null
        }
    }
}

private suspend fun saveMatchResult(
    match: Match,
    winnerSide: String,
    renterA: User?,
    renterB: User?,
    bookingRepo: BookingRepository
): Result<Unit> {
    val loserSide = if (winnerSide == "A") "B" else "A"
    val winnerRenter = if (winnerSide == "A") renterA else renterB
    val loserRenter = if (winnerSide == "A") renterB else renterA
    
    val matchResult = MatchResult(
        resultId = "result_${match.rangeKey}_${System.currentTimeMillis()}",
        matchId = match.rangeKey,
        fieldId = match.fieldId,
        date = match.date,
        startAt = match.startAt,
        endAt = match.endAt,
        winnerSide = winnerSide,
        winnerRenterId = winnerRenter?.userId,
        winnerName = winnerRenter?.name,
        winnerPhone = winnerRenter?.phone,
        winnerEmail = winnerRenter?.email,
        loserSide = loserSide,
        loserRenterId = loserRenter?.userId,
        loserName = loserRenter?.name,
        loserPhone = loserRenter?.phone,
        loserEmail = loserRenter?.email,
        matchType = match.matchType,
        totalPrice = match.totalPrice,
        notes = match.notes,
        recordedBy = "current_user_id" // TODO: Lấy từ AuthViewModel
    )
    
    // Lưu vào Firestore
    return bookingRepo.saveMatchResult(matchResult)
}
