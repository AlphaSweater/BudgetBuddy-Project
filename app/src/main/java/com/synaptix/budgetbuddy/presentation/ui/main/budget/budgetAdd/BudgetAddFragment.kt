package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentBudgetAddBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetAddFragment : Fragment() {

    private var _binding: FragmentBudgetAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetAddViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
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
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("ZAR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener { saveTransaction() }
        binding.btnGoBack.setOnClickListener { findNavController().popBackStack() }
        binding.rowSelectCategory.setOnClickListener { showCategorySelector() }
        binding.rowSelectWallet.setOnClickListener { showWalletSelector() }
    }

    // --- Save Logic ---
    private fun saveTransaction() {
        // Update ViewModel LiveData
        viewModel.budgetName.value = binding.budgetName.text.toString()
        viewModel.budgetAmount.value = binding.amount.text.toString().toDoubleOrNull() ?: 0.0

        // Validate input
        if (viewModel.category.value == null  ||
            viewModel.wallet.value == null ||
            viewModel.budgetName.value.isNullOrBlank() ||
            viewModel.budgetAmount.value!! <= 0.0
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
                viewModel.addBudget()
                Toast.makeText(
                    requireContext(),
                    "Budget added successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
                viewModel.reset()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to save transaction: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- Popup Navigation ---
    private fun showWalletSelector() {
        findNavController().navigate(R.id.action_budgetAddFragment_to_budgetSelectWalletFragment)
    }

    private fun showCategorySelector(){
        findNavController().navigate(R.id.action_budgetAddFragment_to_budgetSelectCategoryFragment)
    }

    // --- Update Methods ---

    private fun updateSelectedCategory(categoryName: String) {
        if (categoryName.isBlank()) {
            binding.textSelectedCategoryName.text = "No category selected"
            return
        }
        binding.textSelectedCategoryName.text = categoryName
    }

    private fun updateSelectedWallet(walletName: String) {
        if (walletName.isBlank()) {
            binding.textSelectedWalletName.text = "No wallet selected"
            return
        }
        binding.textSelectedWalletName.text = walletName
    }

    // --- Observers ---
    private fun observeViewModel() {

        viewModel.category.observe(viewLifecycleOwner) { category ->
            updateSelectedCategory(category?.categoryName ?: "")
            Log.d("Category", "Selected Category: $category")
        }

        viewModel.wallet.observe(viewLifecycleOwner) { wallet ->
            updateSelectedWallet(wallet?.walletName ?: "")
            Log.d("Wallet", "Selected Wallet ID: $wallet")
        }

        viewModel.budgetAmount.observe(viewLifecycleOwner) { amount ->
            Log.d("Amount", "Entered Amount: $amount")
        }

        viewModel.budgetName.observe(viewLifecycleOwner) { name ->
            Log.d("Note", "Entered Note: $name")
        }
    }
}
