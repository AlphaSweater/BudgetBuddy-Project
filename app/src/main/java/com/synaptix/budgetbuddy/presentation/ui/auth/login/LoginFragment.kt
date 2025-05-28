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
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAuthLoginBinding
import com.synaptix.budgetbuddy.presentation.ui.auth.AuthActivity
import com.synaptix.budgetbuddy.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_auth_login) {

    private var _binding: FragmentAuthLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthLoginBinding.bind(view)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Add text change listeners for real-time validation
        binding.edtEmailAddress.addTextChangedListener(createTextWatcher { text ->
            binding.tilEmail.error = viewModel.validateEmail(text.toString())
        })

        binding.edtPassword.addTextChangedListener(createTextWatcher { text ->
            binding.tilPassword.error = viewModel.validatePassword(text.toString())
        })

        // Handle login button click with animation
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailAddress.text.toString()
            val password = binding.edtPassword.text.toString()
            
            if (viewModel.validateInputs(email, password)) {
                binding.btnLogin.startLoading()
                viewModel.login(email, password)
            }
        }

        // Handle back button click
        binding.btnBackLogin.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginUiState.Idle -> {
                    enableInputs(true)
                }
                is LoginUiState.Loading -> {
                    enableInputs(false)
                }
                is LoginUiState.Success -> {
                    navigateToMain()
                }
                is LoginUiState.Error -> {
                    binding.btnLogin.reset()
                    showErrorMessage(state.message)
                    enableInputs(true)
                }
                is LoginUiState.ValidationError -> {
                    binding.tilEmail.error = state.emailError
                    binding.tilPassword.error = state.passwordError
                    binding.btnLogin.reset()
                }
            }
        }
    }

    private fun navigateToMain() {
        viewLifecycleOwner.lifecycleScope.launch {
            showSuccessMessage("Login successful")
            delay(1000)
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .setTextColor(resources.getColor(android.R.color.white, null))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .setTextColor(resources.getColor(android.R.color.white, null))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setAction("Dismiss") { }
            .show()
    }

    private fun enableInputs(enabled: Boolean) {
        binding.edtEmailAddress.isEnabled = enabled
        binding.edtPassword.isEnabled = enabled
        binding.btnBackLogin.isEnabled = enabled
    }

    private fun createTextWatcher(onTextChanged: (Editable?) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onTextChanged(s)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

