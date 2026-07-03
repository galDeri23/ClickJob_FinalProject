package com.example.clickjob_finalproject.ui.home

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentJobDetailsBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobDetailsFragment : Fragment() {

    private var _binding: FragmentJobDetailsBinding? = null
    private val binding get() = _binding!!

    private var bottomNav: View? = null

    // Maps job category name to its circle image drawable
    private val categoryImages = mapOf(
        "מסעדות" to R.drawable.img_cat_circle_hospitality,
        "אבטחה וביטחון" to R.drawable.img_cat_circle_security,
        "משלוחים ותחבורה" to R.drawable.img_cat_circle_delivery,
        "בניין וייצור" to R.drawable.img_cat_circle_construction,
        "חינוך והוראה" to R.drawable.img_cat_circle_education,
        "בעלי חיים" to R.drawable.img_cat_circle_pets,
        "אפסנאות ולוגיסטיקה" to R.drawable.img_cat_circle_logistics,
        "רפואה ובריאות" to R.drawable.img_cat_circle_health,
        "הפקה ואירועים" to R.drawable.img_cat_circle_events,
        "טכנולוגיה" to R.drawable.img_cat_circle_tech,
        "שירות לקוחות" to R.drawable.img_cat_circle_service,
        "מכירות ואופנה" to R.drawable.img_cat_circle_sales,
        "עיצוב וקריאייטיב" to R.drawable.img_cat_circle_creative,
        "אחזקה" to R.drawable.img_cat_circle_maintenance
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jobId = arguments?.getString("jobId")
        val applicationId = arguments?.getString("applicationId")
        val isViewOnly = arguments?.getBoolean("isViewOnly", false) ?: false
        if (isViewOnly) {
            binding.btnApply.visibility = View.GONE
        }

        if (jobId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "שגיאה בטעינת המשרה", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupBackButton()
        setupFavoriteButton()
        loadJob(jobId, applicationId)
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupFavoriteButton() {
        var isFavorite = false
        binding.btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            Toast.makeText(
                requireContext(),
                if (isFavorite) "נשמר למשרות שאהבת" else "הוסר מהמשרות שאהבת",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadJob(jobId: String, applicationId: String?) {
        UserRepository.getJobById(
            jobId = jobId,
            onSuccess = { job ->
                // Business card: job title, company name, address
                binding.tvJobTitle.text = job.title
                binding.tvCompanyName.text = job.company
                binding.tvAddressCard.text = job.address
                binding.tvDescription.text = job.description

                // Category circle image
                val categoryImage = categoryImages[job.category]
                if (categoryImage != null) {
                    binding.imgLogo.setImageResource(categoryImage)
                }

                // Salary box
                binding.tvSalary.text = "₪${job.salary}"
                binding.tvSalaryType.text = if (job.salaryType == "daily") "ליום" else "לשעה"

                // Timing: date range or single day + hours
                binding.tvTiming.text = buildTimingText(job.date, job.endDate, job.startTime, job.endTime)

                // Job type based on work frequency
                binding.tvJobType.text = when (job.workFrequency) {
                    "חד פעמי" -> "עבודה חד פעמית"
                    "רציף" -> "עבודה רציפה, לאורך תקופה"
                    else -> "לא צוין"
                }

                if (job.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(job.imageUrl)
                        .centerCrop()
                        .into(binding.imgHeader)
                }

                bindRequirementChips(job.requirements)

                binding.btnShare.setOnClickListener {
                    val shareText = "משרה ב-${job.company}: ${job.title}\n" +
                            "קטגוריה: ${job.category}\n" +
                            "שכר: ₪${job.salary} ${if (job.salaryType == "daily") "ליום" else "לשעה"}\n" +
                            "כתובת: ${job.address}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(Intent.createChooser(intent, "שתף משרה"))
                }

                // Show confirm or apply button based on applicationId
                if (!applicationId.isNullOrEmpty()) {
                    // Came from notification/my jobs - show confirm button
                    binding.btnApply.text = "אישור עבודה"
                    binding.btnApply.setBackgroundColor(Color.parseColor("#24061E"))
                    binding.btnApply.setOnClickListener {
                        binding.btnApply.isEnabled = false
                        UserRepository.getJobApplications(
                            jobId = jobId,
                            onSuccess = { applications ->
                                val application = applications.find { it.id == applicationId }
                                if (application == null) {
                                    binding.btnApply.isEnabled = true
                                    return@getJobApplications
                                }
                                UserRepository.confirmJob(
                                    application = application,
                                    job = job,
                                    onSuccess = {
                                        Toast.makeText(requireContext(), "העבודה אושרה! ✓", Toast.LENGTH_SHORT).show()
                                        findNavController().popBackStack()
                                    },
                                    onFailure = {
                                        binding.btnApply.isEnabled = true
                                        Toast.makeText(requireContext(), "שגיאה באישור", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onFailure = {
                                binding.btnApply.isEnabled = true
                            }
                        )
                    }
                } else {
                    // Came from search/home - show apply button
                    binding.btnApply.text = "הגשת מועמדות"
                    binding.btnApply.setOnClickListener {
                        binding.btnApply.isEnabled = false
                        UserRepository.applyToJob(
                            job = job,
                            onSuccess = {
                                binding.btnApply.text = "המועמדות נשלחה ✓"
                                Toast.makeText(requireContext(), "המועמדות נשלחה בהצלחה!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                binding.btnApply.isEnabled = true
                                Toast.makeText(requireContext(), "שגיאה בשליחת המועמדות", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                binding.btnAddress.setOnClickListener {
                    if (job.address.isNotEmpty()) {
                        val encodedAddress = Uri.encode(job.address)
                        try {
                            val wazeUri = Uri.parse("waze://?q=$encodedAddress&navigate=yes")
                            startActivity(Intent(Intent.ACTION_VIEW, wazeUri))
                        } catch (e: Exception) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encodedAddress")))
                        }
                    } else {
                        Toast.makeText(requireContext(), "לא הוזנה כתובת", Toast.LENGTH_SHORT).show()
                    }
                }

                binding.btnEmployerProfile.setOnClickListener {
                    if (job.link.isNotEmpty()) {
                        val url = if (job.link.startsWith("http")) job.link else "https://${job.link}"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } else {
                        Toast.makeText(requireContext(), "לא הוזן קישור לעסק", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה בטעינת המשרה", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Builds the timing line: single day or date range, with hours
    private fun buildTimingText(startDate: Long, endDate: Long, startTime: String, endTime: String): String {
        val format = SimpleDateFormat("dd/MM", Locale.getDefault())
        val startStr = format.format(Date(startDate))

        // Fallback for old jobs without endDate
        val effectiveEnd = if (endDate > 0L) endDate else startDate
        val endStr = format.format(Date(effectiveEnd))

        val dateText = if (startStr == endStr) {
            "בתאריך $startStr"
        } else {
            "בתאריך $startStr-$endStr"
        }

        return "$dateText, שעות $startTime-$endTime"
    }

    private fun bindRequirementChips(requirements: List<String>) {
        binding.chipGroupRequirements.removeAllViews()
        for (requirement in requirements) {
            val chip = Chip(requireContext()).apply {
                text = requirement
                isClickable = false
                isCheckable = false
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(0xFFEFEFEF.toInt())
                chipStrokeWidth = 0f
            }
            binding.chipGroupRequirements.addView(chip)
        }
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