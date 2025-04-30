package com.synaptix.budgetbuddy.presentation.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentLoginBinding
import com.synaptix.budgetbuddy.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLoginBinding.bind(view)

        // Observe login state
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginUiState.Idle -> {
                    binding.progressSpinner.visibility = View.GONE
                    enableInputs(true)
                }
                is LoginUiState.Loading -> {
                    binding.progressSpinner.visibility = View.VISIBLE
                    enableInputs(false)
                }
                is LoginUiState.Success -> {
                    binding.progressSpinner.visibility = View.GONE
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()

                    // Navigate to main screen
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is LoginUiState.Error -> {
                    binding.progressSpinner.visibility = View.GONE
                    enableInputs(true)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
            }
        }

        // Handle login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailAddress.text.toString()
            val password = binding.edtPassword.text.toString()
            if (email.isNotBlank() && password.isNotBlank()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle back button
        binding.btnBackLogin.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun enableInputs(enabled: Boolean) {
        binding.edtEmailAddress.isEnabled = enabled
        binding.edtPassword.isEnabled = enabled
        binding.btnLogin.isEnabled = enabled
        binding.btnBackLogin.isEnabled = enabled
    }
}