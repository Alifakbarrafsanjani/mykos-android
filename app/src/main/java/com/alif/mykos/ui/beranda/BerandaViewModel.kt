package com.alif.mykos.ui.beranda

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alif.mykos.R
import com.alif.mykos.data.local.AppDatabase
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.data.repository.KamarRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

enum class FilterStatus {
    SEMUA,
    TERISI,
    KOSONG
}

enum class SortOrder(@StringRes val labelResId: Int) {
    TERBARU(R.string.sort_terbaru),
    HARGA_TERENDAH(R.string.sort_harga_terendah),
    HARGA_TERTINGGI(R.string.sort_harga_tertinggi),
    NOMOR_AZ(R.string.sort_nomor_az)
}

data class BerandaUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalKamar: Int = 0,
    val totalTerisi: Int = 0,
    val totalKosong: Int = 0,
    val estimasiPemasukan: Long = 0L,
    val displayedList: List<KamarEntity> = emptyList(),
    val occupiedKamarList: List<KamarEntity> = emptyList(),
    val isDatabaseEmpty: Boolean = true,
    val activeSort: SortOrder = SortOrder.TERBARU
)

class BerandaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KamarRepository

    private val _searchQuery = MutableStateFlow("")
    private val _filterStatus = MutableStateFlow(FilterStatus.SEMUA)
    private val _sortOrder = MutableStateFlow(SortOrder.TERBARU)
    private val _retryTrigger = MutableStateFlow(0)

    init {
        val dao = AppDatabase.getInstance(application).kamarDao()
        repository = KamarRepository(dao)
    }

    val uiState: StateFlow<BerandaUiState> = combine(
        repository.allKamar.onStart { delay(500L) },
        _searchQuery,
        _filterStatus,
        _sortOrder,
        _retryTrigger
    ) { allList, query, filter, sort, _ ->
        val total = allList.size
        val occupiedRooms = allList.filter { it.isTerisi() }.sortedBy { it.nomorKamar.lowercase() }
        val terisi = occupiedRooms.size
        val kosong = total - terisi
        val totalPemasukan = occupiedRooms.sumOf { it.hargaSewa }

        val filteredByStatus = when (filter) {
            FilterStatus.SEMUA -> allList
            FilterStatus.TERISI -> occupiedRooms
            FilterStatus.KOSONG -> allList.filter { !it.isTerisi() }
        }

        val trimmedQuery = query.trim()
        val filteredBySearch = if (trimmedQuery.isEmpty()) {
            filteredByStatus
        } else {
            filteredByStatus.filter { kamar ->
                kamar.nomorKamar.contains(trimmedQuery, ignoreCase = true) ||
                        (kamar.namaPenghuni?.contains(trimmedQuery, ignoreCase = true) == true)
            }
        }

        val sortedList = when (sort) {
            SortOrder.TERBARU -> filteredBySearch.sortedByDescending { it.id }
            SortOrder.HARGA_TERENDAH -> filteredBySearch.sortedBy { it.hargaSewa }
            SortOrder.HARGA_TERTINGGI -> filteredBySearch.sortedByDescending { it.hargaSewa }
            SortOrder.NOMOR_AZ -> filteredBySearch.sortedBy { it.nomorKamar.lowercase() }
        }

        BerandaUiState(
            isLoading = false,
            errorMessage = null,
            totalKamar = total,
            totalTerisi = terisi,
            totalKosong = kosong,
            estimasiPemasukan = totalPemasukan,
            displayedList = sortedList,
            occupiedKamarList = occupiedRooms,
            isDatabaseEmpty = allList.isEmpty(),
            activeSort = sort
        )
    }
        .catch { throwable ->
            emit(
                BerandaUiState(
                    isLoading = false,
                    errorMessage = throwable.localizedMessage
                        ?: application.getString(R.string.error_desc_default)
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = BerandaUiState(isLoading = true)
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: FilterStatus) {
        _filterStatus.value = status
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun retryLoad() {
        _retryTrigger.value += 1
    }
}