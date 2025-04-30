package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentLabelSelectorBinding

class LabelSelectorFragment : Fragment() {

    private var _binding: FragmentLabelSelectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var labelAdapter: LabelAdapter

    private val baseLabelNames = listOf("Salary", "Gift", "Bonus", "Investment", "Cashback")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLabelSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupOnClickListeners()
    }

    private fun setupRecyclerView() {
        val selectedLabels = arguments?.getSerializable("currentLabels") as? ArrayList<Label> ?: arrayListOf()

        val labels = baseLabelNames.map { labelName ->
            val isSelected = selectedLabels.any { it.labelName == labelName && it.isSelected }
            Label(
                labelName = labelName,
                transactionInfo = "0 transactions in 0 wallets",
                isSelected = isSelected
            )
        }.toMutableList()

        labelAdapter = LabelAdapter(labels) {
            // Optional: handle live preview updates
        }

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
        val result = Bundle().apply {
            putSerializable("selectedLabels", selected)
        }
        parentFragmentManager.setFragmentResult("labelSelectorResult", result)
        _binding = null
    }

}
