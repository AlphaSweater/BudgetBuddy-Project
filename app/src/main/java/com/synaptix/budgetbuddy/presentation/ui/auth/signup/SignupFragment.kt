package com.synaptix.budgetbuddy.presentation.ui.auth.signup

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.synaptix.budgetbuddy.presentation.ui.auth.AuthActivity
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentSignupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_signup) {

    private lateinit var binding: FragmentSignupBinding
    private val viewModel: SignupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate ViewBinding
        binding = FragmentSignupBinding.bind(view)

        // Handle back button
        binding.btnBackSignup.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

       //binding data and passing data to viewModel
        binding.btnSignup.setOnClickListener {
            val email = binding.edtEmailAddress.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtTxtPasswordConfirm.text.toString()

            //checks to ensure email is valid
            if (isValidEmail(email) == false) {
                binding.edtEmailAddress.error = "Invalid email address"
                return@setOnClickListener
            }

            //checks to ensure password has more than 8 characters and has 1no, 1upper, 1special
            if (password.length >= 8 && password.matches(Regex(".*[0-9].*"))
                && password.matches(Regex(".*[A-Z].*"))
                && password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))) {

                binding.edtPassword.error = "Password must be at least 8 characters and contain 1 number, capital and special character"
                return@setOnClickListener
            }

            // Check if password and confirm password match
            if (password != confirmPassword) {
                binding.edtTxtPasswordConfirm.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Call the ViewModel to handle signup
            viewModel.signUp(email, password)
            (activity as? AuthActivity)?.showLogin()

        }


    }

    //uses regex to ensure that the email follows a valid layout
    //AI assisted with the regex logic for this function
    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))
    }

}

