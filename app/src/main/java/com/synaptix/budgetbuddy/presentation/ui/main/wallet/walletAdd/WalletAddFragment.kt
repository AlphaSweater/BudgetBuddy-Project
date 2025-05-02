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
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setup Methods ---
    private fun setupUI() {
        setupCurrencySpinner()
        setupOnClickListeners()
    }

    //Handles the setup of the currency spinner.
    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("ZAR")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupOnClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnWalletEdit.setOnClickListener {
//            findNavController().navigate(R.id.action_walletAddFragment_to_walletSelectIconFragment)
        }

        binding.btnSave.setOnClickListener {
            saveWallet()
            findNavController().popBackStack()
        }

    }

    // --- Save Logic ---
    private fun saveWallet() {
        viewModel.walletName.value = binding.edtWalletName.text.toString()
        viewModel.walletCurrency.value = binding.spinnerCurrency.selectedItem.toString()
        viewModel.walletAmount.value = binding.edtInitialAmount.text.toString().toDoubleOrNull() ?: 0.0

        // Validate input
        if (viewModel.walletName.value.isNullOrBlank()   ||
            viewModel.walletCurrency.value.isNullOrBlank() ||
            viewModel.walletAmount.value == null
        ) {
            Toast.makeText(
                requireContext(),
                "Please fill in all required fields correctly.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Launch coroutine to call suspend function
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.addWallet()
                Toast.makeText(
                    requireContext(),
                    "Wallet saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Wallet to save transaction: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- Observers ---
    private fun observeViewModel() {
        viewModel.walletName.observe(viewLifecycleOwner) { walletName ->
            Log.d("Wallet", "Selected Wallet Name: $walletName")
            // Update UI based on the selected wallet
        }

        viewModel.walletCurrency.observe(viewLifecycleOwner) { walletCurrency ->
            Log.d("Wallet", "Selected Wallet Currency: $walletCurrency")
            // Update UI based on the selected wallet
        }

        viewModel.walletAmount.observe(viewLifecycleOwner) { walletAmount ->
            Log.d("Wallet", "Selected Wallet Amount: $walletAmount")
            // Update UI based on the selected wallet
        }
    }
}