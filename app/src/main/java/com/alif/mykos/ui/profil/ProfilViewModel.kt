package com.alif.mykos.ui.profil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alif.mykos.R
import com.alif.mykos.data.local.AppDatabase
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.data.preferences.ThemePreferences
import com.alif.mykos.data.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfilViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application)
    private val authRepository = AuthRepository()
    private val kamarDao = AppDatabase.getInstance(application).kamarDao()

    val themeMode: StateFlow<Int> = themePreferences.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ThemePreferences.MODE_SYSTEM
        )

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            themePreferences.saveThemeMode(mode)
        }
    }

    suspend fun getOccupiedKamarList(): List<KamarEntity> {
        return kamarDao.getOccupiedKamarSync()
    }

    suspend fun updateDisplayName(namaBaru: String): Result<Unit> {
        return authRepository.updateDisplayName(namaBaru)
    }

    suspend fun sendPasswordResetEmail(): Result<Unit> {
        val email = authRepository.currentUser?.email
        if (email.isNullOrBlank()) {
            return Result.failure(Exception("Email akun tidak ditemukan."))
        }
        return authRepository.sendPasswordReset(email)
    }

    fun getUserDisplayName(): String {
        val user = authRepository.currentUser
        return user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: getApplication<Application>().getString(R.string.default_owner_name)
    }

    fun getUserEmail(): String {
        return authRepository.currentUser?.email ?: "-"
    }

    fun logout() {
        authRepository.logout()
    }
}