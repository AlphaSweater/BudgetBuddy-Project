package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.walletSelectorPopUpBottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentSelectWalletBottomSheetBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.AddTransactionViewModel

class WalletSelectorBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSelectWalletBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by activityViewModels()

    private lateinit var walletAdapter: WalletAdapter

    private val walletList = listOf(
        Wallet(1, 1, "Main Wallet", "ZAR", 1200.50),
        Wallet(2, 1, "Savings", "ZAR", 8500.00),
        Wallet(3, 1, "Crypto", "ZAR", 0.004)
    )

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
        _binding = FragmentSelectWalletBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        walletAdapter = WalletAdapter(walletList) { walletId ->
            viewModel.walletId.value = walletId // Update ViewModel with selected wallet ID
            dismiss()
        }

        binding.walletRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = walletAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}