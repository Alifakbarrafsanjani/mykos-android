package com.alif.mykos.ui.profil

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alif.mykos.R
import com.alif.mykos.data.preferences.ThemePreferences
import com.alif.mykos.databinding.DialogEditNamaBinding
import com.alif.mykos.databinding.FragmentProfilBinding
import com.alif.mykos.ui.auth.AuthActivity
import com.alif.mykos.utils.OccupiedRoomExporter
import com.alif.mykos.utils.RentReminderHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilFragment : Fragment(R.layout.fragment_profil) {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfilViewModel by viewModels()

    private val createPdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            exportOccupiedRooms(uri)
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            runManualNotificationCheck()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_notif_permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfilBinding.bind(view)

        setupUserInfo()
        setupListeners()
        observeThemeState()
    }

    private fun setupUserInfo() {
        binding.tvProfilNama.text = viewModel.getUserDisplayName()
        binding.tvProfilEmail.text = viewModel.getUserEmail()
    }

    private fun setupListeners() {
        binding.cardProfilUser.setOnClickListener {
            showEditNamaDialog()
        }

        binding.itemModeTema.setOnClickListener {
            showThemeSelectionDialog()
        }

        binding.itemGantiPassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.itemExportTerisi.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val occupiedRooms = viewModel.getOccupiedKamarList()
                if (occupiedRooms.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_no_occupied_rooms),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    createPdfLauncher.launch(OccupiedRoomExporter.createDefaultFileName())
                }
            }
        }

        binding.itemCekNotifikasi.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !RentReminderHelper.hasNotificationPermission(requireContext())
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                runManualNotificationCheck()
            }
        }

        binding.itemTentangAplikasi.setOnClickListener {
            showAboutAppDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun runManualNotificationCheck() {
        viewLifecycleOwner.lifecycleScope.launch {
            val notifiedCount = withContext(Dispatchers.IO) {
                RentReminderHelper.checkAndNotifyDueRooms(
                    context = requireContext(),
                    sendDemoIfNone = true
                )
            }

            val toastMsg = if (notifiedCount > 0) {
                getString(R.string.msg_notif_sent)
            } else {
                getString(R.string.msg_notif_checked_empty)
            }
            Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditNamaDialog() {
        val dialogBinding = DialogEditNamaBinding.inflate(layoutInflater)
        dialogBinding.etEditNama.setText(viewModel.getUserDisplayName())
        dialogBinding.etEditNama.setSelection(dialogBinding.etEditNama.text?.length ?: 0)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_edit_nama_title)
            .setMessage(R.string.dialog_edit_nama_message)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.btn_batal, null)
            .setPositiveButton(R.string.btn_simpan, null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val namaBaru = dialogBinding.etEditNama.text?.toString()?.trim().orEmpty()
                if (namaBaru.isEmpty()) {
                    dialogBinding.tilEditNama.error = getString(R.string.err_field_required)
                    return@setOnClickListener
                }

                dialogBinding.tilEditNama.error = null
                btnSimpan.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    val result = viewModel.updateDisplayName(namaBaru)
                    if (result.isSuccess) {
                        binding.tvProfilNama.text = namaBaru
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.msg_nama_updated),
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    } else {
                        btnSimpan.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.msg_nama_update_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val email = viewModel.getUserEmail()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_ganti_password_title)
            .setMessage(getString(R.string.dialog_ganti_password_message, email))
            .setNegativeButton(R.string.btn_batal, null)
            .setPositiveButton(R.string.btn_kirim) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = viewModel.sendPasswordResetEmail()
                    val msgRes = if (result.isSuccess) {
                        R.string.msg_ganti_password_sent
                    } else {
                        R.string.msg_ganti_password_failed
                    }
                    Toast.makeText(requireContext(), getString(msgRes), Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun exportOccupiedRooms(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val occupiedRooms = viewModel.getOccupiedKamarList()
            if (occupiedRooms.isEmpty()) return@launch

            val isSuccess = withContext(Dispatchers.IO) {
                OccupiedRoomExporter.exportOccupiedRoomsToPdf(
                    requireContext(),
                    uri,
                    occupiedRooms
                )
            }

            if (isSuccess) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_export_success),
                    Toast.LENGTH_SHORT
                ).show()
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_export_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeThemeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.themeMode.collect { mode ->
                    binding.tvCurrentTheme.text = when (mode) {
                        ThemePreferences.MODE_LIGHT -> getString(R.string.tema_terang)
                        ThemePreferences.MODE_DARK -> getString(R.string.tema_gelap)
                        else -> getString(R.string.tema_sistem)
                    }
                }
            }
        }
    }

    private fun showThemeSelectionDialog() {
        val themeModes = intArrayOf(
            ThemePreferences.MODE_SYSTEM,
            ThemePreferences.MODE_LIGHT,
            ThemePreferences.MODE_DARK
        )
        val themeLabels = arrayOf(
            getString(R.string.tema_sistem),
            getString(R.string.tema_terang),
            getString(R.string.tema_gelap)
        )

        val currentMode = viewModel.themeMode.value
        val checkedIndex = themeModes.indexOf(currentMode).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_mode_tema)
            .setSingleChoiceItems(themeLabels, checkedIndex) { dialog, which ->
                viewModel.setThemeMode(themeModes[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_batal, null)
            .show()
    }

    private fun showAboutAppDialog() {
        val message = "${getString(R.string.app_version_info)}\n\n${getString(R.string.app_about_desc)}"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_tentang_aplikasi)
            .setMessage(message)
            .setPositiveButton(R.string.btn_tutup, null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_logout_title)
            .setMessage(R.string.dialog_logout_message)
            .setNegativeButton(R.string.btn_batal, null)
            .setPositiveButton(R.string.btn_keluar) { _, _ ->
                viewModel.logout()
                val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}