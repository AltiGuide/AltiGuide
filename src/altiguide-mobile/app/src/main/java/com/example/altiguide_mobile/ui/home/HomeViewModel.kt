package com.example.altiguide_mobile.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.altiguide_mobile.data.model.MountainModel
import com.example.altiguide_mobile.data.model.TransactionModel
import com.example.altiguide_mobile.data.repository.MountainRepository
import com.example.altiguide_mobile.data.repository.TransactionRepository
import com.example.altiguide_mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

import com.example.altiguide_mobile.data.model.RouteModel

private const val TAG = "HomeViewModel"
private const val MAX_RETRIES = 2

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mountainRepository: MountainRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedRoute = MutableStateFlow<RouteModel?>(null)
    val selectedRoute: StateFlow<RouteModel?> = _selectedRoute.asStateFlow()

    private val _mountainsState = MutableStateFlow<UiState<List<MountainModel>>>(UiState.Idle)
    val mountainsState: StateFlow<UiState<List<MountainModel>>> = _mountainsState.asStateFlow()

    private val _activeWeatherState = MutableStateFlow<UiState<com.example.altiguide_mobile.data.model.WeatherResponse>>(UiState.Idle)
    val activeWeatherState: StateFlow<UiState<com.example.altiguide_mobile.data.model.WeatherResponse>> = _activeWeatherState.asStateFlow()

    private val _mountainDetailState = MutableStateFlow<UiState<MountainModel>>(UiState.Idle)
    val mountainDetailState: StateFlow<UiState<MountainModel>> = _mountainDetailState.asStateFlow()

    private val _bookingsState = MutableStateFlow<UiState<List<TransactionModel>>>(UiState.Idle)
    val bookingsState: StateFlow<UiState<List<TransactionModel>>> = _bookingsState.asStateFlow()

    fun selectRoute(route: RouteModel?) {
        _selectedRoute.value = route
    }

    fun fetchMountainDetail(id: Int) {
        viewModelScope.launch {
            _mountainDetailState.value = UiState.Loading
            try {
                val detail = mountainRepository.getMountainDetail(id)
                _mountainDetailState.value = UiState.Success(detail)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal fetch detail gunung: ${e.message}", e)
                _mountainDetailState.value = UiState.Error(e.message ?: "Gagal memuat detail gunung")
            }
        }
    }

    fun clearMountainDetail() {
        _mountainDetailState.value = UiState.Idle
    }

    init {
        fetchMountains()
    }

    fun fetchMountains() {
        viewModelScope.launch {
            _mountainsState.value = UiState.Loading
            var attempts = 0
            var lastError: Exception? = null

            while (attempts <= MAX_RETRIES) {
                try {
                    val mountains = mountainRepository.getMountains()
                    _mountainsState.value = UiState.Success(mountains)
                    return@launch
                } catch (e: Exception) {
                    attempts++
                    lastError = e
                    Log.e(TAG, "Gagal fetch gunung (percobaan $attempts/${MAX_RETRIES + 1}): ${e.message}", e)

                    if (attempts <= MAX_RETRIES && e is IOException) {
                        delay(1000L)
                        _mountainsState.value = UiState.Loading
                    }
                }
            }

            val msg = lastError?.message ?: "Gagal memuat data gunung"
            _mountainsState.value = UiState.Error(msg)
        }
    }

    fun fetchWeatherForMountain(latitude: Double, longitude: Double, elevation: Double? = null) {
        viewModelScope.launch {
            _activeWeatherState.value = UiState.Loading
            try {
                val weather = mountainRepository.getMountainWeatherDirect(latitude, longitude, elevation)
                _activeWeatherState.value = UiState.Success(weather)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal fetch cuaca open-meteo: ${e.message}", e)
                _activeWeatherState.value = UiState.Error(e.message ?: "Gagal memuat cuaca")
            }
        }
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _bookingsState.value = UiState.Loading
            try {
                val response = transactionRepository.getTransactions()
                _bookingsState.value = UiState.Success(response.data)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal fetch bookings: ${e.message}", e)
                _bookingsState.value = UiState.Error(e.message ?: "Gagal memuat riwayat booking")
            }
        }
    }

    private fun clearError() {
        _mountainsState.value = UiState.Idle
    }
}

