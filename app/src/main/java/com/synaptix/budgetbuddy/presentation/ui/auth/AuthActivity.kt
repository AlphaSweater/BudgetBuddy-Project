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

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, LandingFragment())
                .commit()
        }
    }

    fun showLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, LoginFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showSignup() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, SignupFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showAddWallet(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, WalletAddFragment())
            .addToBackStack(null)
            .commit()
    }



    fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
