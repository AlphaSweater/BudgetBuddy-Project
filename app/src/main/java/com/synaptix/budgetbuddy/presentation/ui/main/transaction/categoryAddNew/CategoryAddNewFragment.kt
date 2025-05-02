package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentCategoryAddNewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryAddNewFragment : Fragment() {

    private var _binding: FragmentCategoryAddNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryAddNewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryAddNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Bind the "Create" button

        binding.btnCreate.setOnClickListener {
            viewModel.categoryName.value = binding.editTxtText.text.toString()
            viewModel.createCategory()
        }

        // Hook up icon click listeners
        binding.btnBirthday.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_cake_24
            binding.imageView6.setImageResource(R.drawable.baseline_cake_24) // Preview
        }

        binding.btnBaby.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_child_friendly_24
            binding.imageView6.setImageResource(R.drawable.baseline_child_friendly_24) // Preview
        }

        binding.btnPlane.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_airplanemode_active_24
            binding.imageView6.setImageResource(R.drawable.baseline_airplanemode_active_24)
        }

        binding.btnFastFood.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_fastfood_24
            binding.imageView6.setImageResource(R.drawable.baseline_fastfood_24)
        }

        binding.btnLightBulb.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_lightbulb_24
            binding.imageView6.setImageResource(R.drawable.baseline_lightbulb_24)
        }

        binding.btnGas.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_local_gas_station_24
            binding.imageView6.setImageResource(R.drawable.baseline_local_gas_station_24)
        }

        binding.btnShopping.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_shopping_bag_24
            binding.imageView6.setImageResource(R.drawable.baseline_shopping_bag_24)
        }

        binding.btnComputer.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_computer_24
            binding.imageView6.setImageResource(R.drawable.baseline_computer_24)
        }

        binding.btnTheatre.setOnClickListener {
            viewModel.selectedIcon.value = R.drawable.baseline_theater_comedy_24
            binding.imageView6.setImageResource(R.drawable.baseline_theater_comedy_24)
        }

        binding.btnLightPink.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_light_pink
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnDarkPink.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_dark_pink
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnLightPurple.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_light_purple
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnDarkPurple.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_dark_purple
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnLightBlue.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_light_blue
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnDarkBlue.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_dark_blue
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnLightGreen.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_light_green
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnDarkGreen.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_dark_green
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnYellow.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_yellow
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnOrange.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_orange
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnGold.setOnClickListener {
            viewModel.selectedColor.value = R.color.cat_gold
            binding.imageView6.setImageResource(R.drawable.circle_button_background)
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Observe the creation result
        viewModel.eventCategoryCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.textView2.text = "Category Created Successfully!"
                binding.textView2.setTextColor(requireContext().getColor(R.color.profit_green))
                binding.editTxtText.text.clear()
                binding.imageView6.setImageResource(R.drawable.ic_circle_24)
            } else {
                binding.textView2.text = "Please enter a name and choose an icon"
                binding.textView2.setTextColor(requireContext().getColor(R.color.expense_red))
            }
        }
    }
}