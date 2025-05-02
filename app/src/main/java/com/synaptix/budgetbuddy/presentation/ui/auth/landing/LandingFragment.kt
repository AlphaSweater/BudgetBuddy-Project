package com.synaptix.budgetbuddy.presentation.ui.auth.landing

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.synaptix.budgetbuddy.presentation.ui.auth.AuthActivity
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAuthLandingBinding

class LandingFragment : Fragment(R.layout.fragment_auth_landing) {

    private lateinit var binding: FragmentAuthLandingBinding
    private val viewModel: LandingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate the ViewBinding
        binding = FragmentAuthLandingBinding.bind(view)

        // Set up button clicks with ViewBinding
        binding.btnLogin.setOnClickListener {
            (activity as? AuthActivity)?.showLogin()
        }

        binding.btnSignup.setOnClickListener {
            (activity as? AuthActivity)?.showSignup()
        }
    }
}
