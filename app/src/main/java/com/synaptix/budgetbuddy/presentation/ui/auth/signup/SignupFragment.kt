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

            //user input validation that ensures inputs are not empty
            if (email.isEmpty()) {
                binding.edtEmailAddress.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.edtPassword.error = "Password is required"
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                binding.edtTxtPasswordConfirm.error = "Please confirm your password"
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
}
