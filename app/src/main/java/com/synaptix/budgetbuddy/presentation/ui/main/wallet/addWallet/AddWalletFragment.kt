package com.synaptix.budgetbuddy.presentation.ui.main.wallet.addWallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.synaptix.budgetbuddy.databinding.FragmentAddWalletBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddWalletFragment : Fragment() {

    private var _binding: FragmentAddWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddWalletViewModel by viewModels()


    companion object {
        fun newInstance() = AddWalletFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //oncliick listener that saves user input and injects into usecase
        binding.btnSave.setOnClickListener {
            val name = binding.edtWalletName.text.toString()
            val currency = binding.edtCurrency.text.toString()
            val balanceText = binding.edtInitialAmount.text.toString()

            // check if wallet name is empty
            if (name.isEmpty()) {
                binding.edtWalletName.error = "Wallet name is required"
                return@setOnClickListener
            }

            // Check if currency is empty
            if (currency.isEmpty()) {
                binding.edtCurrency.error = "Currency is required"
                return@setOnClickListener
            }

            if (balanceText.isEmpty()){
                binding.edtCurrency.error = "Currency is required"
                return@setOnClickListener
            }

            //concers balancetext into a double
            val balance = balanceText.toDoubleOrNull()

            // Check if balance is empty
            if (balance == null) {
                binding.edtInitialAmount.error = "Please enter a valid number"
                return@setOnClickListener
            }

            // Launch coroutine to call suspend function
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.addWallet(name, balance, currency )
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
    }

}