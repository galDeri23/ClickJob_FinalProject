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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clickjob_finalproject.MainActivity
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.data.model.JobPost
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentPostJobBinding
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale

class PostJobFragment : Fragment() {

    private var _binding: FragmentPostJobBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var duplicatedImageUrl: String = ""

    private var startDate: Long = System.currentTimeMillis()
    private var endDate: Long = System.currentTimeMillis()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            duplicatedImageUrl = ""

            selectedImageUri?.let { uri ->
                binding.ivJobImage.setImageURI(uri)
                binding.ivJobImage.visibility = View.VISIBLE
                binding.ivAddImageIcon.visibility = View.GONE
            }
        }
    }

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
    ): View {
        _binding = FragmentPostJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomNav()

        setupBackButton()
        setupCategorySpinner()
        setupSpinners()
        setupSalaryToggle()
        setupChips()
        setupImageUpload()
        setupDateRangePicker()
        setupButtons()

        val duplicateJobId = arguments?.getString("duplicateJobId")
        if (!duplicateJobId.isNullOrEmpty()) {
            prefillFromJobId(duplicateJobId)
        }
    }

    private fun setupBackButton() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

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
            view.findViewById<TextView>(R.id.tvCategoryName).text = item.name
            view.findViewById<android.widget.ImageView>(R.id.imgCategory)
                .setImageResource(item.imageRes)

            return view
        }
    }

    private fun setupCategorySpinner() {
        binding.spinnerCategory.adapter = CategorySpinnerAdapter(requireContext(), categories)
    }

    private fun setupSpinners() {
        val salaryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            salaryOptions
        )
        salaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSalary.adapter = salaryAdapter
        binding.spinnerSalary.setSelection(salaryOptions.indexOf("50"))

        val workersAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            workerCountOptions
        )
        workersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWorkers.adapter = workersAdapter

        val timeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeOptions
        )
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerStartTime.adapter = timeAdapter
        binding.spinnerStartTime.setSelection(14)

        binding.spinnerEndTime.adapter = timeAdapter
        binding.spinnerEndTime.setSelection(21)
    }

    private fun setupSalaryToggle() {
        binding.toggleHourly.setOnClickListener {
            setSalaryTypeHourly()
        }

        binding.toggleDaily.setOnClickListener {
            setSalaryTypeDaily()
        }
    }

    private fun setSalaryTypeHourly() {
        binding.toggleHourly.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleHourly.setBackgroundResource(R.drawable.bg_toggle_selected_teal)

        binding.toggleDaily.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        binding.toggleDaily.background = null
    }

    private fun setSalaryTypeDaily() {
        binding.toggleDaily.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleDaily.setBackgroundResource(R.drawable.bg_toggle_selected_teal)

        binding.toggleHourly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        binding.toggleHourly.background = null
    }

    private fun setupChips() {
        addPlusChip()
    }

    private fun addPlusChip() {
        val addChip = Chip(requireContext()).apply {
            text = "+"
            isClickable = true
            setChipBackgroundColorResource(R.color.white)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.DarkDeep))
            chipStrokeWidth = 1f
            setChipStrokeColorResource(R.color.DarkDeep)
            setOnClickListener { showAddRequirementDialog() }
        }

        binding.chipGroupRequirements.addView(addChip)
    }

    private fun clearRequirementChips() {
        binding.chipGroupRequirements.removeAllViews()
        addPlusChip()
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
                if (text.isNotEmpty()) addRequirementChip(text)
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
                binding.chipGroupRequirements.removeView(this)
            }
        }

        binding.chipGroupRequirements.addView(chip, 0)
    }

    private fun setupImageUpload() {
        binding.imageContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }
    }

    private fun setupDateRangePicker() {
        binding.etDateRange.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("בחר תאריך או טווח")
                .setSelection(
                    androidx.core.util.Pair(startDate, endDate)
                )
                .build()

            picker.addOnPositiveButtonClickListener { range ->
                startDate = range.first ?: System.currentTimeMillis()
                endDate = range.second ?: startDate
                updateDateRangeText()
            }

            picker.show(parentFragmentManager, "dateRangePicker")
        }
    }

    private fun updateDateRangeText() {
        val format = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
        val startText = format.format(startDate)
        val endText = format.format(endDate)

        binding.etDateRange.setText(
            if (startText == endText) startText else "$startText - $endText"
        )
    }

    private fun setupButtons() {
        binding.btnPublish.setOnClickListener {
            if (validateForm()) saveJob()
        }
    }

    private fun validateForm(): Boolean {
        if (binding.etJobTitle.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "יש למלא שם משרה", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.etCompany.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "יש למלא שם חברה", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.etDateRange.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "יש לבחור תאריך למשרה", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.etDescription.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "יש למלא אופי עבודה", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveJob() {
        val selectedCategory = categories[binding.spinnerCategory.selectedItemPosition]

        val requirements = mutableListOf<String>()
        for (i in 0 until binding.chipGroupRequirements.childCount) {
            val chip = binding.chipGroupRequirements.getChildAt(i) as? Chip
            val chipText = chip?.text?.toString() ?: ""

            if (chipText != "+") {
                requirements.add(chipText)
            }
        }

        val salaryType = if (binding.toggleHourly.background != null) "hourly" else "daily"
        val isUrgent = (startDate - System.currentTimeMillis()) < 48 * 60 * 60 * 1000L
        val workFrequency = if (binding.rbContinuous.isChecked) "רציף" else "חד פעמי"

        val job = JobPost(
            title = binding.etJobTitle.text.toString().trim(),
            company = binding.etCompany.text.toString().trim(),
            category = selectedCategory.name,
            salaryType = salaryType,
            salary = binding.spinnerSalary.selectedItem.toString(),
            workFrequency = workFrequency,
            date = startDate,
            endDate = endDate,
            startTime = binding.spinnerStartTime.selectedItem.toString(),
            endTime = binding.spinnerEndTime.selectedItem.toString(),
            workersNeeded = binding.spinnerWorkers.selectedItem.toString().toIntOrNull() ?: 1,
            description = binding.etDescription.text.toString().trim(),
            requirements = requirements,
            phone = binding.etPhone.text.toString().trim(),
            address = binding.etAddress.text.toString().trim(),
            link = binding.etLink.text.toString().trim(),
            imageUrl = duplicatedImageUrl,
            isUrgent = isUrgent
        )

        binding.btnPublish.isEnabled = false

        if (selectedImageUri != null) {
            uploadImageAndSave(job)
        } else {
            saveToFirestore(job)
        }
    }

    private fun uploadImageAndSave(job: JobPost) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("job_images/$userId/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveToFirestore(job.copy(imageUrl = uri.toString()))
                }
            }
            .addOnFailureListener {
                binding.btnPublish.isEnabled = true
                Toast.makeText(requireContext(), "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToFirestore(job: JobPost) {
        UserRepository.saveJobPost(
            job = job,
            onSuccess = {
                Toast.makeText(requireContext(), "המשרה פורסמה בהצלחה!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            },
            onFailure = {
                binding.btnPublish.isEnabled = true
                Toast.makeText(requireContext(), "שגיאה בפרסום, נסי שוב", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun prefillFromJobId(jobId: String) {
        UserRepository.getJobById(
            jobId = jobId,
            onSuccess = { job ->

                val index = categories.indexOfFirst { it.name == job.category }
                if (index >= 0) {
                    binding.spinnerCategory.setSelection(index)
                }

                binding.etCompany.setText(job.company)
                binding.etJobTitle.setText(job.title)

                if (job.workFrequency == "חד פעמי") {
                    binding.rbOneTime.isChecked = true
                } else {
                    binding.rbContinuous.isChecked = true
                }


                startDate = System.currentTimeMillis()
                endDate = System.currentTimeMillis()
                binding.etDateRange.setText("")

                val salaryIndex = salaryOptions.indexOf(job.salary)
                if (salaryIndex >= 0) {
                    binding.spinnerSalary.setSelection(salaryIndex)
                }

                val workersIndex = workerCountOptions.indexOf(job.workersNeeded.toString())
                if (workersIndex >= 0) {
                    binding.spinnerWorkers.setSelection(workersIndex)
                }

                val startIndex = timeOptions.indexOf(job.startTime)
                if (startIndex >= 0) {
                    binding.spinnerStartTime.setSelection(startIndex)
                }

                val endIndex = timeOptions.indexOf(job.endTime)
                if (endIndex >= 0) {
                    binding.spinnerEndTime.setSelection(endIndex)
                }

                binding.etDescription.setText(job.description)
                binding.etPhone.setText(job.phone)
                binding.etAddress.setText(job.address)
                binding.etLink.setText(job.link)

                if (job.salaryType == "daily") {
                    setSalaryTypeDaily()
                } else {
                    setSalaryTypeHourly()
                }

                clearRequirementChips()
                job.requirements.forEach { requirement ->
                    addRequirementChip(requirement)
                }

                duplicatedImageUrl = job.imageUrl

                if (job.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(job.imageUrl)
                        .into(binding.ivJobImage)

                    binding.ivJobImage.visibility = View.VISIBLE
                    binding.ivAddImageIcon.visibility = View.GONE
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה בטעינת נתוני המשרה", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showBottomNav()
        _binding = null
    }
}