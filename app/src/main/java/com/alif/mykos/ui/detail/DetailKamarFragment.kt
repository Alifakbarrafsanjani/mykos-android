package com.alif.mykos.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.alif.mykos.R
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.databinding.FragmentDetailKamarBinding
import com.alif.mykos.utils.CurrencyFormatter
import com.alif.mykos.utils.OccupiedRoomExporter
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailKamarFragment : Fragment(R.layout.fragment_detail_kamar) {

    private var _binding: FragmentDetailKamarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailKamarViewModel by viewModels()
    private var currentKamar: KamarEntity? = null
    private var isDeleting = false

    private val createSingleRoomPdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            exportCurrentRoomToPdf(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailKamarBinding.bind(view)

        val kamarId = arguments?.getInt("kamarId") ?: 0

        setupToolbar()
        setupActionButtons()
        observeKamarData(kamarId)
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupActionButtons() {
        binding.btnHubungiWa.setOnClickListener {
            val kamar = currentKamar ?: return@setOnClickListener
            openWhatsApp(kamar)
        }

        binding.btnTelepon.setOnClickListener {
            val kamar = currentKamar ?: return@setOnClickListener
            openDialer(kamar.noHpPenghuni)
        }

        binding.btnEditKamar.setOnClickListener {
            val kamar = currentKamar ?: return@setOnClickListener
            val args = Bundle().apply {
                putInt("kamarId", kamar.id)
            }
            findNavController().navigate(
                R.id.action_detailKamarFragment_to_formKamarFragment,
                args
            )
        }

        binding.btnHapusKamar.setOnClickListener {
            val kamar = currentKamar ?: return@setOnClickListener
            showDeleteConfirmationDialog(kamar)
        }

        binding.btnUnduhDataKamar.setOnClickListener {
            val kamar = currentKamar ?: return@setOnClickListener
            createSingleRoomPdfLauncher.launch(
                OccupiedRoomExporter.createSingleRoomFileName(kamar.nomorKamar)
            )
        }

        binding.btnKirimPdfWa.setOnClickListener {
            val kamar = currentKamar ?: return@setOnClickListener
            shareSingleRoomPdfToWhatsApp(kamar)
        }
    }

    private fun shareSingleRoomPdfToWhatsApp(kamar: KamarEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            val pdfUri = withContext(Dispatchers.IO) {
                OccupiedRoomExporter.generateShareableSingleRoomPdfUri(
                    requireContext(),
                    kamar
                )
            }

            if (pdfUri == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_share_pdf_failed),
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val nama = kamar.namaPenghuni?.takeIf { it.isNotBlank() }
                ?: getString(R.string.default_penghuni_name)
            val harga = CurrencyFormatter.formatHargaPerBulan(kamar.hargaSewa)
            val caption = getString(
                R.string.wa_share_pdf_caption,
                nama,
                kamar.nomorKamar,
                harga
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val whatsappIntent = Intent(sendIntent).apply {
                setPackage("com.whatsapp")
            }

            try {
                startActivity(whatsappIntent)
            } catch (_: ActivityNotFoundException) {
                try {
                    startActivity(
                        Intent.createChooser(
                            sendIntent,
                            getString(R.string.msg_share_pdf_chooser)
                        )
                    )
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_share_pdf_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun exportCurrentRoomToPdf(uri: Uri) {
        val kamar = currentKamar ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val isSuccess = withContext(Dispatchers.IO) {
                OccupiedRoomExporter.exportSingleRoomToPdf(
                    requireContext(),
                    uri,
                    kamar
                )
            }

            if (isSuccess) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_detail_export_success),
                    Toast.LENGTH_SHORT
                ).show()
                try {
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(viewIntent)
                } catch (_: ActivityNotFoundException) {
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_detail_export_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeKamarData(kamarId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observeKamar(kamarId).collect { kamar ->
                    if (kamar == null) {
                        if (!isDeleting) {
                            findNavController().navigateUp()
                        }
                        return@collect
                    }

                    currentKamar = kamar
                    bindKamarToUi(kamar)
                }
            }
        }
    }

    private fun bindKamarToUi(kamar: KamarEntity) {
        val context = requireContext()

        binding.tvDetailNomorKamar.text = kamar.nomorKamar
        binding.tvDetailHarga.text = CurrencyFormatter.formatHargaPerBulan(kamar.hargaSewa)

        if (!kamar.fotoUri.isNullOrBlank()) {
            binding.ivDetailFoto.load(Uri.parse(kamar.fotoUri)) {
                crossfade(true)
                placeholder(R.drawable.ic_image_placeholder)
                error(R.drawable.ic_image_placeholder)
            }
        } else {
            binding.ivDetailFoto.setImageResource(R.drawable.ic_image_placeholder)
        }

        if (kamar.isTerisi()) {
            binding.tvDetailStatusBadge.text = getString(R.string.status_terisi)
            binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_status_terisi)
            binding.tvDetailStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.on_success_container)
            )

            binding.layoutPenghuniTerisi.visibility = View.VISIBLE
            binding.layoutPenghuniKosong.visibility = View.GONE
            binding.btnUnduhDataKamar.visibility = View.VISIBLE
            binding.btnKirimPdfWa.visibility = View.VISIBLE

            binding.tvDetailNamaPenghuni.text =
                kamar.namaPenghuni?.takeIf { it.isNotBlank() } ?: "-"
            binding.tvDetailNoHp.text =
                kamar.noHpPenghuni?.takeIf { it.isNotBlank() } ?: "-"
            binding.tvDetailTanggalMasuk.text =
                kamar.tanggalMasuk?.takeIf { it.isNotBlank() } ?: "-"
        } else {
            binding.tvDetailStatusBadge.text = getString(R.string.status_kosong)
            binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_status_kosong)
            binding.tvDetailStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.on_neutral_container)
            )

            binding.layoutPenghuniTerisi.visibility = View.GONE
            binding.layoutPenghuniKosong.visibility = View.VISIBLE
            binding.btnUnduhDataKamar.visibility = View.GONE
            binding.btnKirimPdfWa.visibility = View.GONE
        }

        populateFasilitasChips(kamar.getFasilitasList())
    }

    private fun populateFasilitasChips(fasilitasList: List<String>) {
        binding.chipGroupDetailFasilitas.removeAllViews()

        val items = fasilitasList.ifEmpty { listOf(getString(R.string.fasilitas_standar)) }
        for (fasilitas in items) {
            val chip = Chip(requireContext()).apply {
                text = fasilitas
                isClickable = false
                isCheckable = false
            }
            binding.chipGroupDetailFasilitas.addView(chip)
        }
    }

    private fun openWhatsApp(kamar: KamarEntity) {
        val rawPhone = kamar.noHpPenghuni?.trim().orEmpty()
        if (rawPhone.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_phone_empty),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val waNumber = CurrencyFormatter.formatWhatsAppNumber(rawPhone)
        val nama = kamar.namaPenghuni?.takeIf { it.isNotBlank() }
            ?: getString(R.string.default_penghuni_name)
        val message = Uri.encode(getString(R.string.wa_chat_template, nama, kamar.nomorKamar))
        val waUri = Uri.parse("https://wa.me/$waNumber?text=$message")

        try {
            startActivity(Intent(Intent.ACTION_VIEW, waUri))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_whatsapp_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openDialer(rawPhone: String?) {
        val phone = rawPhone?.trim().orEmpty()
        if (phone.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_phone_empty),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dialUri = Uri.parse("tel:$phone")
        try {
            startActivity(Intent(Intent.ACTION_DIAL, dialUri))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_dialer_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmationDialog(kamar: KamarEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_hapus_kamar_title, kamar.nomorKamar))
            .setMessage(R.string.dialog_hapus_kamar_message)
            .setNegativeButton(R.string.btn_batal, null)
            .setPositiveButton(R.string.btn_hapus) { _, _ ->
                isDeleting = true
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteKamar(kamar)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_kamar_dihapus),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}