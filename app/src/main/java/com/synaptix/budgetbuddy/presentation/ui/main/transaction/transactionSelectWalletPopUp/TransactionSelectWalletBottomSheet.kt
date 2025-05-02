package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectWalletBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.R

@AndroidEntryPoint
class TransactionSelectWalletBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentTransactionSelectWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()
    private val walletViewModel: TransactionSelectWalletViewModel by viewModels()
    private lateinit var transactionSelectWalletAdapter: TransactionSelectWalletAdapter


    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val halfScreenHeight = (resources.displayMetrics.heightPixels * 0.5).toInt()

            it.layoutParams.height = halfScreenHeight
            it.requestLayout()

            behavior.peekHeight = halfScreenHeight
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSelectWalletBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        walletViewModel.loadWallets()
        //AI assisted with this which calls the function in the viewModel and passes the data to the adapter
        walletViewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            transactionSelectWalletAdapter = TransactionSelectWalletAdapter(walletList) { walletId ->
                viewModel.walletId.value = walletId
                dismiss()
            }
            binding.walletRecyclerView.adapter = transactionSelectWalletAdapter
        }
    }

private fun setupRecyclerView() {
    binding.walletRecyclerView.layoutManager = LinearLayoutManager(requireContext())
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}