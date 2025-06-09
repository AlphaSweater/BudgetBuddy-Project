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
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.extentions.getThemeColor
import java.math.BigDecimal
import java.math.RoundingMode

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

            switchExcludeTotal.setOnCheckedChangeListener { _, isChecked ->
                sharedViewModel.updateExcludeFromTotal(isChecked)
            }

            btnSave.setOnClickListener {
                saveWallet()
            }
        }
    }


    // --- Save Logic ---
    private fun saveWallet() {
        val walletName = binding.edtWalletName.text.toString()
        sharedViewModel.setWalletName(walletName)

        val walletCurrency = binding.spinnerCurrency.selectedItem.toString()
        sharedViewModel.setWalletCurrency(walletCurrency)

        val walletAmount = sharedViewModel.walletAmount.value ?: 0.0
        val walletMinGoal = sharedViewModel.walletMinGoal.value ?: 0.0
        val walletMaxGoal = sharedViewModel.walletMaxGoal.value ?: 0.0

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

    private fun setupTextWatchers() {
        setupAmountWatcher()
        setupMinAmountWatcher()
        setupMaxAmountWatcher()
    }

    private fun setupAmountWatcher() {
        var current = ""

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (newText != current) {
                    binding.edtTextAmount.removeTextChangedListener(this)

                    // Handle empty input
                    if (newText.isEmpty()) {
                        current = ""
                        binding.edtTextAmount.setText("")
                        sharedViewModel.setWalletAmount(0.0)
                        return
                    }

                    // Remove any non-digit characters
                    val cleanString = newText.replace("[^\\d]".toRegex(), "")

                    // Check if the number is too long (12 digits before decimal + 2 after)
                    if (cleanString.length > 14) {
                        // Keep only the first 14 digits
                        val truncatedString = cleanString.substring(0, 14)
                        val parsed = BigDecimal(truncatedString)
                            .setScale(2, RoundingMode.FLOOR)
                            .divide(BigDecimal(100))

                        val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                        current = formatted
                        binding.edtTextAmount.setText(formatted)
                        binding.edtTextAmount.setSelection(formatted.length)
                        sharedViewModel.setWalletAmount(parsed.toDouble())
                    } else {
                        val parsed = if (cleanString.isNotEmpty()) {
                            BigDecimal(cleanString)
                                .setScale(2, RoundingMode.FLOOR)
                                .divide(BigDecimal(100))
                        } else {
                            BigDecimal.ZERO
                        }

                        if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                            current = ""
                            binding.edtTextAmount.setText("")
                            sharedViewModel.setWalletAmount(0.0)
                        } else {
                            val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                            current = formatted
                            binding.edtTextAmount.setText(formatted)
                            binding.edtTextAmount.setSelection(formatted.length)
                            sharedViewModel.setWalletAmount(parsed.toDouble())  // This line was missing
                        }
                    }

                    binding.edtTextAmount.addTextChangedListener(this)
                }
            }
        }
        binding.edtTextAmount.addTextChangedListener(watcher)
        binding.edtTextAmount.tag = watcher
    }

    private fun setupMinAmountWatcher() {
        var current = ""

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (newText != current) {
                    binding.edtMinAmount.removeTextChangedListener(this)

                    // Handle empty input
                    if (newText.isEmpty()) {
                        current = ""
                        binding.edtMinAmount.setText("")
                        sharedViewModel.setWalletMinGoal(0.0)
                        return
                    }

                    // Remove any non-digit characters
                    val cleanString = newText.replace("[^\\d]".toRegex(), "")

                    // Check if the number is too long (12 digits before decimal + 2 after)
                    if (cleanString.length > 14) {
                        // Keep only the first 14 digits
                        val truncatedString = cleanString.substring(0, 14)
                        val parsed = BigDecimal(truncatedString)
                            .setScale(2, RoundingMode.FLOOR)
                            .divide(BigDecimal(100))

                        val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                        current = formatted
                        binding.edtMinAmount.setText(formatted)
                        binding.edtMinAmount.setSelection(formatted.length)
                        sharedViewModel.setWalletMinGoal(parsed.toDouble())
                    } else {
                        val parsed = if (cleanString.isNotEmpty()) {
                            BigDecimal(cleanString)
                                .setScale(2, RoundingMode.FLOOR)
                                .divide(BigDecimal(100))
                        } else {
                            BigDecimal.ZERO
                        }

                        if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                            current = ""
                            binding.edtMinAmount.setText("")
                            sharedViewModel.setWalletMinGoal(0.0)
                        } else {
                            val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                            current = formatted
                            binding.edtMinAmount.setText(formatted)
                            binding.edtMinAmount.setSelection(formatted.length)
                            sharedViewModel.setWalletMinGoal(parsed.toDouble())
                        }
                    }

                    binding.edtMinAmount.addTextChangedListener(this)
                }
            }
        }
        binding.edtMinAmount.addTextChangedListener(watcher)
        binding.edtMinAmount.tag = watcher
    }

    private fun setupMaxAmountWatcher() {
        var current = ""

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (newText != current) {
                    binding.edtMaxAmount.removeTextChangedListener(this)

                    // Handle empty input
                    if (newText.isEmpty()) {
                        current = ""
                        binding.edtMaxAmount.setText("")
                        sharedViewModel.setWalletMaxGoal(0.0)
                        return
                    }

                    // Remove any non-digit characters
                    val cleanString = newText.replace("[^\\d]".toRegex(), "")

                    // Check if the number is too long (12 digits before decimal + 2 after)
                    if (cleanString.length > 14) {
                        // Keep only the first 14 digits
                        val truncatedString = cleanString.substring(0, 14)
                        val parsed = BigDecimal(truncatedString)
                            .setScale(2, RoundingMode.FLOOR)
                            .divide(BigDecimal(100))

                        val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                        current = formatted
                        binding.edtMaxAmount.setText(formatted)
                        binding.edtMaxAmount.setSelection(formatted.length)
                        sharedViewModel.setWalletMaxGoal(parsed.toDouble())
                    } else {
                        val parsed = if (cleanString.isNotEmpty()) {
                            BigDecimal(cleanString)
                                .setScale(2, RoundingMode.FLOOR)
                                .divide(BigDecimal(100))
                        } else {
                            BigDecimal.ZERO
                        }

                        if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                            current = ""
                            binding.edtMaxAmount.setText("")
                            sharedViewModel.setWalletMaxGoal(0.0)
                        } else {
                            val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                            current = formatted
                            binding.edtMaxAmount.setText(formatted)
                            binding.edtMaxAmount.setSelection(formatted.length)
                            sharedViewModel.setWalletMaxGoal(parsed.toDouble())
                        }
                    }

                    binding.edtMaxAmount.addTextChangedListener(this)
                }
            }
        }
        binding.edtMaxAmount.addTextChangedListener(watcher)
        binding.edtMaxAmount.tag = watcher
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