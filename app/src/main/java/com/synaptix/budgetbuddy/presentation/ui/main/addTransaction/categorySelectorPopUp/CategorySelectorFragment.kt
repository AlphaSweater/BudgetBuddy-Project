package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.categorySelectorPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.databinding.FragmentSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.AddTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategorySelectorFragment : Fragment() {

    @Inject
    lateinit var getUserIdUseCase: GetUserIdUseCase
    private var _binding: FragmentSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by activityViewModels()
    private val categoryViewmodel: CategorySelectorViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectCategoryBinding.inflate(inflater, container, false)
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
        findNavController().navigate(R.id.navigation_add_new_category)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun getUserId(): Int {
        return getUserIdUseCase.execute()
    }

    private fun instantiateDBS() {
        categoryViewmodel.loadCategories(1)

        categoryViewmodel.categories.observe(viewLifecycleOwner) { categoryEntities ->
            val categories = categoryEntities.map {
                Category(
                    it.category_id,
                    it.user_id ?: 0,
                    it.name,
                    it.type,
                    getDrawableId(it.icon),
                    getColorId(it.colour)
                )
            }
            val (expenseCategories, incomeCategories) = categories.partition { it.categoryType == "expense" }

            binding.recyclerViewExpenseCategory.adapter = CategoryAdapter(expenseCategories) { category ->
                viewModel.categoryId.value = category.categoryId
            }

            binding.recyclerViewIncomeCategory.adapter = CategoryAdapter(incomeCategories) { category ->
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