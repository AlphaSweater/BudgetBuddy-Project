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

        //binding data and passing data to viewModel
        binding.btnSignup.setOnClickListener {
            val email = binding.edtEmailAddress.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtTxtPasswordConfirm.text.toString()

            //checks to ensure email is valid
            if (!isValidEmail(email)) {
                binding.edtEmailAddress.error = "Invalid email address"
                return@setOnClickListener
            }

            //checks to see if email is already in database


            //checks to ensure password has more than 8 characters and has 1no, 1upper, 1special
            if (password.length < 8
                || !password.matches(Regex(".*[0-9].*"))
                || !password.matches(Regex(".*[A-Z].*"))
                || !password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
            ) {

                binding.edtPassword.error =
                    "Password must be at least 8 characters and contain 1 number, capital and special character"
                return@setOnClickListener
            }

            // Check if password and confirm password match
            if (password != confirmPassword) {
                binding.edtTxtPasswordConfirm.error = "Passwords do not match"
                return@setOnClickListener
            }


            lifecycleScope.launch {
                val emailExists = viewModel.checkEmailExists(email)
                if (emailExists) {
                    binding.edtEmailAddress.error = "Email already in use"
                    return@launch
                }
                // Call the ViewModel to handle signup
                viewModel.signUp(email, password)
                (activity as? AuthActivity)?.showLogin()

            }


        }



    }
    //uses regex to ensure that the email follows a valid layout
    //AI assisted with the regex logic for this function
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))
    }
}

