package com.synaptix.budgetbuddy.presentation.ui.main

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navView: BottomNavigationView

    private val hiddenBottomNavFragments = setOf(
        R.id.navigation_transaction_select_label,
        R.id.navigation_transaction_add,
        R.id.navigation_wallet_add
        // add others here
    )

    private val showFabOnFragments = setOf(
        R.id.navigation_home,
        R.id.navigation_wallet_main,
        R.id.navigation_budget_main
        // add others here
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView = binding.navView

        // ✅ Replace setupWithNavController with custom listener to clear backstack
        navView.setOnItemSelectedListener { item ->
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true) // Prevent fragment reload
                .setPopUpTo(navController.graph.startDestinationId, false) // Clear stack
                .build()

            try {
                navController.navigate(item.itemId, null, options)
                true
            } catch (e: IllegalArgumentException) {
                false // Ignore if already on that destination
            }
        }

        // ✅ Navigate to add transaction screen from FAB
        binding.fabAddTransaction.setOnClickListener {
            navController.navigate(R.id.navigation_transaction_add)
        }

        // ✅ Hide bottom nav on certain fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Hide/show bottom nav bar
            if (destination.id in hiddenBottomNavFragments) {
                navView.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
            }

            // Hide/show FAB
            if (destination.id in showFabOnFragments) {
                binding.fabAddTransaction.show()
            } else {
                binding.fabAddTransaction.hide()
            }
        }
    }
}