package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.walletSelectorPopUpBottomSheet.WalletSelectorBottomSheetFragment
import com.synaptix.budgetbuddy.ui.recurrence.RecurrenceBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream


@AndroidEntryPoint
class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by activityViewModels()

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var tempImageUri: Uri? = null

    // --- Lifecycle ---
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupCameraStuff()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        observeViewModel() // Re-observe ViewModel data to update UI
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setup Methods ---
    private fun setupUI() {
        setupCurrencySpinner()
        setupClickListeners()
    }

    //Handles the setup of the currency spinner.
    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("ZAR", "USD", "EUR", "GBP")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.rowSelectRecurrenceRate.setOnClickListener {
            RecurrenceBottomSheet().show(parentFragmentManager, "RecurrenceBottomSheet")
        }

        binding.rowSelectWallet.setOnClickListener {
            showWalletSelector()
        }

        binding.rowSelectLabel.setOnClickListener {
            showLabelSelector()
        }

        binding.rowSelectCategory.setOnClickListener {
            showCategorySelector()
        }

        val openDatePicker = {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                // Convert selection (epoch millis) to readable date
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                binding.edtTextDate.setText(formattedDate)
            }

            picker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
        }

        binding.rowSelectDate.setOnClickListener { openDatePicker() }
        binding.edtTextDate.setOnClickListener { openDatePicker() }

        binding.rowSelectPhoto.setOnClickListener { showImageSourceDialog() }

        binding.btnSave.setOnClickListener { saveTransaction() }

        binding.btnGoBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupCameraStuff() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempImageUri != null) {
                val bytes = uriToByteArray(tempImageUri!!)
                viewModel.setImageBytes(bytes)
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val bytes = uriToByteArray(it)
                viewModel.setImageBytes(bytes)
            }
        }

    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "transaction_${System.currentTimeMillis()}.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        }
                        tempImageUri = requireContext().contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        if (tempImageUri != null) {
                            takePictureLauncher.launch(tempImageUri!!)
                        } else {
                            Toast.makeText(requireContext(), "Failed to prepare image location.", Toast.LENGTH_SHORT).show()
                        }

                    }
                    1 -> pickImageLauncher.launch("image/*")
                }
            }.show()
    }

    private fun updateSelectedLabelChips(labels: List<Label>) {
        val selectedLabels = labels.filter { it.isSelected }

        val chipGroup = binding.chipGroupLabels
        chipGroup.removeAllViews()

        if (selectedLabels.isEmpty()) {
            chipGroup.visibility = View.GONE
            return
        }

        chipGroup.visibility = View.VISIBLE

        selectedLabels.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label.labelName
                isClickable = false
                isCheckable = false

                // Resolve the color from the theme attribute
                val chipBackgroundColor = MaterialColors.getColor(this.context, R.attr.bb_surfaceAlt, Color.TRANSPARENT)
                setChipBackgroundColor(ColorStateList.valueOf(chipBackgroundColor))

                val textColor = MaterialColors.getColor(this.context, R.attr.bb_primaryText, Color.TRANSPARENT)
                setTextColor(textColor)
            }
            chipGroup.addView(chip)
        }
    }

    // --- Save Logic ---
    private fun saveTransaction() {
        // TODO: Replace with actual data from UI
        val amount = binding.edtTextAmount.text.toString().toDoubleOrNull() ?: 0.0
        viewModel.amount.value = amount

        val date = binding.edtTextDate.text.toString()
        viewModel.date.value = date

        viewModel.note.value = binding.edtTextNote.text.toString()

        // Validate input
        if (viewModel.categoryId.value != null  ||
            viewModel.walletId.value != null ||
            viewModel.currency.value.isNullOrBlank() ||
            amount <= 0.0 ||
            date.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Please fill in all required fields correctly.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Launch coroutine to call suspend function
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.addTransaction()
                Toast.makeText(
                    requireContext(),
                    "Transaction saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to save transaction: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- Popup Navigation ---
    private fun showLabelSelector() {
        findNavController().navigate(R.id.action_addTransactionFragment_to_labelSelectorFragment)
    }

    private fun showWalletSelector() {
        val bottomSheet = WalletSelectorBottomSheetFragment()
        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }

    private fun showCategorySelector(){
        findNavController().navigate(R.id.action_addTransactionFragment_to_categorySelectorFragment)
    }

    // --- Observers ---
    private fun observeViewModel() {
        viewModel.selectedLabels.observe(viewLifecycleOwner) { selectedLabels ->
            Log.d("ViewModelsLabels", selectedLabels.toString())
            updateSelectedLabelChips(selectedLabels)
        }

        viewModel.walletId.observe(viewLifecycleOwner) { walletId ->
            Log.d("Wallet", "Selected Wallet ID: $walletId")
            // Update UI based on the selected wallet
        }

        viewModel.categoryId.observe(viewLifecycleOwner) { categoryId ->
            Log.d("Category", "Selected Category ID: $categoryId")
            // Update UI based on the selected category
        }

        viewModel.currency.observe(viewLifecycleOwner) { currency ->
            Log.d("Currency", "Selected Currency: $currency")
            // Update UI based on the selected currency
        }

        viewModel.amount.observe(viewLifecycleOwner) { amount ->
            Log.d("Amount", "Entered Amount: $amount")
            // Update UI based on the entered amount
        }

        viewModel.date.observe(viewLifecycleOwner) { date ->
            Log.d("Date", "Selected Date: $date")
            // Update UI based on the selected date
        }

        viewModel.note.observe(viewLifecycleOwner) { note ->
            Log.d("Note", "Entered Note: $note")
            // Update UI based on the entered note
        }

        viewModel.imageBytes.observe(viewLifecycleOwner) { bytes ->
            if (bytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imagePreview.setImageBitmap(bitmap)
                binding.imagePreview.visibility = View.VISIBLE
                Log.d("Image", "Image Preview: ${bitmap.width}x${bitmap.height}")
            } else {
                binding.imagePreview.setImageDrawable(null)
                binding.imagePreview.visibility = View.GONE
            }
        }

    }
}