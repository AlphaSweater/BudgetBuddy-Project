package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R

class LabelSelectorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LabelAdapter

    // Your base label names
    private val baseLabelNames = listOf("Salary", "Gift", "Bonus", "Investment", "Cashback")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_label_selector, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewLabels)

        val previouslySelected = arguments?.getStringArrayList("currentLabels") ?: arrayListOf()

        val labels = baseLabelNames.map { labelName ->
            Label(
                labelName = labelName,
                transactionInfo = "0 transactions in 0 wallets",
                isSelected = previouslySelected.contains(labelName)
            )
        }.toMutableList()

        adapter = LabelAdapter(labels) {
            // You can do live UI preview updates here if needed
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val selected = ArrayList(
            adapter.getSelectedLabels().map { it.labelName }
        )
        val result = Bundle().apply {
            putStringArrayList("selectedLabels", selected)
        }
        parentFragmentManager.setFragmentResult("labelSelectorResult", result)
    }
}

