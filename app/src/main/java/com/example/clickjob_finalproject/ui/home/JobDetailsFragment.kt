package com.example.clickjob_finalproject.ui.home

import android.content.Intent
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

        if (jobId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "שגיאה בטעינת המשרה", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupBackButton()
        setupFavoriteButton()
        loadJob(jobId)
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


    // Loads job details from Firestore and populates the UI
    private fun loadJob(jobId: String) {
        UserRepository.getJobById(
            jobId = jobId,
            onSuccess = { job ->
                binding.tvCompanyName.text = job.company
                binding.tvCategory.text = job.category
                binding.tvAddress.text = job.address
                binding.tvDescription.text = job.description

                // Format date and time
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(job.date))
                binding.tvDay1.text = "$dateStr  ${job.startTime} - ${job.endTime}"

                // Load job image if exists
                if (job.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(job.imageUrl)
                        .centerCrop()
                        .into(binding.imgHeader)
                }

                // Requirements chips
                bindRequirementChips(job.requirements)

                // Share button
                binding.btnShare.setOnClickListener {
                    val shareText = "משרה ב-${job.company}: ${job.title}\n" +
                            "קטגוריה: ${job.category}\n" +
                            "שכר: ₪${job.salary} לשעה\n" +
                            "כתובת: ${job.address}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(Intent.createChooser(intent, "שתף משרה"))
                }

                // Apply button
                binding.btnApply.setOnClickListener {
                    Toast.makeText(requireContext(), "המועמדות נשלחה!", Toast.LENGTH_SHORT).show()
                }

                // Address button - open maps
                binding.btnAddress.setOnClickListener {
                    if (job.address.isNotEmpty()) {
                        val encodedAddress = Uri.encode(job.address)
                        // Try Waze first, fallback to Google Maps
                        try {
                            val wazeUri = Uri.parse("waze://?q=$encodedAddress&navigate=yes")
                            val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri)
                            startActivity(wazeIntent)
                        } catch (e: Exception) {
                            val mapsUri = Uri.parse("geo:0,0?q=$encodedAddress")
                            val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri)
                            startActivity(mapsIntent)
                        }
                    } else {
                        Toast.makeText(requireContext(), "לא הוזנה כתובת", Toast.LENGTH_SHORT).show()
                    }
                }

                // Employer profile button
                binding.btnEmployerProfile.setOnClickListener {
                    if (job.link.isNotEmpty()) {
                        val url = if (job.link.startsWith("http")) job.link else "https://${job.link}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
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