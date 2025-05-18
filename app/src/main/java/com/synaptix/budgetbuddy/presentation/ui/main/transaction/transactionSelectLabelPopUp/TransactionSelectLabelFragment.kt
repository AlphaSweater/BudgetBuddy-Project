package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectLabelPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.data.entity.LabelEntity
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectLabelBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionSelectLabelFragment : Fragment() {

    private var _binding: FragmentTransactionSelectLabelBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()
    private val labelViewModel: TransactionSelectLabelViewModel by viewModels()

    private val labelAdapter by lazy {
        TransactionSelectLabelAdapter { selectedLabels ->
            viewModel.selectedLabels.value = ArrayList(selectedLabels)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSelectLabelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeLabels()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewLabels.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = labelAdapter
        }

        // Initialize with selected labels from ViewModel
        val selectedLabels = viewModel.selectedLabels.value ?: emptyList()
        val initialLabels = selectedLabels.map { it.copy() }
        labelAdapter.submitList(initialLabels)
    }

    private fun setupClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeLabels() {
        viewLifecycleOwner.lifecycleScope.launch {
            labelViewModel.loadLabelsForUser(userId = 1) // TODO: Get actual user ID
            labelViewModel.labels.collectLatest { labelEntities ->
                val selectedLabels = viewModel.selectedLabels.value ?: emptyList()
                val labels = labelEntities.map { entity ->
                    Label(
                        labelName = entity.name,
                        transactionInfo = "0 transactions in 0 wallets", // TODO: Get actual transaction info
                        isSelected = selectedLabels.any { it.labelName == entity.name }
                    )
                }
                labelAdapter.submitList(labels)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
