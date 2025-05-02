package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.CategoryIn
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionSelectCategoryFragment : Fragment() {

    @Inject
    lateinit var getUserIdUseCase: GetUserIdUseCase
    private var _binding: FragmentTransactionSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()
    private val categoryViewmodel: TransactionSelectCategoryViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSelectCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupOnClickListeners()
        showExpenseCategories()
        instantiateDBS()
    }


    private fun setupRecyclerViews() {
        binding.recyclerViewExpenseCategory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIncomeCategory.layoutManager = LinearLayoutManager(requireContext())
    }
    private fun setupOnClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnExpenseToggle.setOnClickListener {
            showExpenseCategories()
        }

        binding.btnIncomeToggle.setOnClickListener {
            showIncomeCategories()
        }

        binding.btnAddCategory.setOnClickListener {
            showAddCategory()
        }
    }

    private fun showExpenseCategories() {
        binding.recyclerViewExpenseCategory.visibility = View.VISIBLE
        binding.recyclerViewIncomeCategory.visibility = View.GONE

        highlightToggle(binding.btnExpenseToggle, binding.btnIncomeToggle)
    }

    private fun showIncomeCategories() {
        binding.recyclerViewExpenseCategory.visibility = View.GONE
        binding.recyclerViewIncomeCategory.visibility = View.VISIBLE

        highlightToggle(binding.btnIncomeToggle, binding.btnExpenseToggle)
    }

    private fun highlightToggle(selected: TextView, unselected: TextView) {
        selected.setBackgroundResource(R.drawable.toggle_selected)
        unselected.setBackgroundResource(android.R.color.transparent)
    }

    private fun showAddCategory() {
        findNavController().navigate(R.id.navigation_category_add_new)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getUserId(): Int {
        var userid = 0
        lifecycleScope.launch {
             userid = getUserIdUseCase.execute()
        }
        return userid
    }

    private fun instantiateDBS() {
        categoryViewmodel.loadCategories(getUserId())

        categoryViewmodel.categories.observe(viewLifecycleOwner) { categoryEntities ->
            val categories = categoryEntities.map {
                CategoryIn(
                    it.category_id,
                    it.user_id ?: 0,
                    it.name,
                    it.type,
                    it.icon,
                    it.colour
                )
            }
            val (expenseCategories, incomeCategories) = categories.partition { it.categoryType == "expense" }

            binding.recyclerViewExpenseCategory.adapter = TransactionSelectCategoryAdapter(expenseCategories) { category ->
                viewModel.categoryId.value = category.categoryId
            }

            binding.recyclerViewIncomeCategory.adapter = TransactionSelectCategoryAdapter(incomeCategories) { category ->
                viewModel.categoryId.value = category.categoryId
            }
        }

    }

    fun getDrawableId(name: String): Int {
        val cleanName = name.removePrefix("@drawable/")
        return resources.getIdentifier(cleanName, "drawable", requireContext().packageName)
    }

    fun getColorId(name: String): Int {
        val cleanName = name.removePrefix("@color/")
        return resources.getIdentifier(cleanName, "color", requireContext().packageName)
    }
}