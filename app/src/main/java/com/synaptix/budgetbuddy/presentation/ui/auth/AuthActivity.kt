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

package com.synaptix.budgetbuddy.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.ActivityAuthBinding
import com.synaptix.budgetbuddy.presentation.ui.auth.landing.LandingFragment
import com.synaptix.budgetbuddy.presentation.ui.auth.login.LoginFragment
import com.synaptix.budgetbuddy.presentation.ui.auth.signup.SignupFragment
import com.synaptix.budgetbuddy.presentation.ui.main.MainActivity
import com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd.WalletAddFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    // ViewBinding instance for activity_auth.xml
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the LandingFragment when activity starts (only if not restoring state)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, LandingFragment())
                .commit()
        }
    }

    // Function to display the LoginFragment when called
    fun showLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, LoginFragment())
            .addToBackStack(null) // Adds to backstack so user can press back
            .commit()
    }

    // Function to display the SignupFragment when called
    fun showSignup() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, SignupFragment())
            .addToBackStack(null)
            .commit()
    }

    // Function to display WalletAddFragment for adding new wallet
    fun showAddWallet() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, WalletAddFragment())
            .addToBackStack(null)
            .commit()
    }

    // Function to navigate to MainActivity after successful login or signup
    fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Closes AuthActivity so user can't return to login/signup
    }
}
