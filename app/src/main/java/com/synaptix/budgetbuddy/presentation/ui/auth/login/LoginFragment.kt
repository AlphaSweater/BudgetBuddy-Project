//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.presentation.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAuthLoginBinding
import com.synaptix.budgetbuddy.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_auth_login) {

    private lateinit var binding: FragmentAuthLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    // Called when the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAuthLoginBinding.bind(view)

        // Observe login state changes and handle UI updates accordingly
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

                    // Navigate to main screen after successful login
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

        // Handle back button click
        binding.btnBackLogin.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // Enable or disable input fields based on the login state
    private fun enableInputs(enabled: Boolean) {
        binding.edtEmailAddress.isEnabled = enabled
        binding.edtPassword.isEnabled = enabled
        binding.btnLogin.isEnabled = enabled
        binding.btnBackLogin.isEnabled = enabled
    }
}
