package com.alif.mykos.ui.beranda

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alif.mykos.R
import com.alif.mykos.data.repository.AuthRepository
import com.alif.mykos.databinding.FragmentBerandaBinding
import com.alif.mykos.utils.CurrencyFormatter
import com.alif.mykos.utils.OccupiedRoomExporter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BerandaFragment : Fragment(R.layout.fragment_beranda) {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BerandaViewModel by viewModels()
    private val authRepository = AuthRepository()
    private lateinit var kamarAdapter: KamarAdapter

    private val createPdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            exportOccupiedRoomsToUri(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBerandaBinding.bind(view)

        applyStatusBarInsets()
        setupGreeting()
        setupStatLabels()
        setupRecyclerView()
        setupSearchFilterSort()
        observeUiState()
    }

    private fun applyStatusBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.cardHeroHeader) { headerView, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val baseTopMargin = resources.getDimensionPixelSize(R.dimen.spacing_md)
            val params = headerView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = statusBarTop + baseTopMargin
            headerView.layoutParams = params
            insets
        }
    }

    private fun setupGreeting() {
        val user = authRepository.currentUser
        val displayName = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }

        binding.tvGreeting.text = if (displayName != null) {
            getString(R.string.beranda_greeting_format, displayName)
        } else {
            getString(R.string.beranda_greeting_default)
        }

        val initial = displayName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
        binding.tvUserInitial.text = initial
    }

    private fun setupStatLabels() {
        binding.statTotal.tvStatLabel.text = getString(R.string.stat_total)
        binding.statTotal.tvStatCount.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )

        binding.statTerisi.tvStatLabel.text = getString(R.string.stat_terisi)
        binding.statTerisi.tvStatCount.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.success)
        )

        binding.statKosong.tvStatLabel.text = getString(R.string.stat_kosong)
        binding.statKosong.tvStatCount.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.text_secondary)
        )
    }

    private fun setupRecyclerView() {
        kamarAdapter = KamarAdapter { kamar ->
            val args = Bundle().apply {
                putInt("kamarId", kamar.id)
            }
            findNavController().navigate(
                R.id.action_berandaFragment_to_detailKamarFragment,
                args
            )
        }

        binding.rvKamar.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = kamarAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupSearchFilterSort() {
        binding.etSearchKamar.doAfterTextChanged { editable ->
            viewModel.setSearchQuery(editable?.toString().orEmpty())
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedId = checkedIds.firstOrNull() ?: R.id.chipFilterSemua
            val status = when (selectedId) {
                R.id.chipFilterTerisi -> FilterStatus.TERISI
                R.id.chipFilterKosong -> FilterStatus.KOSONG
                else -> FilterStatus.SEMUA
            }
            viewModel.setFilterStatus(status)
        }

        binding.btnSortKamar.setOnClickListener {
            showSortDialog()
        }

        binding.btnExportTerisi.setOnClickListener {
            val occupiedRooms = viewModel.uiState.value.occupiedKamarList
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

        binding.stateEmpty.btnEmptyAction.setOnClickListener {
            val args = Bundle().apply {
                putInt("kamarId", -1)
            }
            findNavController().navigate(
                R.id.action_berandaFragment_to_formKamarFragment,
                args
            )
        }

        binding.stateError.btnErrorRetry.setOnClickListener {
            viewModel.retryLoad()
        }
    }

    private fun exportOccupiedRoomsToUri(uri: Uri) {
        val occupiedRooms = viewModel.uiState.value.occupiedKamarList
        if (occupiedRooms.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
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
                openPdfPreview(uri)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_export_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openPdfPreview(uri: Uri) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(viewIntent)
        } catch (_: ActivityNotFoundException) {
            // File sudah tersimpan di folder Download pengguna
        }
    }

    private fun showSortDialog() {
        val options = SortOrder.entries.toTypedArray()
        val labels = options.map { getString(it.labelResId) }.toTypedArray()
        val currentSelection = options.indexOf(viewModel.uiState.value.activeSort).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_sort_title)
            .setSingleChoiceItems(labels, currentSelection) { dialog, which ->
                viewModel.setSortOrder(options[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_batal, null)
            .show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.statTotal.tvStatCount.text = state.totalKamar.toString()
                    binding.statTerisi.tvStatCount.text = state.totalTerisi.toString()
                    binding.statKosong.tvStatCount.text = state.totalKosong.toString()
                    binding.tvEstimasiPemasukan.text =
                        CurrencyFormatter.formatRupiah(state.estimasiPemasukan)

                    when {
                        state.isLoading -> {
                            binding.stateLoading.root.visibility = View.VISIBLE
                            binding.stateEmpty.root.visibility = View.GONE
                            binding.stateError.root.visibility = View.GONE
                            binding.rvKamar.visibility = View.GONE
                        }

                        state.errorMessage != null -> {
                            binding.stateLoading.root.visibility = View.GONE
                            binding.stateEmpty.root.visibility = View.GONE
                            binding.stateError.root.visibility = View.VISIBLE
                            binding.stateError.tvErrorDescription.text = state.errorMessage
                            binding.rvKamar.visibility = View.GONE
                        }

                        state.displayedList.isEmpty() -> {
                            binding.stateLoading.root.visibility = View.GONE
                            binding.stateError.root.visibility = View.GONE
                            binding.rvKamar.visibility = View.GONE
                            binding.stateEmpty.root.visibility = View.VISIBLE

                            if (state.isDatabaseEmpty) {
                                binding.stateEmpty.tvEmptyTitle.text =
                                    getString(R.string.empty_title_default)
                                binding.stateEmpty.tvEmptyDescription.text =
                                    getString(R.string.empty_desc_default)
                                binding.stateEmpty.btnEmptyAction.visibility = View.VISIBLE
                            } else {
                                binding.stateEmpty.tvEmptyTitle.text =
                                    getString(R.string.empty_title_search)
                                binding.stateEmpty.tvEmptyDescription.text =
                                    getString(R.string.empty_desc_search)
                                binding.stateEmpty.btnEmptyAction.visibility = View.GONE
                            }
                        }

                        else -> {
                            binding.stateLoading.root.visibility = View.GONE
                            binding.stateEmpty.root.visibility = View.GONE
                            binding.stateError.root.visibility = View.GONE
                            binding.rvKamar.visibility = View.VISIBLE
                            kamarAdapter.submitList(state.displayedList)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}