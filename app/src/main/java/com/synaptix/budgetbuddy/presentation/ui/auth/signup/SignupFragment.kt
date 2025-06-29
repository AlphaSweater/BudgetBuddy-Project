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

package com.synaptix.budgetbuddy.presentation.ui.auth.signup

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.synaptix.budgetbuddy.presentation.ui.auth.AuthActivity
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAuthSignupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.android.material.snackbar.Snackbar
import android.text.TextWatcher
import android.text.Editable
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_auth_signup) {

    private var _binding: FragmentAuthSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthSignupBinding.bind(view)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Add text change listeners for real-time validation
        binding.edtEmailAddress.addTextChangedListener(createTextWatcher { text ->
            // Clear error when user starts typing
            binding.tilEmail.error = null
        })

        binding.edtPassword.addTextChangedListener(createTextWatcher { text ->
            // Clear error when user starts typing
            binding.tilPassword.error = null
        })

        binding.edtTxtPasswordConfirm.addTextChangedListener(createTextWatcher { text ->
            // Clear error when user starts typing
            binding.tilPasswordConfirm.error = null
        })

        // Add focus change listeners to validate on focus loss
        binding.edtEmailAddress.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.tilEmail.error = viewModel.validateEmail(binding.edtEmailAddress.text.toString())
            }
        }

        binding.edtPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.tilPassword.error = viewModel.validatePassword(binding.edtPassword.text.toString())
            }
        }

        binding.edtTxtPasswordConfirm.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.tilPasswordConfirm.error = viewModel.validatePasswordConfirmation(
                    binding.edtPassword.text.toString(),
                    binding.edtTxtPasswordConfirm.text.toString()
                )
            }
        }

        // Handle back button with animation
        binding.btnBackSignup.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Handle sign-up button click with animation
        binding.btnSignup.setOnClickListener {
            val email = binding.edtEmailAddress.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtTxtPasswordConfirm.text.toString()

            // Validate first
            if (!viewModel.validateInputs(email, password, confirmPassword)) {
                binding.btnSignup.showError()
                return@setOnClickListener
            }

            // Only start loading if validation passes
            binding.btnSignup.startLoading()
            performSignup()
        }
    }

    private fun performSignup() {
        val email = binding.edtEmailAddress.text.toString()
        val password = binding.edtPassword.text.toString()

        // Check if email exists and proceed with signup
        lifecycleScope.launch {
            try {
                val emailExists = viewModel.checkEmailExists(email)
                if (emailExists) {
                    binding.btnSignup.showError()
                    showErrorState("Email already in use")
                    return@launch
                }

                // Proceed with signup
                viewModel.signUp(email, password)
            } catch (e: Exception) {
                binding.btnSignup.showError()
                showErrorState("An error occurred. Please try again.")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.signupState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SignupUiState.Idle -> {
                    enableInputs(true)
                    binding.btnSignup.reset()
                }
                is SignupUiState.Loading -> {
                    enableInputs(false)
                }
                is SignupUiState.Success -> {
                    binding.btnSignup.showSuccess()
                    showSuccessState()
                }
                is SignupUiState.Error -> {
                    binding.btnSignup.showError()
                    showErrorState(state.message)
                    enableInputs(true)
                }
                is SignupUiState.ValidationError -> {
                    binding.tilEmail.error = state.emailError
                    binding.tilPassword.error = state.passwordError
                    binding.tilPasswordConfirm.error = state.confirmPasswordError
                    binding.btnSignup.showError()
                    enableInputs(true)
                }
            }
        }
    }

    private fun showSuccessState() {
        viewLifecycleOwner.lifecycleScope.launch {
            showSuccessMessage("Account created successfully!")
            delay(1000)
            (activity as? AuthActivity)?.showLogin()
        }
    }

    private fun showErrorState(message: String) {
        showErrorMessage(message)
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
        binding.edtTxtPasswordConfirm.isEnabled = enabled
        binding.btnBackSignup.isEnabled = enabled
        binding.btnSignup.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

