package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentWalletAddBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.text.Editable
import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.navGraphViewModels

@AndroidEntryPoint
class WalletAddFragment : Fragment() {

    private var _binding: FragmentWalletAddBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: WalletAddViewModel by navGraphViewModels(R.id.ind_wallet_navigation_graph) { defaultViewModelProviderFactory }

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
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setup Methods ---
    private fun setupUI() {
        setupCurrencySpinner()
        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("ZAR")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupClickListeners() {
        with(binding) {
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            switchNotifications.setOnCheckedChangeListener { _, isChecked ->
                sharedViewModel.updateEnableNotifications(isChecked)
            }

            switchExcludeTotal.setOnCheckedChangeListener { _, isChecked ->
                sharedViewModel.updateExcludeFromTotal(isChecked)
            }

            btnSave.setOnClickListener {
                saveWallet()
            }
        }
    }

    private fun setupTextWatchers() {
        with(binding){
            binding.edtWalletName.doAfterTextChanged { text ->
                sharedViewModel.setWalletName(text.toString())
            }

            binding.edtTextAmount.doAfterTextChanged { text ->
                sharedViewModel.setWalletAmount(text.toString())
            }
        }
    }

    // --- Save Logic ---
    private fun saveWallet() {
        val walletName = binding.edtWalletName.text.toString()
        sharedViewModel.setWalletName(walletName)

        val walletAmount = binding.edtTextAmount.text.toString()
        sharedViewModel.setWalletAmount(walletAmount)

        val walletMinGoal = binding.edtMinAmount.text.toString()
        sharedViewModel.setWalletMinGoal(walletMinGoal)

        val walletMaxGoal = binding.edtMaxAmount.text.toString()
        sharedViewModel.setWalletMaxGoal(walletMaxGoal)

        val walletCurrency = binding.spinnerCurrency.selectedItem.toString()
        sharedViewModel.setWalletCurrency(walletCurrency)

        // Show validation errors if any
        sharedViewModel.showValidationErrors()

        // Check if form is valid before proceeding
        if (!sharedViewModel.validateForm()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                sharedViewModel.addWallet()
            } catch (e: Exception) {
                showError("Failed to save wallet: ${e.message}")
            }
        }
    }

    // --- Observers ---
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    sharedViewModel.uiState.collect { state ->
                        handleUiState(state)
                    }
                }
                
                // Collect validation state
                launch {
                    sharedViewModel.validationState.collect { state ->
                        handleValidationState(state)
                    }
                }
            }
        }
    }

    private fun handleUiState(state: WalletAddViewModel.UiState) {
        when (state) {
            is WalletAddViewModel.UiState.Loading -> {
                binding.btnSave.isEnabled = false
            }
            is WalletAddViewModel.UiState.Success -> {
                showSuccess("Wallet added successfully")
                findNavController().popBackStack()
            }
            is WalletAddViewModel.UiState.Error -> {
                binding.btnSave.isEnabled = false
                showError(state.message)
            }
            else -> {
                binding.btnSave.isEnabled = true
            }
        }
    }

    private fun handleValidationState(state: WalletAddViewModel.ValidationState) {
        with(binding) {
            // Show/hide error messages for each field
            textNameError.apply {
                text = state.walletNameError
                visibility = if (state.shouldShowErrors && state.walletNameError != null) View.VISIBLE else View.GONE
            }

            textAmountError.apply {
                text = state.walletAmountError
                visibility = if (state.shouldShowErrors && state.walletAmountError != null) View.VISIBLE else View.GONE
            }

//            textCurrencyError.apply {
//                text = state.walletCurrencyError
//                visibility = if (state.shouldShowErrors && state.walletCurrencyError != null) View.VISIBLE else View.GONE
//            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .show()
    }
}