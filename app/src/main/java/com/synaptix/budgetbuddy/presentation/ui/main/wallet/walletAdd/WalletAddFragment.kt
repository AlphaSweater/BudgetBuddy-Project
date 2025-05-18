package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd

import android.graphics.BitmapFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentTransactionAddBinding
import com.synaptix.budgetbuddy.databinding.FragmentWalletAddBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible

@AndroidEntryPoint
class WalletAddFragment : Fragment() {

    private var _binding: FragmentWalletAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletAddViewModel by viewModels()


    // --- Lifecycle ---
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setup Methods ---
    private fun setupUI() {
        setupCurrencySpinner()
    }

    //Handles the setup of the currency spinner.
    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("ZAR", "USD", "EUR", "GBP") // Add more currencies as needed
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupListeners() {
        // Back button
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Wallet name
        binding.edtWalletName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateWalletName(s?.toString() ?: "")
            }
        })

        // Initial amount
        binding.edtInitialAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateWalletAmount(s?.toString() ?: "0")
            }
        })

        // Notifications switch
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotifications(isChecked)
        }

        // Exclude from total switch
        binding.switchExcludeTotal.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateExcludeFromTotal(isChecked)
        }

        // Save button
        binding.btnSave.setOnClickListener {
            saveWallet()
        }
    }

    // --- Save Logic ---
    private fun saveWallet() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.updateWalletCurrency(binding.spinnerCurrency.selectedItem.toString())
                viewModel.addWallet()
                    .onSuccess {
                        Toast.makeText(
                            requireContext(),
                            "Wallet saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }
                    .onFailure { exception ->
                        Toast.makeText(
                            requireContext(),
                            exception.message ?: "Failed to save wallet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- Observers ---
    private fun observeViewModel() {
        // Observe validation errors
        viewModel.nameError.observe(viewLifecycleOwner) { error ->
            showError(error, binding.edtWalletName)
        }

        viewModel.currencyError.observe(viewLifecycleOwner) { error ->
            // Handle currency error if needed
        }

        viewModel.amountError.observe(viewLifecycleOwner) { error ->
            showError(error, binding.edtInitialAmount)
        }

        // Observe loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.btnSave.isEnabled = !isLoading
                // You can also show a loading indicator if needed
            }
        }
    }

    private fun showError(error: String?, view: View) {
        when (view) {
            binding.edtWalletName -> {
                binding.edtWalletName.error = error
            }
            binding.edtInitialAmount -> {
                binding.edtInitialAmount.error = error
            }
        }
    }
}