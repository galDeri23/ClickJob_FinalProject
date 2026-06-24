package com.example.clickjob_finalproject.ui.home
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clickjob_finalproject.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class JobDetailsFragment : Fragment(R.layout.fragment_job_details) {


    private var bottomNav: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // --- Find views ---

        val imgHeader = view.findViewById<ImageView>(R.id.imgHeader)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnFavorite = view.findViewById<ImageButton>(R.id.btnFavorite)
        val btnShare = view.findViewById<ImageButton>(R.id.btnShare)

        val imgLogo = view.findViewById<ImageView>(R.id.imgLogo)
        val tvCompanyName = view.findViewById<TextView>(R.id.tvCompanyName)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)

        val btnEmployerProfile = view.findViewById<View>(R.id.btnEmployerProfile)
        val btnAddress = view.findViewById<View>(R.id.btnAddress)
        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)

        val chipGroupRequirements = view.findViewById<ChipGroup>(R.id.chipGroupRequirements)
        val btnApply = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApply)

        // --- Sample data (replace later with real data from arguments / ViewModel) ---

        tvCompanyName.text = "wat-san"
        tvCategory.text = "מסעדנות ואירוח"
        tvAddress.text = "כתובת"

        // This is exactly the place to plug in a real list of requirements per job.
        // The number of chips is NOT fixed - bindRequirementChips() clears the group
        // and adds one Chip per string, so 1, 5, or 10 requirements all work the same way.
        val requirements = listOf(
            "שליטה בצ'ופסטיקס",
            "ניסיון באירוח",
            "שירותיות"
        )
        bindRequirementChips(chipGroupRequirements, requirements)

        // --- Click listeners (just enough so nothing crashes) ---

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        var isFavorite = false
        btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            Toast.makeText(
                requireContext(),
                if (isFavorite) "נשמר למשרות שאהבת" else "הוסר מהמשרות שאהבת",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "שיתוף משרה (TODO: לחבר Intent.ACTION_SEND)", Toast.LENGTH_SHORT).show()
        }

        btnEmployerProfile.setOnClickListener {
            Toast.makeText(requireContext(), "מעבר לפרופיל מעסיק (TODO)", Toast.LENGTH_SHORT).show()
        }

        btnAddress.setOnClickListener {
            Toast.makeText(requireContext(), "פתיחת מפה לכתובת (TODO)", Toast.LENGTH_SHORT).show()
        }

        btnApply.setOnClickListener {
            Toast.makeText(requireContext(), "המועמדות נשלחה!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Clears the ChipGroup and adds one Chip per requirement string.
     * Call this with a different-sized list for every job - the layout
     * doesn't need to change, the chips just wrap to as many rows as needed.
     */
    private fun bindRequirementChips(chipGroup: ChipGroup, requirements: List<String>) {
        chipGroup.removeAllViews()

        for (requirement in requirements) {
            val chip = Chip(chipGroup.context).apply {
                text = requirement
                isClickable = false
                isCheckable = false
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(0xFFEFEFEF.toInt())
                chipStrokeWidth = 0f
            }
            chipGroup.addView(chip)
        }
    }

    // Hide the bottom nav bar while this screen is on top
    override fun onResume() {
        super.onResume()
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE
    }

    // Restore it as soon as we leave this screen (back press, navigating away, etc.)
    override fun onPause() {
        super.onPause()
        bottomNav?.visibility = View.VISIBLE
    }
}