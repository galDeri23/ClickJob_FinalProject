package com.example.clickjob_finalproject.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.data.model.Document
import com.example.clickjob_finalproject.data.model.UserProfile
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentProfileBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var isEditMode = false
    private var isDocumentsExpanded = false
    private var currentProfile: UserProfile? = null
    private var currentHardSkills = mutableListOf<String>()
    private var currentSoftSkills = mutableListOf<String>()
    private var cameraImageUri: Uri? = null

    private val camereLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri -> uploadProfileImage(uri) }
        }
    }

    private fun createImageUri(): Uri {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "profile_${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireContext().contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )!!.also { cameraImageUri = it }
    }
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> uploadProfileImage(uri) }
        }
    }

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                if (!isPdf(uri)) {
                    Toast.makeText(
                        requireContext(),
                        "ניתן להעלות קבצי PDF בלבד",
                        Toast.LENGTH_LONG
                    ).show()
                    return@let
                }
                uploadAdditionalDocument(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()
        setupClickListeners()
    }

    private fun loadProfile() {
        binding.progressBar.visibility = View.VISIBLE
        binding.headerLayout.visibility = View.INVISIBLE

        UserRepository.getUserProfile(
            onSuccess = { profile ->
                currentProfile = profile
                currentHardSkills = (profile.languages + profile.licenses +
                        profile.certificates + profile.software +
                        profile.jobCategories).toMutableList()
                currentSoftSkills = profile.softSkills.toMutableList()
                binding.progressBar.visibility = View.GONE
                binding.headerLayout.visibility = View.VISIBLE
                populateUI(profile)
            },
            onFailure = {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "שגיאה בטעינת הפרופיל", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun populateUI(profile: UserProfile) {
        binding.tvName.text = profile.name
        binding.tvJobTitle.text = profile.jobCategories.firstOrNull() ?: ""

        if (profile.rating > 0.0) {
            binding.ratingRow.visibility = View.VISIBLE
            binding.tvRating.text = String.format("%.1f", profile.rating)
        } else {
            binding.ratingRow.visibility = View.GONE
        }

        // Update documents count
        val count = (if (profile.cvUrl.isNotEmpty()) 1 else 0) + profile.documents.size
        binding.tvDocumentsCount.text = "($count)"

        loadProfileImage()
        renderSkillChips()
    }

    private fun loadProfileImage() {
        val profile = currentProfile
        val user = FirebaseAuth.getInstance().currentUser

        when {
            // Saved profile image (uploaded manually)
            profile?.profileImageUrl?.isNotEmpty() == true -> {
                Glide.with(this).load(profile.profileImageUrl).circleCrop().into(binding.imgProfile)
            }
            // Google account photo
            user?.photoUrl != null -> {
                Glide.with(this).load(user.photoUrl).circleCrop().into(binding.imgProfile)
            }
            // Default
            else -> {
                binding.imgProfile.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }
    }

    private fun renderSkillChips() {
        binding.flexHardSkills.removeAllViews()
        binding.flexSoftSkills.removeAllViews()

        currentHardSkills.forEach { skill ->
            addChip(binding.flexHardSkills, skill, isEditMode) {
                currentHardSkills.remove(skill)
                renderSkillChips()
            }
        }

        currentSoftSkills.forEach { skill ->
            addChip(binding.flexSoftSkills, skill, isEditMode) {
                currentSoftSkills.remove(skill)
                renderSkillChips()
            }
        }

        if (isEditMode) {
            addPlusButton(binding.flexHardSkills) { showAddSkillDialog(isHard = true) }
            addPlusButton(binding.flexSoftSkills) { showAddSkillDialog(isHard = false) }
        }
    }

    private fun addChip(
        container: FlexboxLayout,
        text: String,
        withDelete: Boolean,
        onDelete: () -> Unit
    ) {
        val chip = TextView(requireContext()).apply {
            this.text = if (withDelete) "✕ $text" else text
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_dark, null))
            setPadding(
                resources.getDimensionPixelSize(R.dimen.chip_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.chip_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.chip_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.chip_padding_vertical)
            )
            background = resources.getDrawable(R.drawable.bg_gray, null)
            if (withDelete) setOnClickListener { onDelete() }
        }
        val lp = FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0,
                resources.getDimensionPixelSize(R.dimen.chip_margin),
                resources.getDimensionPixelSize(R.dimen.chip_margin))
        }
        chip.layoutParams = lp
        container.addView(chip)
    }

    private fun addPlusButton(container: FlexboxLayout, onClick: () -> Unit) {
        val btn = TextView(requireContext()).apply {
            text = "+"
            textSize = 18f
            setTextColor(resources.getColor(R.color.DarkDeep, null))
            setPadding(24, 8, 24, 8)
            background = resources.getDrawable(R.drawable.bg_gray, null)
            setOnClickListener { onClick() }
        }
        val lp = FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT
        )
        btn.layoutParams = lp
        container.addView(btn)
    }

    private fun showAddSkillDialog(isHard: Boolean) {
        val input = android.widget.EditText(requireContext())
        input.hint = if (isHard) "הוסיפי כישור" else "הוסיפי כישור רך"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(if (isHard) "כישור חדש" else "כישור רך חדש")
            .setView(input)
            .setPositiveButton("הוסף") { _, _ ->
                val skill = input.text.toString().trim()
                if (skill.isNotEmpty()) {
                    if (isHard) currentHardSkills.add(skill)
                    else currentSoftSkills.add(skill)
                    renderSkillChips()
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            if (isEditMode) saveProfile() else enterEditMode()
        }

        binding.imgProfile.setOnClickListener {
            if (isEditMode) openImagePicker()
        }

        binding.btnPhone.setOnClickListener {
            if (!isEditMode) {
                val phone = currentProfile?.phone ?: return@setOnClickListener
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                startActivity(intent)
            }
        }

        binding.btnSocial.setOnClickListener {
            if (!isEditMode) {
                val instagram = currentProfile?.instagramUrl ?: return@setOnClickListener
                if (instagram.isEmpty()) {
                    Toast.makeText(requireContext(), "לא הוזן קישור לאינסטגרם", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagram))
                startActivity(intent)
            }
        }

        binding.btnDocuments.setOnClickListener {
            toggleDocuments()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), com.example.clickjob_finalproject.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun toggleDocuments() {
        isDocumentsExpanded = !isDocumentsExpanded

        binding.ivDocumentsArrow.animate()
            .rotation(if (isDocumentsExpanded) -90f else 0f)
            .setDuration(200)
            .start()

        if (isDocumentsExpanded) {
            binding.layoutDocumentsList.visibility = View.VISIBLE
            renderDocumentsList()
        } else {
            binding.layoutDocumentsList.visibility = View.GONE
        }
    }

    private fun renderDocumentsList() {
        val profile = currentProfile ?: return

        // Show CV name
        binding.tvCvName.text = if (profile.cvName.isNotEmpty()) profile.cvName else "קורות חיים"

        // CV click - open file
        binding.rowCv.setOnClickListener {
            if (profile.cvUrl.isNotEmpty()) openDocument(profile.cvUrl)
        }

        // Show delete CV button in edit mode only
        binding.btnDeleteCv.visibility = if (isEditMode) View.VISIBLE else View.GONE
        binding.btnDeleteCv.setOnClickListener {
            val current = currentProfile ?: return@setOnClickListener
            val urlToDelete = current.cvUrl

            val updatedProfile = current.copy(cvUrl = "", cvName = "")
            currentProfile = updatedProfile

            UserRepository.saveUserProfile(
                profile = updatedProfile,
                onSuccess = {
                    deleteStorageFile(urlToDelete)
                    renderDocumentsList()
                },
                onFailure = {
                    Toast.makeText(requireContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Render additional documents
        binding.layoutAdditionalDocs.removeAllViews()
        profile.documents.forEachIndexed { index, doc ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.chip_margin) * 6
                )
                layoutDirection = View.LAYOUT_DIRECTION_RTL
            }

            val tvDocName = TextView(requireContext()).apply {
                text = doc.name
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_dark, null))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { openDocument(doc.url) }
            }
            row.addView(tvDocName)

            if (isEditMode) {
                val btnDelete = TextView(requireContext()).apply {
                    text = "✕"
                    textSize = 14f
                    setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                    setOnClickListener {
                        val urlToDelete = doc.url
                        val updatedDocs = profile.documents.toMutableList().also { it.removeAt(index) }
                        val updatedProfile = currentProfile?.copy(documents = updatedDocs) ?: return@setOnClickListener
                        currentProfile = updatedProfile
                        UserRepository.saveUserProfile(
                            profile = updatedProfile,
                            onSuccess = {
                                deleteStorageFile(urlToDelete)
                                renderDocumentsList()
                            },
                            onFailure = {}
                        )
                    }
                }
                row.addView(btnDelete)
            }

            binding.layoutAdditionalDocs.addView(row)
        }

        // Show add document button in edit mode only
        binding.btnAddDocument.visibility = if (isEditMode) View.VISIBLE else View.GONE
        binding.btnAddDocument.setOnClickListener { openDocumentPicker() }

        // Update count
        val count = (if (profile.cvUrl.isNotEmpty()) 1 else 0) + profile.documents.size
        binding.tvDocumentsCount.text = "($count)"
    }

    private fun openDocument(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // Removes the actual file from Storage, so deleted documents don't linger
    private fun deleteStorageFile(url: String) {
        if (url.isEmpty()) return
        FirebaseStorage.getInstance()
            .getReferenceFromUrl(url)
            .delete()
            .addOnFailureListener {
                android.util.Log.e("STORAGE", "Failed to delete file: ${it.message}")
            }
    }
    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
        }
        documentPickerLauncher.launch(intent)
    }

    private fun uploadAdditionalDocument(uri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileName = getFileName(uri)

        val storageRef = FirebaseStorage.getInstance()
            .reference.child("documents/$userId/$fileName")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val newDoc = Document(name = fileName, url = downloadUri.toString())
                    val updatedDocs = (currentProfile?.documents ?: emptyList()) + newDoc
                    val updatedProfile = currentProfile?.copy(documents = updatedDocs) ?: return@addOnSuccessListener
                    currentProfile = updatedProfile

                    UserRepository.saveUserProfile(
                        profile = updatedProfile,
                        onSuccess = { renderDocumentsList() },
                        onFailure = {
                            Toast.makeText(requireContext(), "שגיאה בשמירה", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "שגיאה בהעלאה", Toast.LENGTH_SHORT).show()
            }
    }

    // The picker filter isn't always honoured by third-party file providers,
    // so the chosen file is checked again before uploading
    private fun isPdf(uri: Uri): Boolean {
        if (requireContext().contentResolver.getType(uri) == "application/pdf") return true
        return getFileName(uri).lowercase().endsWith(".pdf")
    }
    private fun getFileName(uri: Uri): String {
        return requireContext().contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex("_display_name")
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "document"
    }

    private fun enterEditMode() {
        isEditMode = true

        binding.tvName.visibility = View.GONE
        binding.etName.visibility = View.VISIBLE
        binding.etName.setText(binding.tvName.text)

        binding.tvJobTitle.visibility = View.GONE
        binding.etJobTitle.visibility = View.VISIBLE
        binding.etJobTitle.setText(binding.tvJobTitle.text)

        binding.contactRow.visibility = View.GONE
        binding.editContactRow.visibility = View.VISIBLE
        binding.etPhone.setText(currentProfile?.phone ?: "")
        binding.etInstagram.setText(currentProfile?.instagramUrl ?: "")

        binding.ivCameraOverlay.visibility = View.VISIBLE
        binding.btnEdit.setImageResource(R.drawable.ic_check)

        if (isDocumentsExpanded) renderDocumentsList()
        renderSkillChips()
    }

    private fun saveProfile() {
        isEditMode = false

        val updatedName = binding.etName.text.toString().trim()
        val updatedJobTitle = binding.etJobTitle.text.toString().trim()
        val updatedPhone = binding.etPhone.text.toString().trim()
        val updatedInstagram = binding.etInstagram.text.toString().trim()

        binding.tvName.text = updatedName
        binding.tvName.visibility = View.VISIBLE
        binding.etName.visibility = View.GONE

        binding.tvJobTitle.text = updatedJobTitle
        binding.tvJobTitle.visibility = View.VISIBLE
        binding.etJobTitle.visibility = View.GONE

        binding.contactRow.visibility = View.VISIBLE
        binding.editContactRow.visibility = View.GONE

        binding.ivCameraOverlay.visibility = View.GONE
        binding.btnEdit.setImageResource(R.drawable.ic_edit)

        val updatedProfile = currentProfile?.copy(
            name = updatedName,
            phone = updatedPhone,
            instagramUrl = updatedInstagram,
            softSkills = currentSoftSkills
        ) ?: return

        currentProfile = updatedProfile

        UserRepository.saveUserProfile(
            profile = updatedProfile,
            onSuccess = {
                Toast.makeText(requireContext(), "הפרופיל עודכן בהצלחה ✓", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה בשמירה, נסי שוב", Toast.LENGTH_SHORT).show()
            }
        )

        if (isDocumentsExpanded) renderDocumentsList()
        renderSkillChips()
    }

    private fun openImagePicker() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("בחרי תמונה")
            .setItems(arrayOf("גלריה", "צלם עכשיו")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                        imagePickerLauncher.launch(intent)
                    }
                    1 -> camereLauncher.launch(createImageUri())
                }
            }
            .show()
    }

    private fun uploadProfileImage(uri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("profile_images/$userId.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save URL to Firestore
                    val updatedProfile = currentProfile?.copy(
                        profileImageUrl = downloadUri.toString()
                    ) ?: return@addOnSuccessListener
                    currentProfile = updatedProfile

                    UserRepository.saveUserProfile(
                        profile = updatedProfile,
                        onSuccess = {
                            Glide.with(this).load(downloadUri).circleCrop().into(binding.imgProfile)
                            Toast.makeText(requireContext(), "תמונה עודכנה ✓", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(requireContext(), "שגיאה בשמירה", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}