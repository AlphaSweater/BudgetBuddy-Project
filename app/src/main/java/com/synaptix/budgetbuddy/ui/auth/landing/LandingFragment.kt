package com.synaptix.budgetbuddy.ui.auth.landing

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.synaptix.budgetbuddy.AuthActivity
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentLandingBinding

class LandingFragment : Fragment(R.layout.fragment_landing) {

    private lateinit var binding: FragmentLandingBinding
    private val viewModel: LandingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate the ViewBinding
        binding = FragmentLandingBinding.bind(view)

        // Set up button clicks with ViewBinding
        binding.btnLogin.setOnClickListener {
            (activity as? AuthActivity)?.showLogin()
        }

        binding.btnSignup.setOnClickListener {
            (activity as? AuthActivity)?.showSignup()
        }
    }
}
