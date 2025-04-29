package com.synaptix.budgetbuddy.presentation.ui.auth.signup

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
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

        // Handle signup logic with ViewBinding
        binding.btnSignup.setOnClickListener {
            (activity as? AuthActivity)?.showLogin()
            // Handle signup action
        }

        // Handle back button
        binding.btnBackSignup.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Inside onViewCreated in SignupFragment.kt

        binding.btnSignup.setOnClickListener {
            val email = binding.edtEmailAddress.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtTxtPasswordConfirm.text.toString()

            viewModel.signUp(email, password)

        }
    }
}
