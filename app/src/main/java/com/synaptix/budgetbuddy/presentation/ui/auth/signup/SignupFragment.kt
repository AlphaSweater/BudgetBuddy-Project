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
            val error = viewModel.validateEmail(text.toString())
            binding.tilEmail.error = error
        })

        binding.edtPassword.addTextChangedListener(createTextWatcher { text ->
            val error = viewModel.validatePassword(text.toString())
            binding.tilPassword.error = error
        })

        binding.edtTxtPasswordConfirm.addTextChangedListener(createTextWatcher { text ->
            val error = viewModel.validatePasswordConfirmation(
                binding.edtPassword.text.toString(),
                text.toString()
            )
            binding.tilPasswordConfirm.error = error
        })

        // Handle back button with animation
        binding.btnBackSignup.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Handle sign-up button click with animation
        binding.btnSignup.setOnClickListener {
            performSignup()
        }
    }

    private fun performSignup() {
        val email = binding.edtEmailAddress.text.toString()
        val password = binding.edtPassword.text.toString()

        // Animate button press with a smoother animation
        binding.btnSignup.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(150)
            .withEndAction {
                binding.btnSignup.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .start()
            }
            .start()

        // Show loading state
        showLoadingState()

        // Check if email exists and proceed with signup
        lifecycleScope.launch {
            try {
                val emailExists = viewModel.checkEmailExists(email)
                if (emailExists) {
                    showErrorState("Email already in use")
                    return@launch
                }

                // Proceed with signup
                viewModel.signUp(email, password)
            } catch (e: Exception) {
                showErrorState("An error occurred. Please try again.")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.signupState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SignupUiState.Idle -> {
                    showIdleState()
                }
                is SignupUiState.Loading -> {
                    showLoadingState()
                }
                is SignupUiState.Success -> {
                    showSuccessState()
                }
                is SignupUiState.Error -> {
                    showErrorState(state.message)
                }
                is SignupUiState.ValidationError -> {
                    binding.tilEmail.error = state.emailError
                    binding.tilPassword.error = state.passwordError
                    binding.tilPasswordConfirm.error = state.confirmPasswordError
                }
            }
        }
    }

    private fun showIdleState() {
        (activity as? AuthActivity)?.showLoading(false)
        enableInputs(true)
    }

    private fun showLoadingState() {
        (activity as? AuthActivity)?.showLoading(true)
        enableInputs(false)
    }

    private fun showSuccessState() {
        (activity as? AuthActivity)?.showLoading(false)
        showSuccessMessage("Account created successfully!")
        (activity as? AuthActivity)?.showLogin()
    }

    private fun showErrorState(message: String) {
        (activity as? AuthActivity)?.showLoading(false)
        enableInputs(true)
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
        binding.btnSignup.isEnabled = enabled
        binding.btnBackSignup.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
