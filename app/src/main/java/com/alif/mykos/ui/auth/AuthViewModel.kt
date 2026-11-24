package com.alif.mykos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.mykos.data.repository.AuthRepository
import com.alif.mykos.utils.UiState
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<FirebaseUser>>(UiState.Idle)
    val loginState: StateFlow<UiState<FirebaseUser>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<FirebaseUser>>(UiState.Idle)
    val registerState: StateFlow<UiState<FirebaseUser>> = _registerState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState<Unit>> = _resetPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun register(namaLengkap: String, email: String, password: String) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.register(namaLengkap, email, password)
            _registerState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        _resetPasswordState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            _resetPasswordState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun resetStates() {
        _loginState.value = UiState.Idle
        _registerState.value = UiState.Idle
        _resetPasswordState.value = UiState.Idle
    }

    private fun mapFirebaseError(throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthInvalidUserException ->
                "Akun dengan email tersebut tidak ditemukan."
            is FirebaseAuthInvalidCredentialsException ->
                "Email atau password tidak sesuai. Silakan periksa kembali."
            is FirebaseAuthUserCollisionException ->
                "Email ini sudah terdaftar. Silakan gunakan email lain atau masuk."
            is FirebaseAuthWeakPasswordException ->
                "Password terlalu lemah. Gunakan minimal 6 karakter."
            is FirebaseNetworkException ->
                "Koneksi internet bermasalah. Periksa jaringan Anda."
            else ->
                throwable.localizedMessage ?: "Terjadi kesalahan autentikasi. Silakan coba lagi."
        }
    }
}