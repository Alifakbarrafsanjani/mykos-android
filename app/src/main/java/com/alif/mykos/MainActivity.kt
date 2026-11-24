package com.alif.mykos

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alif.mykos.databinding.ActivityMainBinding
import com.alif.mykos.utils.RentReminderHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            triggerRentReminderCheck()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupNotifications()
    }

    private fun setupNotifications() {
        RentReminderHelper.createNotificationChannel(this)
        RentReminderHelper.scheduleDailyReminder(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !RentReminderHelper.hasNotificationPermission(this)
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            triggerRentReminderCheck()
        }
    }

    private fun triggerRentReminderCheck() {
        lifecycleScope.launch(Dispatchers.IO) {
            RentReminderHelper.checkAndNotifyDueRooms(this@MainActivity, sendDemoIfNone = false)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        // Sembunyikan BottomAppBar & FAB pada halaman Detail dan Form Kamar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.berandaFragment, R.id.profilFragment -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fabTambahKamar.show()
                }
                else -> {
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabTambahKamar.hide()
                }
            }
        }

        // Tombol FAB tengah untuk membuka Form Tambah Kamar (kamarId = -1)
        binding.fabTambahKamar.setOnClickListener {
            val args = Bundle().apply {
                putInt("kamarId", -1)
            }
            navController.navigate(R.id.formKamarFragment, args)
        }
    }
}