package com.example.clickjob_finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class RegisterStep3Fragment : Fragment() {

    // Category title + sample chips
    private val categories = listOf(
        "שפות" to listOf("עברית", "אנגלית", "ערבית"),
        "רישיונות" to listOf("רישיון B", "רישיון C"),
        "תעודות" to listOf("בגרות", "תואר ראשון"),
        "תוכנות" to listOf("Excel", "Word", "Photoshop"),
        "שפות תכנות" to listOf("Python", "Kotlin", "Java"),
        "מיומנויות רכות" to listOf("אסרטיבי", "עבודת צוות", "יצירתיות"),
        "אחר" to listOf("סקיל", "סקיל")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rowIds = listOf(
            R.id.rowLanguages,
            R.id.rowLicenses,
            R.id.rowCertificates,
            R.id.rowSoftware,
            R.id.rowProgramming,
            R.id.rowSoftSkills,
            R.id.rowOther
        )

        rowIds.forEachIndexed { index, rowId ->
            val row = view.findViewById<LinearLayout>(rowId)
            val header = row.findViewById<LinearLayout>(R.id.llHeader)
            val content = row.findViewById<LinearLayout>(R.id.llContent)
            val title = row.findViewById<TextView>(R.id.tvCategoryTitle)
            val chipGroup = row.findViewById<ChipGroup>(R.id.chipGroup)
            val arrow = row.findViewById<android.widget.ImageView>(R.id.ivArrow)

            // Set title
            title.text = categories[index].first

            // Add chips
            categories[index].second.forEach { chipText ->
                val chip = Chip(requireContext())
                chip.text = chipText
                chip.isCheckable = true
                chip.textSize = 13f
                chipGroup.addView(chip)
            }

            // Toggle expand/collapse
            header.setOnClickListener {
                if (content.visibility == View.GONE) {
                    content.visibility = View.VISIBLE
                    arrow.rotation = 180f
                } else {
                    content.visibility = View.GONE
                    arrow.rotation = 0f
                }
            }
        }
    }
}