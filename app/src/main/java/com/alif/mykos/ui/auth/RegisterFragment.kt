package com.alif.mykos.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.alif.mykos.MainActivity
import com.alif.mykos.R
import com.alif.mykos.databinding.FragmentRegisterBinding
import com.alif.mykos.utils.UiState
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupListeners()
        observeStates()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.tvToLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validateAndRegister() {
        val namaLengkap = binding.etNamaLengkap.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        binding.tilNamaLengkap.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var isValid = true

        if (namaLengkap.isEmpty()) {
            binding.tilNamaLengkap.error = getString(R.string.err_nama_empty)
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.err_email_empty)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_email_invalid)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.err_password_empty)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.err_password_short)
            isValid = false
        }

        if (isValid) {
            viewModel.register(namaLengkap, email, password)
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is UiState.Idle -> setLoading(false)
                        is UiState.Loading -> setLoading(true)
                        is UiState.Success -> {
                            setLoading(false)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.msg_register_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetStates()
                            navigateToMain()
                        }
                        is UiState.Error -> {
                            setLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetStates()
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}