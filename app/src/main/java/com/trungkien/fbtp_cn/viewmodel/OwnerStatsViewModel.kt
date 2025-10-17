package com.trungkien.fbtp_cn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.trungkien.fbtp_cn.repository.OwnerStatsRepository
import com.trungkien.fbtp_cn.repository.OwnerStats

data class OwnerStatsUi(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: OwnerStats? = null,
    val range: TimeRange = TimeRange.Week
)

enum class TimeRange { Today, Week, Month, Year }

class OwnerStatsViewModel(
    private val repo: OwnerStatsRepository = OwnerStatsRepository()
) : ViewModel() {
    private val _ui = MutableStateFlow(OwnerStatsUi())
    val ui: StateFlow<OwnerStatsUi> = _ui.asStateFlow()

    fun load(ownerId: String, today: java.time.LocalDate = java.time.LocalDate.now()) {
        val range = _ui.value.range
        val (start, end, bucket) = when (range) {
            TimeRange.Today -> Triple(today, today, OwnerStatsRepository.Bucket.HOURLY)
            TimeRange.Week -> Triple(today.minusDays(6), today, OwnerStatsRepository.Bucket.DAILY)
            TimeRange.Month -> Triple(today.minusDays(29), today, OwnerStatsRepository.Bucket.DAILY)
            TimeRange.Year -> Triple(today.withDayOfYear(1), today, OwnerStatsRepository.Bucket.MONTHLY)
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            val res = repo.loadStats(ownerId, start.toString(), end.toString(), bucket)
            _ui.value = res.fold(
                onSuccess = { OwnerStatsUi(isLoading = false, stats = it, range = range) },
                onFailure = { OwnerStatsUi(isLoading = false, error = it.message, range = range) }
            )
        }
    }

    fun setRange(range: TimeRange) {
        _ui.value = _ui.value.copy(range = range)
    }
}


