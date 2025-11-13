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
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerMatchDetailScreen(
    matchId: String,
    navController: NavController,
    onRestoreBars: (() -> Unit)? = null,
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
    
    // State cho việc chọn đội thắng và tỉ số
    var selectedWinner by remember { mutableStateOf<String?>(null) }
    var renterAScore by remember { mutableStateOf(0) }
    var renterBScore by remember { mutableStateOf(0) }
    var renterANote by remember { mutableStateOf("") }
    var renterBNote by remember { mutableStateOf("") }
    var isSavingResult by remember { mutableStateOf(false) }
    var existingResult by remember { mutableStateOf<MatchResult?>(null) }
    
    // Tính toán trạng thái hòa
    val isDraw = renterAScore == renterBScore && renterAScore >= 0
    
    // Hàm validation tỉ số và trạng thái
    fun validateScoreAndStatus(side: String): Boolean {
        return when {
            isDraw -> true // Hòa thì luôn hợp lệ (bao gồm cả 0-0)
            side == "A" -> renterAScore > renterBScore // A thắng khi tỉ số A > B
            side == "B" -> renterBScore > renterAScore // B thắng khi tỉ số B > A
            else -> false
        }
    }
    
    // Load và lắng nghe realtime theo matchId
    DisposableEffect(matchId) {
        isLoading = true
        var registration = bookingRepo.listenMatchById(
            matchId = matchId,
            onChange = { matchData ->
                match = matchData
                if (matchData != null) {
                    // ✅ Load notes từ mảng notes[0]=A, notes[1]=B
                    renterANote = matchData.notes.getOrNull(0).orEmpty()
                    renterBNote = matchData.notes.getOrNull(1).orEmpty()
                    println("🔍 DEBUG: ========== OwnerMatchDetailScreen - Match data loaded ==========")
                    println("🔍 DEBUG: Match ID: ${matchData.rangeKey}")
                    println("🔍 DEBUG: Match status: ${matchData.status}")
                    println("🔍 DEBUG: Loaded notes from match:")
                    println("  - noteA: '$renterANote' (Renter A - người đặt đầu tiên)")
                    println("  - noteB: '$renterBNote' (Renter B - đối thủ match vào)")
                    println("  - notes array: A='${matchData.notes.getOrNull(0)}', B='${matchData.notes.getOrNull(1)}'")
                    val aCount = matchData.serviceLinesBySide["A"]?.size ?: 0
                    val bCount = matchData.serviceLinesBySide["B"]?.size ?: 0
                    println("  - serviceLinesBySide[A] count: ${aCount}")
                    println("  - serviceLinesBySide[B] count: ${bCount}")
                    println("  - participants count: ${matchData.participants.size}")
                    matchData.participants.forEachIndexed { index, p ->
                        println("    [$index] side: ${p.side}, renterId: ${p.renterId}, bookingId: ${p.bookingId}")
                    }
                    // ✅ DEBUG: Log chi tiết serviceLinesBySide["B"]
                    val bServicesDbg = matchData.serviceLinesBySide["B"].orEmpty()
                    if (bServicesDbg.isNotEmpty()) {
                        println("✅ DEBUG: serviceLinesBySide['B'] details:")
                        bServicesDbg.forEachIndexed { index, service ->
                            println("  [$index] serviceId='${service.serviceId}', name='${service.name}', qty=${service.quantity}, price=${service.price}, total=${service.lineTotal}")
                        }
                    } else {
                        println("⚠️ DEBUG: serviceLinesBySide['B'] is EMPTY")
                    }
                    println("🔍 DEBUG: =================================================================")
                    
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
                            renterAScore = existingResult?.renterAScore ?: 0
                            renterBScore = existingResult?.renterBScore ?: 0
                            // Load trạng thái hòa
                            if (existingResult?.isDraw == true) {
                                selectedWinner = "DRAW"
                            }
                        }
                    }
                    // ✅ Load participants và bookings khi thay đổi match
                    // - Renter A: lấy serviceLines từ Booking A (bookingId từ participant)
                    // - Renter B: lấy serviceLines từ Match.serviceLinesB (KHÔNG có Booking B)
                    matchData.participants.forEach { participant ->
                        println("🔍 DEBUG: Loading participant - side: ${participant.side}, renterId: ${participant.renterId}, bookingId: ${participant.bookingId}")
                        
                        // Load User info (tên, email, phone)
                        userRepo.getUserById(participant.renterId,
                            onSuccess = { user ->
                                println("✅ DEBUG: Loaded user for side ${participant.side}: ${user.name}")
                                if (participant.side == "A") {
                                    renterA = user
                                } else {
                                    renterB = user
                                }
                            },
                            onError = { e -> 
                                println("❌ ERROR: Failed to load user for side ${participant.side}: ${e.message}")
                            }
                        )
                        
                        // ✅ FIX: Renter A - Lấy serviceLines từ Match.serviceLinesA (lưu trực tiếp vào Match)
                        // ✅ FIX: Renter B - Lấy serviceLines từ Match.serviceLinesB (không có Booking B)
                        if (participant.side == "A") {
                            // ✅ FIX: Renter A - Lấy serviceLines từ Match.serviceLinesA
                        val aServices = matchData.serviceLinesBySide["A"] ?: emptyList()
                        println("✅ DEBUG: Renter A - serviceLines from arrays/legacy:")
                        println("  - count: ${aServices.size}")
                        aServices.forEachIndexed { index, service ->
                                println("    [$index] ${service.name} (id: ${service.serviceId}): qty=${service.quantity}, price=${service.price}, total=${service.lineTotal}")
                            }
                            // Vẫn load Booking A để lấy thông tin khác nếu cần (nhưng serviceLines lấy từ Match)
                            scope.launch {
                                val bookingResult = bookingRepo.getBookingById(participant.bookingId)
                                bookingResult.onSuccess { booking ->
                                    if (booking != null) {
                                        println("✅ DEBUG: Loaded booking for Renter A:")
                                        println("  - bookingId: ${booking.bookingId}")
                                        bookingA = booking
                                    } else {
                                        println("⚠️ WARNING: Booking not found for Renter A, bookingId: ${participant.bookingId}")
                                    }
                                }
                                bookingResult.onFailure { e ->
                                    println("❌ ERROR: Failed to load booking for Renter A: ${e.message}")
                                }
                            }
                        } else {
                        // ✅ FIX: Renter B - Lấy serviceLines từ map side B hoặc legacy serviceLinesB
                        val bServices = matchData.serviceLinesBySide["B"] ?: emptyList()
                        println("✅ DEBUG: Renter B - serviceLines from arrays/legacy:")
                        println("  - count: ${bServices.size}")
                        bServices.forEachIndexed { index, service ->
                                println("    [$index] ${service.name} (id: ${service.serviceId}): qty=${service.quantity}, price=${service.price}, total=${service.lineTotal}")
                            }
                            // Renter B không có Booking, không cần load bookingB
                            bookingB = null
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
                    IconButton(onClick = { 
                        navController.popBackStack() 
                    }) {
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
                LoadingDialog(message = "Đang tải thông tin trận đấu...")
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Không thể kết nối đến server",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vui lòng kiểm tra kết nối internet và thử lại",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { 
                                isLoading = true
                                error = null
                                // Retry loading
                                bookingRepo.getMatchById(
                                    matchId = matchId,
                                    onSuccess = { matchData ->
                                        match = matchData
                                        isLoading = false
                                    },
                                    onError = { e ->
                                        error = e.message ?: "Lỗi kết nối"
                                        isLoading = false
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Thử lại")
                        }
                    }
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
                                    // Kiểm tra validation tỉ số
                                    if (validateScoreAndStatus("A")) {
                                        if (isDraw) {
                                            selectedWinner = "DRAW" // Trạng thái hòa
                                        } else {
                                            selectedWinner = if (selectedWinner == "A") null else "A"
                                        }
                                    } else {
                                        toastMessage = "Tỉ số và trạng thái không trùng khớp"
                                    }
                                } else {
                                    toastMessage = if (!isMatchFinished) "Trận đấu chưa kết thúc" else "Kết quả đã được lưu, không thể thay đổi"
                                }
                            },
                            score = renterAScore,
                            onScoreChanged = { newScore ->
                                if (isMatchFinished && existingResult == null) {
                                    renterAScore = newScore
                                    // Reset selection khi thay đổi tỉ số
                                    selectedWinner = null
                                }
                            },
                            opponentScore = renterBScore,
                            isDraw = isDraw,
                            renterNote = renterANote,
                            onNoteChanged = { newNote ->
                                if (isMatchFinished && existingResult == null) {
                                    renterANote = newNote
                                }
                            },
                            serviceLines = run {
                                val services = match?.serviceLinesBySide?.get("A") ?: emptyList()
                                println("🔍 DEBUG: RenterInfoCard A - serviceLines count: ${services.size}")
                                services
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Renter B
                    val hasParticipantB = match?.participants?.any { it.side == "B" } == true
                    if (renterB != null) {
                        RenterInfoCard(
                            renter = renterB!!,
                            side = "B", 
                            isSelected = selectedWinner == "B",
                            isMatchFinished = isMatchFinished,
                            onWinnerSelected = { 
                                if (isMatchFinished && existingResult == null) {
                                    if (validateScoreAndStatus("B")) {
                                        if (isDraw) {
                                            selectedWinner = "DRAW"
                                        } else {
                                            selectedWinner = if (selectedWinner == "B") null else "B"
                                        }
                                    } else {
                                        toastMessage = "Tỉ số và trạng thái không trùng khớp"
                                    }
                                } else {
                                    toastMessage = if (!isMatchFinished) "Trận đấu chưa kết thúc" else "Kết quả đã được lưu, không thể thay đổi"
                                }
                            },
                            score = renterBScore,
                            onScoreChanged = { newScore ->
                                if (isMatchFinished && existingResult == null) {
                                    renterBScore = newScore
                                    selectedWinner = null
                                }
                            },
                            opponentScore = renterAScore,
                            isDraw = isDraw,
                            renterNote = renterBNote,
                            onNoteChanged = { newNote ->
                                if (isMatchFinished && existingResult == null) {
                                    renterBNote = newNote
                                }
                            },
                            serviceLines = run {
                                val services = match?.serviceLinesBySide?.get("B") ?: emptyList()
                                println("🔍 DEBUG: RenterInfoCard B - serviceLines count: ${services.size}")
                                services
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (hasParticipantB) {
                        // ✅ Hiển thị placeholder nếu profile Renter B chưa load nhưng dữ liệu match đã có
                        val placeholderUser = com.trungkien.fbtp_cn.model.User(
                            userId = match?.participants?.firstOrNull { it.side == "B" }?.renterId ?: "",
                            name = "Renter B",
                            email = "",
                            phone = ""
                        )
                        println("⚠️ DEBUG: Renter B profile not loaded yet - showing placeholder with notes/services from match")
                        RenterInfoCard(
                            renter = placeholderUser,
                            side = "B", 
                            isSelected = selectedWinner == "B",
                            isMatchFinished = isMatchFinished,
                            onWinnerSelected = { 
                                if (isMatchFinished && existingResult == null) {
                                    if (validateScoreAndStatus("B")) {
                                        if (isDraw) {
                                            selectedWinner = "DRAW"
                                        } else {
                                            selectedWinner = if (selectedWinner == "B") null else "B"
                                        }
                                    } else {
                                        toastMessage = "Tỉ số và trạng thái không trùng khớp"
                                    }
                                } else {
                                    toastMessage = if (!isMatchFinished) "Trận đấu chưa kết thúc" else "Kết quả đã được lưu, không thể thay đổi"
                                }
                            },
                            score = renterBScore,
                            onScoreChanged = { newScore ->
                                if (isMatchFinished && existingResult == null) {
                                    renterBScore = newScore
                                    selectedWinner = null
                                }
                            },
                            opponentScore = renterAScore,
                            isDraw = isDraw,
                            renterNote = renterBNote,
                            onNoteChanged = { newNote ->
                                if (isMatchFinished && existingResult == null) {
                                    renterBNote = newNote
                                }
                            },
                            serviceLines = run {
                                val services = match?.serviceLinesBySide?.get("B") ?: emptyList()
                                println("🔍 DEBUG: RenterInfoCard B (placeholder) - serviceLines count: ${services.size}")
                                services
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
                                // ✅ FIX: Lưu giá trị vào biến local để tránh race condition
                                val currentMatch = match
                                val currentWinner = selectedWinner
                                val currentRenterA = renterA
                                val currentRenterB = renterB
                                
                                if (currentMatch != null && currentWinner != null) {
                                    isSavingResult = true
                                    scope.launch {
                                        try {
                                            // Lưu notes trước
                                            val notesResult = bookingRepo.updateMatchNotes(
                                                matchId = currentMatch.rangeKey,
                                                noteA = renterANote.ifBlank { null },
                                                noteB = renterBNote.ifBlank { null }
                                            )
                                            
                                            if (notesResult.isSuccess) {
                                                // Sau đó lưu kết quả
                                                val result = saveMatchResult(
                                                    match = currentMatch,
                                                    winnerSide = currentWinner,
                                                    renterA = currentRenterA,
                                                    renterB = currentRenterB,
                                                    renterAScore = renterAScore,
                                                    renterBScore = renterBScore,
                                                    isDraw = isDraw,
                                                    bookingRepo = bookingRepo
                                                )
                                                isSavingResult = false
                                                if (result.isSuccess) {
                                                    toastMessage = "Lưu kết quả trận đấu thành công"
                                                    existingResult = MatchResult(
                                                        resultId = "", // không cần dùng lại
                                                        matchId = currentMatch.rangeKey,
                                                        fieldId = currentMatch.fieldId,
                                                        date = currentMatch.date,
                                                        startAt = currentMatch.startAt,
                                                        endAt = currentMatch.endAt,
                                                        winnerSide = currentWinner,
                                                        renterAScore = renterAScore,
                                                        renterBScore = renterBScore,
                                                        isDraw = isDraw
                                                    )
                                                    navController.popBackStack()
                                                } else {
                                                    toastMessage = "Lỗi: ${result.exceptionOrNull()?.message ?: "Không thể lưu kết quả"}"
                                                }
                                            } else {
                                                isSavingResult = false
                                                toastMessage = "Lỗi: ${notesResult.exceptionOrNull()?.message ?: "Không thể lưu ghi chú"}"
                                            }
                                        } catch (e: Exception) {
                                            isSavingResult = false
                                            toastMessage = "Lỗi: ${e.message ?: "Đã xảy ra lỗi không xác định"}"
                                        }
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

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = msg)
            toastMessage = null
        }
    }
    
    // Loading Dialog cho việc lưu kết quả
    if (isSavingResult) {
        LoadingDialog(
            message = "Đang lưu kết quả trận đấu...",
            onDismiss = {
                // Cho phép hủy save và back
                isSavingResult = false
            }
        )
    }
    
    // Restore bars khi back
    DisposableEffect(Unit) {
        onDispose {
            // Chỉ restore bars nếu không có navigation đang diễn ra
            onRestoreBars?.invoke()
        }
    }
}

private suspend fun saveMatchResult(
    match: Match,
    winnerSide: String,
    renterA: User?,
    renterB: User?,
    renterAScore: Int,
    renterBScore: Int,
    isDraw: Boolean,
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
        winnerSide = if (isDraw) "DRAW" else winnerSide,
        winnerRenterId = if (isDraw) null else winnerRenter?.userId,
        winnerName = if (isDraw) null else winnerRenter?.name,
        winnerPhone = if (isDraw) null else winnerRenter?.phone,
        winnerEmail = if (isDraw) null else winnerRenter?.email,
        loserSide = if (isDraw) null else loserSide,
        loserRenterId = if (isDraw) null else loserRenter?.userId,
        loserName = if (isDraw) null else loserRenter?.name,
        loserPhone = if (isDraw) null else loserRenter?.phone,
        loserEmail = if (isDraw) null else loserRenter?.email,
        matchType = match.matchType,
        totalPrice = match.totalPrice,
        notes = null, // ✅ FIX: Match không còn field notes, chỉ dùng noteA/noteB riêng
        renterAScore = renterAScore,
        renterBScore = renterBScore,
        isDraw = isDraw,
        recordedBy = "current_user_id" // TODO: Lấy từ AuthViewModel
    )
    
    // Lưu vào Firestore
    return bookingRepo.saveMatchResult(matchResult)
}
