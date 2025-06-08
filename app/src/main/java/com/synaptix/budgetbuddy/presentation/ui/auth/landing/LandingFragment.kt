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

package com.synaptix.budgetbuddy.presentation.ui.auth.landing

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.synaptix.budgetbuddy.presentation.ui.auth.AuthActivity
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAuthLandingBinding

// Fragment that displays the landing page for authentication (login or signup)

class LandingFragment : Fragment(R.layout.fragment_auth_landing) {

    private lateinit var binding: FragmentAuthLandingBinding
    private val viewModel: LandingViewModel by viewModels()

    // This method is called when the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate the ViewBinding to access UI elements
        binding = FragmentAuthLandingBinding.bind(view)

        // Set up click listeners for login and signup buttons
        binding.btnLogin.setOnClickListener {
            // Navigate to the login screen in the parent activity
            (activity as? AuthActivity)?.showLogin()
        }

        binding.btnSignup.setOnClickListener {
            // Navigate to the signup screen in the parent activity
            (activity as? AuthActivity)?.showSignup()
        }
    }
}
