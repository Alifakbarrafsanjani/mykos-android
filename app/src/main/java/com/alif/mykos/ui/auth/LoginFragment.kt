package com.alif.mykos.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.alif.mykos.MainActivity
import com.alif.mykos.R
import com.alif.mykos.databinding.FragmentLoginBinding
import com.alif.mykos.utils.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupListeners()
        observeStates()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            validateAndLogin()
        }

        binding.btnToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun validateAndLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var isValid = true

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
            viewModel.login(email, password)
        }
    }

    private fun showForgotPasswordDialog() {
        val inputEditText = EditText(requireContext()).apply {
            hint = getString(R.string.hint_reset_email)
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(binding.etEmail.text?.toString()?.trim().orEmpty())
        }

        val container = FrameLayout(requireContext()).apply {
            val padding = resources.getDimensionPixelSize(R.dimen.spacing_lg)
            setPadding(padding, padding / 2, padding, 0)
            addView(inputEditText)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_reset_title)
            .setMessage(R.string.dialog_reset_message)
            .setView(container)
            .setNegativeButton(R.string.btn_batal, null)
            .setPositiveButton(R.string.btn_kirim) { _, _ ->
                val resetEmail = inputEditText.text?.toString()?.trim().orEmpty()
                if (resetEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_reset_email_invalid),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.sendPasswordReset(resetEmail)
                }
            }
            .show()
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loginState.collect { state ->
                        when (state) {
                            is UiState.Idle -> setLoading(false)
                            is UiState.Loading -> setLoading(true)
                            is UiState.Success -> {
                                setLoading(false)
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

                launch {
                    viewModel.resetPasswordState.collect { state ->
                        when (state) {
                            is UiState.Idle -> Unit
                            is UiState.Loading -> setLoading(true)
                            is UiState.Success -> {
                                setLoading(false)
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.msg_reset_sent),
                                    Toast.LENGTH_LONG
                                ).show()
                                viewModel.resetStates()
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
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnToRegister.isEnabled = !isLoading
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