package com.alif.mykos.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alif.mykos.MainActivity
import com.alif.mykos.data.preferences.ThemePreferences
import com.alif.mykos.data.repository.AuthRepository
import com.alif.mykos.databinding.ActivitySplashBinding
import com.alif.mykos.ui.auth.AuthActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val themePreferences = ThemePreferences(applicationContext)
            val savedThemeMode = themePreferences.themeModeFlow.first()
            ThemePreferences.applyTheme(savedThemeMode)

            delay(1000L)

            val targetIntent = if (authRepository.isUserLoggedIn()) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, AuthActivity::class.java)
            }

            startActivity(targetIntent)
            finish()
        }
    }
}