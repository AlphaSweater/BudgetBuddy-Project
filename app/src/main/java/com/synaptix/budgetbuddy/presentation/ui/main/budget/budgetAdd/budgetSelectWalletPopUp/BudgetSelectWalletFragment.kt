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

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectWalletPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentBudgetSelectWalletBinding
import com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.BudgetAddViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetSelectWalletFragment : Fragment() {

    private var _binding: FragmentBudgetSelectWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetAddViewModel by activityViewModels()
    private val walletViewModel: BudgetSelectWalletViewModel by viewModels()
    private lateinit var budgetSelectWalletAdapter: BudgetSelectWalletAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetSelectWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupOnClickListeners()
        observeWallets()
    }

    // Sets up the RecyclerView with a LinearLayoutManager
    private fun setupRecyclerView() {
        binding.walletRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    // Sets up the back button click listener
    private fun setupOnClickListeners() {
        binding.btnGoBack?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Observes the wallet list and updates the adapter
    private fun observeWallets() {
        walletViewModel.loadWallets()
        walletViewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            budgetSelectWalletAdapter = BudgetSelectWalletAdapter(walletList) { wallet ->
                viewModel.wallet.value = wallet
                findNavController().popBackStack()
            }
            binding.walletRecyclerView.adapter = budgetSelectWalletAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
