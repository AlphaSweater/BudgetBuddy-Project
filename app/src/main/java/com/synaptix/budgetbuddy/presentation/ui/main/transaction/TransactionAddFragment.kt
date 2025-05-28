package com.synaptix.budgetbuddy.presentation.ui.main.transaction

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentTransactionAddBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.synaptix.budgetbuddy.core.model.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.ImageView
import com.synaptix.budgetbuddy.core.model.RecurrenceData
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.extentions.getThemeColor
import kotlin.toString
import com.synaptix.budgetbuddy.core.model.Label
import kotlinx.coroutines.delay

@AndroidEntryPoint
class TransactionAddFragment : Fragment() {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var tempImageUri: Uri? = null

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Lifecycle ---
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Fragment Lifecycle ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupImagePickers()
        observeViewModel()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    override fun onResume() {
        super.onResume()
        restoreState()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Setup Methods ---
    private fun setupViews() {
        setupCurrencySpinner()
        setupClickListeners()
        setupTextWatchers()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun restoreState() {
        if (!viewModel.saveState.value!!) {
            reset()
            return
        }

        binding.apply {
            edtTextAmount.setText(viewModel.amount.value?.toString() ?: "")
            edtTextNote.setText(viewModel.note.value)
            showImagePreview(viewModel.imageBytes.value)
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles the setup of the currency spinner.
    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("ZAR")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCurrency.adapter = adapter
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles the setup of click listeners for various UI elements.
    private fun setupClickListeners() {
        with(binding) {
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnClear.setOnClickListener {
                reset()
            }

            rowSelectCategory.setOnClickListener {
                showCategorySelector()
            }

            rowSelectWallet.setOnClickListener {
                showWalletSelector()
            }

            rowSelectLabels.setOnClickListener {
                showLabelsSelector()
            }

            rowSelectDate.setOnClickListener {
                showDatePicker()
            }

            rowSelectRecurrenceRate.setOnClickListener {
                showRecurrenceSelector()
            }

            rowSelectPhoto.setOnClickListener {
                showImageSourceDialog()
            }

            btnSave.setOnClickListener {
                saveTransaction()
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles the setup of text watchers for input fields.
    private fun setupTextWatchers() {
        binding.edtTextAmount.doAfterTextChanged { text ->
            viewModel.setAmount(text.toString().toDoubleOrNull())
        }

        binding.edtTextNote.doAfterTextChanged { text ->
            viewModel.setNote(text.toString())
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles the setup of image pickers for taking or selecting photos.
    private fun setupImagePickers() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempImageUri != null) {
                handleImageResult(tempImageUri!!)
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleImageResult(it) }
        }

        // Setup click listeners for image preview and remove button
        binding.imagePreview.setOnClickListener {
            showFullScreenImage()
        }

        binding.btnRemovePhoto.setOnClickListener {
            removePhoto()
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Image Handling ---
    private fun handleImageResult(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                viewModel.setImageBytes(bytes)
                showImagePreview(bytes)
            }
        } catch (e: Exception) {
            showError("Failed to process image")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles showing the date picker dialog for selecting a date.
    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(selection))
            viewModel.setDate(date)
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles showing the image preview in a dialog.
    private fun showImagePreview(bytes: ByteArray?) {
        if (bytes == null) {
            binding.imagePreviewContainer.visibility = View.GONE
            return
        }

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imagePreviewContainer.visibility = View.VISIBLE
        binding.imagePreview.setImageBitmap(bitmap)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles removing the selected photo from the transaction.
    private fun removePhoto() {
        viewModel.setImageBytes(null)
        binding.imagePreviewContainer.visibility = View.GONE
        binding.imagePreview.setImageBitmap(null)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles showing the full-screen image in a dialog.
    private fun showFullScreenImage() {
        viewModel.imageBytes.value?.let { bytes ->
            val dialog = AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
                .setView(
                    ImageView(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                    }
                )
                .create()

            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog.show()
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles showing the dialog for selecting image source (camera or gallery).
    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> pickImageLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles launching the camera to take a photo.
    private fun launchCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "transaction_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        
        try {
            tempImageUri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            tempImageUri?.let { uri ->
                takePictureLauncher.launch(uri)
            } ?: showError("Failed to prepare camera")
        } catch (e: Exception) {
            showError("Failed to launch camera")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Update Methods ---
    private fun updateSelectedLabels(labels: List<Label>) {
        binding.chipGroupLabels.removeAllViews()
        
        if (labels.isEmpty()) {
            binding.textSelectedLabels.visibility = View.VISIBLE
            return
        }

        binding.textSelectedLabels.visibility = View.GONE
        
        labels.forEach { label ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = label.name
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    viewModel.removeLabel(label)
                }
            }
            binding.chipGroupLabels.addView(chip)
        }
    }

    private fun updateSelectedCategory(category: Category?) {
        if (category == null) {
            binding.textSelectedCategoryName.text = "Select category"
            binding.imgSelectedCategoryIcon.setImageResource(R.drawable.ic_ui_categories)
            binding.imgSelectedCategoryIcon.setColorFilter(requireContext().getThemeColor(R.attr.bb_accent))
            return
        }

        binding.textSelectedCategoryName.text = category.name
        binding.imgSelectedCategoryIcon.setImageResource(category.icon)
        binding.imgSelectedCategoryIcon.setColorFilter(requireContext().getColor(category.color))
    }

    private fun updateSelectedWallet(wallet: Wallet?) {
        if (wallet == null) {
            binding.textSelectedWalletName.text = "Select wallet"
            return
        }
        binding.textSelectedWalletName.text = wallet.name
    }

    private fun updateSelectedDate(date: String?) {
        if (date == null) {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            binding.textSelectedDate.text = currentDate.format(formatter)
            return
        }
        binding.textSelectedDate.text = date
    }

    private fun updateSelectedRecurrence(recurrence: RecurrenceData) {
        binding.textSelectedRecurrenceRate.text = recurrence.toDisplayString()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Save Logic ---
    private fun saveTransaction() {
        val amount = binding.edtTextAmount.text.toString().toDoubleOrNull()
        viewModel.setAmount(amount)

        val date = binding.textSelectedDate.text.toString()
        viewModel.setDate(date)

        val note = binding.edtTextNote.text.toString()
        viewModel.setNote(note)

        val currency = binding.spinnerCurrency.selectedItem.toString()
        viewModel.setCurrency(currency)

        // Show validation errors if any
        viewModel.showValidationErrors()

        // Check if form is valid before proceeding
        if (!viewModel.validateForm()) return

        // Call addTransaction
        viewModel.addTransaction()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Popup Navigation ---
    private fun showLabelsSelector() {
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectLabelFragment)
    }

    private fun showWalletSelector() {
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectWalletFragment)
    }

    private fun showCategorySelector(){
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectCategoryFragment)
    }

    private fun showRecurrenceSelector(){
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectRecurrenceFragment)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- Observers ---
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    viewModel.uiState.collect { state ->
                        handleUiState(state)
                    }
                }

                // Collect validation state
                launch {
                    viewModel.validationState.collect { state ->
                        handleValidationState(state)
                    }
                }

                // Collect category
                launch {
                    viewModel.category.collect { category ->
                        updateSelectedCategory(category)
                    }
                }

                // Collect wallet
                launch {
                    viewModel.wallet.collect { wallet ->
                        updateSelectedWallet(wallet)
                    }
                }

                // Collect date
                launch {
                    viewModel.date.collect { date ->
                        updateSelectedDate(date)
                    }
                }

                // Collect recurrence data
                launch {
                    viewModel.recurrenceData.collect { rate ->
                        updateSelectedRecurrence(rate)
                    }
                }

                // Collect selected labels
                launch {
                    viewModel.selectedLabels.collect { labels ->
                        updateSelectedLabels(labels)
                    }
                }

                // Collect image bytes
                launch {
                    viewModel.imageBytes.collect { bytes ->
                        if (bytes != null) {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            binding.imagePreview.apply {
                                setImageBitmap(bitmap)
                                visibility = View.VISIBLE
                            }
                        } else {
                            binding.imagePreview.visibility = View.GONE
                        }
                    }
                }

                // Collect save state
                launch {
                    viewModel.saveState.collect { shouldSave ->
                        if (!shouldSave) {
                            reset()
                        }
                    }
                }
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // --- UI State Handlers ---
    private fun handleUiState(state: TransactionAddViewModel.UiState) {
        when (state) {
            is TransactionAddViewModel.UiState.Loading -> {
                binding.btnSave.isEnabled = false
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.successCheckmark.visibility = View.GONE
                binding.loadingText.text = "Saving transaction..."
            }
            is TransactionAddViewModel.UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.successCheckmark.visibility = View.VISIBLE
                binding.loadingText.text = "Transaction saved successfully!"
                
                // Add a slight delay before closing to show the success state
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000) // Show success state for 1 second
                    binding.loadingOverlay.visibility = View.GONE
                    viewModel.reset()
                    findNavController().popBackStack()
                }
            }
            is TransactionAddViewModel.UiState.Error -> {
                binding.btnSave.isEnabled = true
                binding.loadingOverlay.visibility = View.GONE
                showError(state.message)
            }
            else -> {
                binding.btnSave.isEnabled = true
                binding.loadingOverlay.visibility = View.GONE
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    //Handles the validation state and shows/hides error messages for each field.
    private fun handleValidationState(state: TransactionAddViewModel.ValidationState) {
        with(binding) {
            // Show/hide error messages for each field
            textAmountError.apply {
                text = state.amountError
                visibility = if (state.shouldShowErrors && state.amountError != null) View.VISIBLE else View.GONE
            }

            textCurrencyError.apply {
                text = state.currencyError
                visibility = if (state.shouldShowErrors && state.currencyError != null) View.VISIBLE else View.GONE
            }

            textCategoryError.apply {
                text = state.categoryError
                visibility = if (state.shouldShowErrors && state.categoryError != null) View.VISIBLE else View.GONE
            }

            textWalletError.apply {
                text = state.walletError
                visibility = if (state.shouldShowErrors && state.walletError != null) View.VISIBLE else View.GONE
            }

            textDateError.apply {
                text = state.dateError
                visibility = if (state.shouldShowErrors && state.dateError != null) View.VISIBLE else View.GONE
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .show()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Resets the form to its initial state
    private fun reset() {
        viewModel.reset()
        binding.apply {
            edtTextAmount.setText("")
            edtTextNote.setText("")
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\