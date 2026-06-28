package com.example.clickjob_finalproject.ui.myjobs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clickjob_finalproject.MainActivity
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.EmployerJobItem
import com.example.clickjob_finalproject.adapters.EmployerJobsAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PostJobFragment : Fragment() {

    private lateinit var ivBack: ImageView
    private lateinit var spinnerCategory: Spinner
    private lateinit var etCompany: com.google.android.material.textfield.TextInputEditText
    private lateinit var toggleHourly: TextView
    private lateinit var toggleDaily: TextView
    private lateinit var spinnerSalary: Spinner
    private lateinit var spinnerStartTime: Spinner
    private lateinit var spinnerEndTime: Spinner
    private lateinit var spinnerWorkers: Spinner
    private lateinit var etDescription: com.google.android.material.textfield.TextInputEditText
    private lateinit var chipGroupRequirements: ChipGroup
    private lateinit var etPhone: com.google.android.material.textfield.TextInputEditText
    private lateinit var etAddress: com.google.android.material.textfield.TextInputEditText
    private lateinit var etLink: com.google.android.material.textfield.TextInputEditText
    private lateinit var imageContainer: android.widget.FrameLayout
    private lateinit var ivJobImage: ImageView
    private lateinit var ivAddImageIcon: ImageView
    private lateinit var btnPublish: com.google.android.material.button.MaterialButton

    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                ivJobImage.setImageURI(uri)
                ivJobImage.visibility = View.VISIBLE
                ivAddImageIcon.visibility = View.GONE
            }
        }
    }

    // Categories with images
    data class CategoryItem(val name: String, val imageRes: Int)

    private val categories = listOf(
        CategoryItem("מסעדות", R.drawable.img_cat_circle_hospitality),
        CategoryItem("אבטחה וביטחון", R.drawable.img_cat_circle_security),
        CategoryItem("משלוחים ותחבורה", R.drawable.img_cat_circle_delivery),
        CategoryItem("בניין וייצור", R.drawable.img_cat_circle_construction),
        CategoryItem("חינוך והוראה", R.drawable.img_cat_circle_education),
        CategoryItem("בעלי חיים", R.drawable.img_cat_circle_pets),
        CategoryItem("אפסנאות ולוגיסטיקה", R.drawable.img_cat_circle_logistics),
        CategoryItem("רפואה ובריאות", R.drawable.img_cat_circle_health),
        CategoryItem("הפקה ואירועים", R.drawable.img_cat_circle_events),
        CategoryItem("טכנולוגיה", R.drawable.img_cat_circle_tech),
        CategoryItem("שירות לקוחות", R.drawable.img_cat_circle_service),
        CategoryItem("מכירות ואופנה", R.drawable.img_cat_circle_sales),
        CategoryItem("עיצוב וקריאייטיב", R.drawable.img_cat_circle_creative),
        CategoryItem("אחזקה", R.drawable.img_cat_circle_maintenance)
    )

    private val salaryOptions = (20..300 step 5).map { it.toString() }
    private val workerCountOptions = (1..20).map { it.toString() }
    private val timeOptions = (0..23).map { hour -> String.format("%02d:00", hour) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_job, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find all views
        ivBack = view.findViewById(R.id.ivBack)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etCompany = view.findViewById(R.id.etCompany)
        toggleHourly = view.findViewById(R.id.toggleHourly)
        toggleDaily = view.findViewById(R.id.toggleDaily)
        spinnerSalary = view.findViewById(R.id.spinnerSalary)
        spinnerStartTime = view.findViewById(R.id.spinnerStartTime)
        spinnerEndTime = view.findViewById(R.id.spinnerEndTime)
        spinnerWorkers = view.findViewById(R.id.spinnerWorkers)
        etDescription = view.findViewById(R.id.etDescription)
        chipGroupRequirements = view.findViewById(R.id.chipGroupRequirements)
        etPhone = view.findViewById(R.id.etPhone)
        etAddress = view.findViewById(R.id.etAddress)
        etLink = view.findViewById(R.id.etLink)
        imageContainer = view.findViewById(R.id.imageContainer)
        ivJobImage = view.findViewById(R.id.ivJobImage)
        ivAddImageIcon = view.findViewById(R.id.ivAddImageIcon)
        btnPublish = view.findViewById(R.id.btnPublish)

        // Hide bottom navigation bar
        (activity as? MainActivity)?.hideBottomNav()

        setupBackButton()
        setupCategorySpinner()
        setupSpinners()
        setupSalaryToggle()
        setupChips()
        setupImageUpload()
        setupButtons()

        // If duplicating an existing job, fill in details
        arguments?.let { args ->
            prefillFromBundle(args)
        }
    }

    private fun setupBackButton() {
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Custom adapter for category spinner with images
    private inner class CategorySpinnerAdapter(
        context: Context,
        private val items: List<CategoryItem>
    ) : ArrayAdapter<CategoryItem>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_category_spinner, parent, false)

            val item = items[position]
            val tvName = view.findViewById<TextView>(R.id.tvCategoryName)
            val imgCategory = view.findViewById<ImageView>(R.id.imgCategory)

            tvName.text = item.name
            imgCategory.setImageResource(item.imageRes)

            return view
        }
    }

    private fun setupCategorySpinner() {
        val adapter = CategorySpinnerAdapter(requireContext(), categories)
        spinnerCategory.adapter = adapter
    }

    private fun setupSpinners() {
        // Salary spinner
        val salaryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            salaryOptions
        )
        salaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSalary.adapter = salaryAdapter
        spinnerSalary.setSelection(salaryOptions.indexOf("50"))

        // Workers count spinner
        val workersAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            workerCountOptions
        )
        workersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWorkers.adapter = workersAdapter

        // Start time spinner
        val timeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeOptions
        )
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStartTime.adapter = timeAdapter
        spinnerStartTime.setSelection(14) // Default 14:00

        // End time spinner
        val endTimeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeOptions
        )
        endTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEndTime.adapter = endTimeAdapter
        spinnerEndTime.setSelection(21) // Default 21:00
    }

    private fun setupSalaryToggle() {
        toggleHourly.setOnClickListener {
            toggleHourly.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            toggleHourly.setBackgroundResource(R.drawable.bg_toggle_selected_teal)
            toggleDaily.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
            toggleDaily.background = null
        }

        toggleDaily.setOnClickListener {
            toggleDaily.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            toggleDaily.setBackgroundResource(R.drawable.bg_toggle_selected_teal)
            toggleHourly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
            toggleHourly.background = null
        }
    }

    private fun setupChips() {
        // Only show "+" chip initially
        val addChip = Chip(requireContext()).apply {
            text = "+"
            isClickable = true
            setChipBackgroundColorResource(R.color.white)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.DarkDeep))
            chipStrokeWidth = 1f
            setChipStrokeColorResource(R.color.DarkDeep)
            setOnClickListener {
                showAddRequirementDialog()
            }
        }
        chipGroupRequirements.addView(addChip)
    }

    private fun showAddRequirementDialog() {
        val editText = com.google.android.material.textfield.TextInputEditText(requireContext())
        editText.hint = "הכנס דרישה"
        editText.gravity = android.view.Gravity.END

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("הוסף דרישה")
            .setView(editText)
            .setPositiveButton("הוסף") { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    addRequirementChip(text)
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun addRequirementChip(text: String) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            isCloseIconVisible = true
            isClickable = false
            setChipBackgroundColorResource(R.color.white)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.DarkDeep))
            chipStrokeWidth = 1f
            setChipStrokeColorResource(R.color.DarkDeep)
            setOnCloseIconClickListener {
                chipGroupRequirements.removeView(this)
            }
        }
        // Add before the "+" chip
        chipGroupRequirements.addView(chip, 0)
    }

    private fun setupImageUpload() {
        imageContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }
    }

    private fun setupButtons() {
        btnPublish.setOnClickListener {
                saveJob(isDraft = true)

        }
    }

    private fun validateForm(): Boolean {
        if (etCompany.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "יש למלא שם חברה", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etDescription.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "יש למלא אופי עבודה", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveJob(isDraft: Boolean) {
        val selectedCategory = categories[spinnerCategory.selectedItemPosition]
        val job = EmployerJobItem(
            title = selectedCategory.name,
            company = etCompany.text.toString(),
            workersRegistered = 0,
            workersNeeded = spinnerWorkers.selectedItem.toString().toIntOrNull() ?: 1,
            date = "${spinnerStartTime.selectedItem}-${spinnerEndTime.selectedItem}",
            price = spinnerSalary.selectedItem.toString(),
            category = selectedCategory.name
        )

        if (isDraft) {
            Toast.makeText(requireContext(), "הטיוטה נשמרה", Toast.LENGTH_SHORT).show()
        } else {
            // TODO: save to Firestore
            Toast.makeText(requireContext(), "המשרה פורסמה בהצלחה!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun prefillFromBundle(args: Bundle) {
        args.getString("jobTitle")?.let { title ->
            val index = categories.indexOfFirst { it.name == title }
            if (index >= 0) spinnerCategory.setSelection(index)
        }
        args.getString("jobCompany")?.let { etCompany.setText(it) }
        args.getString("jobPrice")?.let { price ->
            val index = salaryOptions.indexOf(price)
            if (index >= 0) spinnerSalary.setSelection(index)
        }
        args.getInt("workersNeeded", 1).let { count ->
            val index = workerCountOptions.indexOf(count.toString())
            if (index >= 0) spinnerWorkers.setSelection(index)
        }
        args.getString("jobDescription")?.let { etDescription.setText(it) }
        args.getString("jobPhone")?.let { etPhone.setText(it) }
        args.getString("jobAddress")?.let { etAddress.setText(it) }
        args.getString("jobLink")?.let { etLink.setText(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show bottom navigation bar again when leaving
        (activity as? MainActivity)?.showBottomNav()
    }
}