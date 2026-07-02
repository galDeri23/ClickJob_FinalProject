package com.example.clickjob_finalproject.ui.myjobs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.data.model.Application
import com.example.clickjob_finalproject.data.model.JobPost
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentWorkerProfileBinding
import com.google.android.flexbox.FlexboxLayout

class WorkerProfileFragment : Fragment() {

    private var _binding: FragmentWorkerProfileBinding? = null
    private val binding get() = _binding!!

    private var applicationId: String = ""
    private var workerId: String = ""
    private var jobId: String = ""
    private var isDocumentsExpanded = false
    private var currentApplication: Application? = null
    private var bottomNav: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applicationId = arguments?.getString("applicationId") ?: ""
        workerId = arguments?.getString("workerId") ?: ""
        jobId = arguments?.getString("jobId") ?: ""

        setupBackButton()
        loadWorkerProfile()
        loadApplicationStatus()
    }

    private fun setupBackButton() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Loads the application to adjust the approve button by its status
    private fun loadApplicationStatus() {
        if (applicationId.isEmpty() || jobId.isEmpty()) return

        UserRepository.getJobApplications(
            jobId = jobId,
            onSuccess = { applications ->
                currentApplication = applications.find { it.id == applicationId }
                when (currentApplication?.status) {
                    "employer_approved" -> {
                        binding.btnApproveWorker.isEnabled = false
                        binding.btnApproveWorker.text = "ממתין לאישור העובד"
                    }
                    "confirmed" -> {
                        binding.btnApproveWorker.isEnabled = false
                        binding.btnApproveWorker.text = "העובד אושר ✓"
                    }
                }
            },
            onFailure = { }
        )
    }

    // Loads worker profile from Firestore
    private fun loadWorkerProfile() {
        UserRepository.getUserProfileById(
            userId = workerId,
            onSuccess = { profile ->
                binding.tvName.text = profile.name
                binding.tvJobTitle.text = profile.jobCategories.firstOrNull() ?: ""

                if (profile.rating > 0.0) {
                    binding.ratingRow.visibility = View.VISIBLE
                    binding.tvRating.text = String.format("%.1f", profile.rating)
                } else {
                    binding.ratingRow.visibility = View.GONE
                }

                // Load profile image
                if (profile.profileImageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(profile.profileImageUrl)
                        .circleCrop()
                        .into(binding.imgProfile)
                }

                // Hard skills
                val hardSkills = (profile.languages + profile.licenses +
                        profile.certificates + profile.software +
                        profile.jobCategories).distinct()
                hardSkills.forEach { addChip(binding.flexHardSkills, it) }

                // Soft skills
                profile.softSkills.forEach { addChip(binding.flexSoftSkills, it) }

                // Documents count
                val count = (if (profile.cvUrl.isNotEmpty()) 1 else 0) + profile.documents.size
                binding.tvDocumentsCount.text = "($count)"

                // Contact buttons
                binding.btnPhone.setOnClickListener {
                    if (profile.phone.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile.phone}"))
                        startActivity(intent)
                    }
                }

                binding.btnSocial.setOnClickListener {
                    if (profile.instagramUrl.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.instagramUrl))
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "לא הוזן קישור לאינסטגרם", Toast.LENGTH_SHORT).show()
                    }
                }

                // Documents expandable
                binding.btnDocuments.setOnClickListener {
                    toggleDocuments(profile.cvUrl, profile.cvName, profile.documents)
                }

                // Approve button
                binding.btnApproveWorker.setOnClickListener {
                    approveWorker()
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה בטעינת פרופיל העובד", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun toggleDocuments(
        cvUrl: String,
        cvName: String,
        documents: List<com.example.clickjob_finalproject.data.model.Document>
    ) {
        isDocumentsExpanded = !isDocumentsExpanded

        binding.ivDocumentsArrow.animate()
            .rotation(if (isDocumentsExpanded) -90f else 0f)
            .setDuration(200)
            .start()

        if (isDocumentsExpanded) {
            binding.layoutDocumentsList.visibility = View.VISIBLE
            binding.tvCvName.text = if (cvName.isNotEmpty()) cvName else "קורות חיים"
            binding.rowCv.setOnClickListener {
                if (cvUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl))
                    startActivity(intent)
                }
            }

            binding.layoutAdditionalDocs.removeAllViews()
            documents.forEach { doc ->
                val tv = TextView(requireContext()).apply {
                    text = doc.name
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_dark, null))
                    setPadding(0, 8, 0, 8)
                    setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.url))
                        startActivity(intent)
                    }
                }
                binding.layoutAdditionalDocs.addView(tv)
            }
        } else {
            binding.layoutDocumentsList.visibility = View.GONE
        }
    }

    private fun approveWorker() {
        if (applicationId.isEmpty() || jobId.isEmpty()) return

        UserRepository.getJobById(
            jobId = jobId,
            onSuccess = { job ->
                val application = currentApplication
                if (application != null) {
                    doApprove(application, job)
                } else {
                    // Fallback: fetch the application if it was not loaded yet
                    UserRepository.getJobApplications(
                        jobId = jobId,
                        onSuccess = { applications ->
                            val found = applications.find { it.id == applicationId }
                            if (found == null) {
                                Toast.makeText(requireContext(), "המועמדות לא נמצאה", Toast.LENGTH_SHORT).show()
                                return@getJobApplications
                            }
                            doApprove(found, job)
                        },
                        onFailure = {
                            Toast.makeText(requireContext(), "שגיאה באישור", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה באישור", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun doApprove(application: Application, job: JobPost) {
        binding.btnApproveWorker.isEnabled = false
        UserRepository.approveApplicant(
            application = application,
            job = job,
            onSuccess = {
                Toast.makeText(requireContext(), "העובד אושר!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            },
            onFailure = {
                binding.btnApproveWorker.isEnabled = true
                Toast.makeText(requireContext(), "שגיאה באישור", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun addChip(container: FlexboxLayout, text: String) {
        val chip = TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_dark, null))
            setPadding(
                resources.getDimensionPixelSize(R.dimen.chip_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.chip_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.chip_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.chip_padding_vertical)
            )
            background = resources.getDrawable(R.drawable.bg_gray, null)
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

    override fun onResume() {
        super.onResume()
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        bottomNav?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}