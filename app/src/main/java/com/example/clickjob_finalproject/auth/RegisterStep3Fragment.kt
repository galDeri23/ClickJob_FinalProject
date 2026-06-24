package com.example.clickjob_finalproject.auth

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterStep3Fragment : Fragment() {

    private val categories = listOf(
        "שפות" to listOf(
            "עברית", "אנגלית", "ערבית", "רוסית", "צרפתית", "ספרדית",
            "גרמנית", "פורטוגזית", "איטלקית", "סינית", "יפנית",
            "טורקית", "אמהרית", "רומנית"
        ),
        "רישיונות" to listOf(
            "רישיון B", "רישיון C", "רישיון D", "רישיון A",
            "רישיון 1", "רישיון מלגזה", "רישיון טרקטור", "רישיון כלי עבודה כבדים"
        ),
        "תעודות" to listOf(
            "בגרות", "תואר ראשון", "תואר שני", "תעודת הוראה",
            "תעודת הצלה", "תעודת עזרה ראשונה", "תעודת מגישה",
            "תעודת ברמן", "תעודת שמירה", "תעודת כשרות",
            "תעודת בטיחות", "תעודת מחשבים"
        ),
        "תוכנות" to listOf(
            "Excel", "Word", "PowerPoint", "Photoshop", "Illustrator",
            "AutoCAD", "SAP", "Priority", "Monday", "Salesforce",
            "Google Workspace", "Figma", "Adobe Premiere"
        ),
        "קטגוריית משרות" to listOf(
            "חינוך והוראה", "בעלי חיים", "הפקה ואירועים", "רפואה ורווחה",
            "טכנולוגיה", "בניין וייצור", "משלוחים ותחבורה", "מסעדנות",
            "אבטחה וביטחון", "אפסנאות ולוגיסטיקה", "שירות לקוחות",
            "עיצוב וקריאייטיב", "מכירות ואופנה", "אחזקה"
        ),
        "מיומנויות רכות" to listOf(
            "אסרטיביות", "עבודת צוות", "יצירתיות", "פתרון בעיות",
            "תקשורת בינאישית", "ניהול זמן", "מנהיגות", "גמישות מחשבתית",
            "אמינות", "סבלנות", "יוזמה", "דיוק ויסודיות",
            "שירותיות", "יכולת למידה מהירה"
        ),
        "אחר" to emptyList()
    )

    // Index of "אחר" row — no predefined chips, free text only
    private val OTHER_INDEX = 6
    // Index of job categories — no "אחר +" chip
    private val JOB_CATEGORIES_INDEX = 4

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
            val chipGroupCustom = row.findViewById<ChipGroup>(R.id.chipGroupCustom)
            val arrow = row.findViewById<ImageView>(R.id.ivArrow)
            val chipAddOther = row.findViewById<Chip>(R.id.chipAddOther)
            val tilOtherInput = row.findViewById<TextInputLayout>(R.id.tilOtherInput)
            val etOtherInput = row.findViewById<TextInputEditText>(R.id.etOtherInput)

            title.text = categories[index].first

            if (index == OTHER_INDEX) {
                // "אחר" row - free text input only, no predefined chips
                chipAddOther.visibility = View.GONE
                tilOtherInput.visibility = View.VISIBLE
                chipGroupCustom.visibility = View.VISIBLE

                etOtherInput.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val text = etOtherInput.text.toString().trim()
                        if (text.isNotEmpty()) {
                            addCustomChip(text, chipGroupCustom)
                            etOtherInput.text?.clear()
                        }
                        true
                    } else false
                }
            } else {
                // Regular rows - predefined chips
                categories[index].second.forEach { chipText ->
                    val chip = Chip(requireContext())
                    chip.text = chipText
                    chip.isCheckable = true
                    chip.textSize = 13f
                    chipGroup.addView(chip)
                }

                // Hide "אחר +" for job categories
                if (index == JOB_CATEGORIES_INDEX) {
                    chipAddOther.visibility = View.GONE
                } else {
                    chipAddOther.visibility = View.VISIBLE
                    chipAddOther.setOnClickListener {
                        showAddCustomDialog(chipGroup)
                    }
                }
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

    private fun showAddCustomDialog(chipGroup: ChipGroup) {
        val input = TextInputEditText(requireContext())
        input.hint = "הוסף..."
        input.gravity = android.view.Gravity.END

        AlertDialog.Builder(requireContext())
            .setTitle("הוסף אחר")
            .setView(input)
            .setPositiveButton("הוסף") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    addCustomChip(text, chipGroup)
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun addCustomChip(text: String, chipGroup: ChipGroup) {
        val chip = Chip(requireContext())
        chip.text = text
        chip.isCheckable = true
        chip.isChecked = true
        chip.textSize = 13f
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener { chipGroup.removeView(chip) }
        chipGroup.addView(chip)
    }

    fun getSelectedSkills(): Step3Data {
        val view = requireView()

        fun getSelected(rowId: Int): List<String> {
            val row = view.findViewById<LinearLayout>(rowId)
            val chipGroup = row.findViewById<ChipGroup>(R.id.chipGroup)
            val chipGroupCustom = row.findViewById<ChipGroup>(R.id.chipGroupCustom)
            val selected = mutableListOf<String>()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                if (chip?.isChecked == true) selected.add(chip.text.toString())
            }
            for (i in 0 until chipGroupCustom.childCount) {
                val chip = chipGroupCustom.getChildAt(i) as? Chip
                if (chip?.isChecked == true) selected.add(chip.text.toString())
            }
            return selected
        }

        fun getCustomOther(): List<String> {
            val row = view.findViewById<LinearLayout>(R.id.rowOther)
            val chipGroupCustom = row.findViewById<ChipGroup>(R.id.chipGroupCustom)
            val selected = mutableListOf<String>()
            for (i in 0 until chipGroupCustom.childCount) {
                val chip = chipGroupCustom.getChildAt(i) as? Chip
                selected.add(chip?.text.toString())
            }
            return selected
        }

        return Step3Data(
            languages = getSelected(R.id.rowLanguages),
            licenses = getSelected(R.id.rowLicenses),
            certificates = getSelected(R.id.rowCertificates),
            software = getSelected(R.id.rowSoftware),
            jobCategories = getSelected(R.id.rowProgramming),
            softSkills = getSelected(R.id.rowSoftSkills),
            other = getCustomOther()
        )
    }
}

data class Step3Data(
    val languages: List<String>,
    val licenses: List<String>,
    val certificates: List<String>,
    val software: List<String>,
    val jobCategories: List<String>,
    val softSkills: List<String>,
    val other: List<String>
)