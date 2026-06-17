package com.example.altiguide_mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.altiguide_mobile.data.model.UserModel
import com.example.altiguide_mobile.data.repository.AuthRepository
import com.example.altiguide_mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserModel>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserModel>> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateState: StateFlow<UiState<String>> = _updateState.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            try {
                val user = authRepository.getUserProfile()
                _profileState.value = UiState.Success(user)
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Gagal memuat profil")
            }
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        phone_number: String?,
        age: Int?,
        address: String?,
        emergency_contact: String?,
        nik: String?,
        password: String = ""
    ) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            try {
                val map = mutableMapOf<String, Any>(
                    "name" to name,
                    "email" to email
                )
                phone_number?.let { map["phone_number"] = it }
                age?.let { map["age"] = it }
                address?.let { map["address"] = it }
                emergency_contact?.let { map["emergency_contact"] = it }
                nik?.let { map["nik"] = it }

                val response = authRepository.updateUserProfile(map)
                if (response.isSuccessful) {
                    if (password.isNotBlank()) {
                        authRepository.changePassword(mapOf("password" to password, "password_confirmation" to password))
                    }
                    kotlinx.coroutines.delay(500)
                    _updateState.value = UiState.Success("Profil berhasil diperbarui")
                    fetchProfile()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal memperbarui profil"
                    _updateState.value = UiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Gagal memperbarui profil")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }
}
