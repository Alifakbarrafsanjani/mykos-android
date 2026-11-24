package com.alif.mykos.ui.form

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.alif.mykos.R
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.databinding.FragmentFormKamarBinding
import com.alif.mykos.utils.ImageStorageHelper
import com.alif.mykos.utils.RentReminderHelper
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FormKamarFragment : Fragment(R.layout.fragment_form_kamar) {

    private var _binding: FragmentFormKamarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FormKamarViewModel by viewModels()

    private var kamarId: Int = -1
    private var existingKamar: KamarEntity? = null
    private var selectedPhotoUri: Uri? = null
    private var pendingCameraUri: Uri? = null

    private val galleryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            showPhotoPreview(uri)
        }
    }

    private val cameraCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        val capturedUri = pendingCameraUri
        if (isSuccess && capturedUri != null) {
            selectedPhotoUri = capturedUri
            showPhotoPreview(capturedUri)
        } else {
            cleanupTempCameraCache()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFormKamarBinding.bind(view)

        kamarId = arguments?.getInt("kamarId", -1) ?: -1

        setupToolbar()
        setupStatusDropdown()
        setupPhotoPicker()
        setupDatePicker()
        setupSaveButton()

        if (kamarId > 0) {
            loadExistingKamar(kamarId)
        }
    }

    private fun setupToolbar() {
        binding.toolbarForm.title = if (kamarId > 0) {
            getString(R.string.title_edit_kamar)
        } else {
            getString(R.string.title_tambah_kamar)
        }

        binding.btnSimpanKamar.text = if (kamarId > 0) {
            getString(R.string.btn_perbarui_kamar)
        } else {
            getString(R.string.btn_simpan_kamar)
        }

        binding.toolbarForm.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupStatusDropdown() {
        val statusOptions = listOf(
            getString(R.string.status_kosong),
            getString(R.string.status_terisi)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statusOptions
        )
        binding.actvStatusKamar.setAdapter(adapter)
        binding.actvStatusKamar.setText(getString(R.string.status_kosong), false)
        togglePenghuniSection(isTerisi = false)

        binding.actvStatusKamar.setOnItemClickListener { _, _, position, _ ->
            val selectedStatus = statusOptions[position]
            val isTerisi = selectedStatus.equals(KamarEntity.STATUS_TERISI, ignoreCase = true)
            togglePenghuniSection(isTerisi)
        }
    }

    private fun togglePenghuniSection(isTerisi: Boolean) {
        binding.layoutFormPenghuni.visibility = if (isTerisi) View.VISIBLE else View.GONE
    }

    private fun setupPhotoPicker() {
        val openPickerAction = {
            PhotoPickerBottomSheet.show(
                fragmentManager = childFragmentManager,
                onCameraSelected = { launchCamera() },
                onGallerySelected = { launchGallery() }
            )
        }

        binding.frameUploadFoto.setOnClickListener { openPickerAction() }
        binding.btnGantiFoto.setOnClickListener { openPickerAction() }
    }

    private fun launchCamera() {
        try {
            val tempUri = ImageStorageHelper.createTempCameraUri(requireContext())
            pendingCameraUri = tempUri
            cameraCaptureLauncher.launch(tempUri)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_camera_not_found),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_camera_open_failed, e.localizedMessage.orEmpty()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun launchGallery() {
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun showPhotoPreview(uri: Uri) {
        binding.layoutUploadPlaceholder.visibility = View.GONE
        binding.ivFormFotoPreview.visibility = View.VISIBLE
        binding.btnGantiFoto.visibility = View.VISIBLE

        binding.ivFormFotoPreview.load(uri) {
            crossfade(true)
            placeholder(R.drawable.ic_image_placeholder)
            error(R.drawable.ic_image_placeholder)
        }
    }

    private fun setupDatePicker() {
        binding.etTanggalMasuk.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.dialog_date_picker_title))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                binding.etTanggalMasuk.setText(formatter.format(Date(selection)))
                binding.tilTanggalMasuk.error = null
            }

            datePicker.show(childFragmentManager, "DATE_PICKER_MASUK")
        }
    }

    private fun loadExistingKamar(id: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val kamar = viewModel.getKamarById(id) ?: return@launch
            existingKamar = kamar

            binding.etNomorKamar.setText(kamar.nomorKamar)
            binding.etHargaSewa.setText(kamar.hargaSewa.toString())

            if (!kamar.fotoUri.isNullOrBlank()) {
                val uri = Uri.parse(kamar.fotoUri)
                selectedPhotoUri = uri
                showPhotoPreview(uri)
            }

            val savedFasilitas = kamar.getFasilitasList().map { it.lowercase() }.toSet()
            for (i in 0 until binding.chipGroupFormFasilitas.childCount) {
                val chip = binding.chipGroupFormFasilitas.getChildAt(i) as? Chip ?: continue
                if (chip.text.toString().lowercase() in savedFasilitas) {
                    chip.isChecked = true
                }
            }

            val isTerisi = kamar.isTerisi()
            val statusText = if (isTerisi) {
                getString(R.string.status_terisi)
            } else {
                getString(R.string.status_kosong)
            }
            binding.actvStatusKamar.setText(statusText, false)
            togglePenghuniSection(isTerisi)

            if (isTerisi) {
                binding.etNamaPenghuni.setText(kamar.namaPenghuni.orEmpty())
                binding.etNoHp.setText(kamar.noHpPenghuni.orEmpty())
                binding.etTanggalMasuk.setText(kamar.tanggalMasuk.orEmpty())
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSimpanKamar.setOnClickListener {
            validateAndSave()
        }
    }

    private fun validateAndSave() {
        val nomorKamar = binding.etNomorKamar.text?.toString()?.trim().orEmpty()
        val hargaText = binding.etHargaSewa.text?.toString()?.trim().orEmpty()
        val statusText = binding.actvStatusKamar.text?.toString()?.trim().orEmpty()
        val isTerisi = statusText.equals(KamarEntity.STATUS_TERISI, ignoreCase = true)

        val namaPenghuni = binding.etNamaPenghuni.text?.toString()?.trim().orEmpty()
        val noHp = binding.etNoHp.text?.toString()?.trim().orEmpty()
        val tanggalMasuk = binding.etTanggalMasuk.text?.toString()?.trim().orEmpty()

        binding.tilNomorKamar.error = null
        binding.tilHargaSewa.error = null
        binding.tilNamaPenghuni.error = null
        binding.tilNoHp.error = null

        var isValid = true

        if (nomorKamar.isEmpty()) {
            binding.tilNomorKamar.error = getString(R.string.err_nomor_kamar_empty)
            isValid = false
        }

        val hargaSewa = hargaText.toLongOrNull()
        if (hargaSewa == null || hargaSewa <= 0L) {
            binding.tilHargaSewa.error = getString(R.string.err_harga_sewa_invalid)
            isValid = false
        }

        if (isTerisi) {
            if (namaPenghuni.isEmpty()) {
                binding.tilNamaPenghuni.error = getString(R.string.err_nama_penghuni_empty)
                isValid = false
            }
            if (noHp.isEmpty()) {
                binding.tilNoHp.error = getString(R.string.err_no_hp_penghuni_empty)
                isValid = false
            }
        }

        if (!isValid || hargaSewa == null) return

        val selectedFasilitas = collectSelectedFasilitas()

        binding.btnSimpanKamar.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveKamar(
                existingKamar = existingKamar,
                nomorKamar = nomorKamar,
                hargaSewa = hargaSewa,
                fasilitas = selectedFasilitas,
                status = statusText,
                selectedPhotoUri = selectedPhotoUri,
                namaPenghuni = namaPenghuni,
                noHpPenghuni = noHp,
                tanggalMasuk = tanggalMasuk
            )

            cleanupTempCameraCache()

            if (existingKamar == null) {
                RentReminderHelper.notifyRoomSaved(
                    context = requireContext(),
                    nomorKamar = nomorKamar,
                    status = statusText,
                    namaPenghuni = namaPenghuni
                )
            }

            val message = if (existingKamar == null) {
                getString(R.string.msg_kamar_ditambahkan)
            } else {
                getString(R.string.msg_kamar_diperbarui)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun collectSelectedFasilitas(): String {
        val selected = mutableListOf<String>()
        for (i in 0 until binding.chipGroupFormFasilitas.childCount) {
            val chip = binding.chipGroupFormFasilitas.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) {
                selected.add(chip.text.toString())
            }
        }
        return selected.joinToString(", ")
    }

    private fun cleanupTempCameraCache() {
        try {
            val tempDir = File(requireContext().cacheDir, "camera_temp")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { it.delete() }
            }
        } catch (_: Exception) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}