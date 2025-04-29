package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelectorDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.synaptix.budgetbuddy.R

class LabelSelectorDialogFragment : DialogFragment() {

    private lateinit var labelAdapter: LabelAdapter
    private var preselected: List<String> = emptyList()
    private var onLabelsSelected: ((List<String>) -> Unit)? = null

    fun setOnLabelsSelected(callback: (List<String>) -> Unit) {
        onLabelsSelected = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preselected = arguments?.getStringArrayList(ARG_PRESELECTED)?.toList() ?: emptyList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_label_selector, null)

        val labelList = listOf("Salary", "Gift", "Bonus", "Interest", "Investment", "Refund", "Cashback")
        labelAdapter = LabelAdapter(labelList, preselected.toSet())

        view.findViewById<RecyclerView>(R.id.recyclerLabels).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = labelAdapter
        }

        builder.setView(view)
            .setTitle("Select Labels")
            .setPositiveButton("Done") { _, _ ->
                onLabelsSelected?.invoke(labelAdapter.getSelectedLabels())
            }
            .setNegativeButton("Cancel", null)

        return builder.create()
    }

    companion object {
        private const val ARG_PRESELECTED = "arg_preselected"

        fun newInstance(preselected: List<String>): LabelSelectorDialogFragment {
            val fragment = LabelSelectorDialogFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_PRESELECTED, ArrayList(preselected))
            fragment.arguments = args
            return fragment
        }
    }
}
