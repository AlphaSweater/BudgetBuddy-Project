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

@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_auth_signup) {

    private lateinit var binding: FragmentAuthSignupBinding
    private val viewModel: SignupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate ViewBinding
        binding = FragmentAuthSignupBinding.bind(view)

        // Handle back button
        binding.btnBackSignup.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Handle sign-up button click
        binding.btnSignup.setOnClickListener {
            val email = binding.edtEmailAddress.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtTxtPasswordConfirm.text.toString()

            // Validate email format
            if (!isValidEmail(email)) {
                binding.edtEmailAddress.error = "Invalid email address"
                return@setOnClickListener
            }

            // Check if the email already exists (check database)
            // Placeholder logic for email existence check (future implementation)

            // Validate password complexity
            if (password.length < 8
                || !password.matches(Regex(".*[0-9].*"))
                || !password.matches(Regex(".*[A-Z].*"))
                || !password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
            ) {
                binding.edtPassword.error =
                    "Password must be at least 8 characters and contain 1 number, capital letter, and special character"
                return@setOnClickListener
            }

            // Ensure password and confirm password match
            if (password != confirmPassword) {
                binding.edtTxtPasswordConfirm.error = "Passwords do not match"
                return@setOnClickListener
            }

            // If all checks pass, proceed with sign-up
            lifecycleScope.launch {
                // Check if the email already exists
                val emailExists = viewModel.checkEmailExists(email)
                if (emailExists) {
                    binding.edtEmailAddress.error = "Email already in use"
                    return@launch
                }
                // Call ViewModel to handle the sign-up process
                val result = viewModel.signUp(email, password)
                // Navigate to login screen after successful sign-up
                (activity as? AuthActivity)?.showLogin()
            }
        }
    }

    // Utility function to validate email format using regex
    // AI assisted with the regex logic for this function
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))
    }
}
