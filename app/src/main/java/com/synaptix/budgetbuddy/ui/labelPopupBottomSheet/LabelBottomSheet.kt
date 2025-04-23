package com.synaptix.budgetbuddy.ui.labelPopupBottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.synaptix.budgetbuddy.R
data class Label(val name: String)

class LabelBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_label_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewLabels)

        val labelList = listOf(
            Label("Salary"),
            Label("Gift"),
            Label("Bonus"),
            Label("Interest"),
            Label("Investment"),
            Label("Refund"),
            Label("Cashback")
        )

        val adapter = LabelAdapter(labelList) { selectedLabel ->
            // Handle item click if needed, or communicate it back
            dismiss()
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
}
