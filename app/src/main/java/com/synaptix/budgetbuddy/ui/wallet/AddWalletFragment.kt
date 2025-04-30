package com.synaptix.budgetbuddy.ui.wallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAddWalletBinding
import dagger.hilt.android.AndroidEntryPoint

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

            viewModel.addWallet(name, balance, currency )
        }
    }

}