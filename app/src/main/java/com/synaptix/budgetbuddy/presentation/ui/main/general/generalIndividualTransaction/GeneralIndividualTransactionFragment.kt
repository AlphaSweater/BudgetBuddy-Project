package com.synaptix.budgetbuddy.presentation.ui.main.general.generalIndividualTransaction

import android.graphics.BitmapFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentGeneralIndividualTransactionBinding
import com.synaptix.budgetbuddy.databinding.FragmentTransactionAddBinding
import com.synaptix.budgetbuddy.presentation.ui.main.home.HomeMainViewModel

class GeneralIndividualTransactionFragment : Fragment() {

    private var _binding: FragmentGeneralIndividualTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeMainViewModel by activityViewModels()

    // --- Lifecycle ---
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralIndividualTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nameText = view?.findViewById<TextView>(R.id.individualCategoryName)
        val amountTop = view?.findViewById<TextView>(R.id.amount)
        val walletText = view?.findViewById<TextView>(R.id.walletName)
        val amountRow = view?.findViewById<TextView>(R.id.textAmount)
        val noteRow = view?.findViewById<TextView>(R.id.textNote)
        val startDateText = view?.findViewById<TextView>(R.id.textStartDate)
        val recurrenceText = view?.findViewById<TextView>(R.id.textRecurrence)
        val photoPreview = view?.findViewById<ImageView>(R.id.transactionImage)

        viewModel.selectedTransaction.observe(viewLifecycleOwner) { transaction ->
            transaction?.let {
                nameText?.text = it.category?.categoryName ?: "Unknown"
                amountTop?.text = "R ${"%.2f".format(it.amount)}"
                walletText?.text = it.wallet?.walletName ?: "No Wallet"
                amountRow?.text = "R ${"%.2f".format(it.amount)}"
                noteRow?.text = it.note ?: "No note provided"
                startDateText?.text = it.date
                recurrenceText?.text = it.recurrenceRate ?: "None"

                // Optional: display the photo if available
                if (it.photo != null) {
                    val bitmap = BitmapFactory.decodeByteArray(it.photo, 0, it.photo.size)
                    photoPreview?.setImageBitmap(bitmap)
                } else {
                    photoPreview?.visibility = View.GONE
                }
            }
        }

        view?.findViewById<ImageButton>(R.id.btnGoBack)?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}