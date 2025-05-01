package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelectorPopUp

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
import com.synaptix.budgetbuddy.databinding.FragmentSelectLabelBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.AddTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class LabelSelectorFragment : Fragment() {

    private var _binding: FragmentSelectLabelBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by activityViewModels()
    private val labelViewModel: LabelSelectorViewModel by viewModels()

    private lateinit var labelAdapter: LabelAdapter

    private val baseLabelNames = listOf("Salary", "Gift", "Bonus", "Investment", "Cashback")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectLabelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupOnClickListeners()

        labelViewModel.loadLabelsForUser(userId = 1)

        lifecycleScope.launchWhenStarted {
            labelViewModel.labels.collect { labels ->
                updateLabels(labels)
            }
        }
    }

    private fun setupRecyclerView() {
        val selectedLabels = viewModel.selectedLabels.value ?: listOf<Label>() // Default to an empty list if null

        val labels = baseLabelNames.map { labelName ->
            val isSelected = selectedLabels.any { it.labelName == labelName && it.isSelected }
            Label(
                labelName = labelName,
                transactionInfo = "0 transactions in 0 wallets",
                isSelected = isSelected
            )
        }.toMutableList()

        labelAdapter = LabelAdapter(labels) {}

        binding.recyclerViewLabels.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = labelAdapter
        }
    }

    private fun setupOnClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val selected = ArrayList(labelAdapter.getSelectedLabels())
        viewModel.selectedLabels.value = selected
        _binding = null
    }

    private fun updateLabels(entities: List<LabelEntity>) {
        val selectedLabels = viewModel.selectedLabels.value ?: emptyList()

        val labelList = entities.map { entity ->
            val isSelected = selectedLabels.any { it.labelName == entity.name && it.isSelected }
            Label(
                labelName = entity.name,
                transactionInfo = "0 transactions in 0 wallets",
                isSelected = isSelected
            )
        }

        labelAdapter.updateLabels(labelList)
    }
    
    
}
