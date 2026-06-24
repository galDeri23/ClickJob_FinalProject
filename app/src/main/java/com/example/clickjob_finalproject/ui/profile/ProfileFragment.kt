package com.example.clickjob_finalproject.ui.profile

import android.R.attr.fontFamily
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.databinding.FragmentProfileBinding
import com.google.android.flexbox.FlexboxLayout

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // TODO: replace with real data loaded from Firebase for the current user
    private val hardSkills = listOf(
        "חשמלאי", "אינסטלטור", "יזמות נדל\"ן", "מאלף",
        "מעלף", "מהלף", "אלוף", "יודע לבחור אבטיח"
    )

    private val softSkills = listOf(
        "אסרטיבי", "מנלומן", "נדען", "מיזופון", "שונא ג'ינג'ים",
        "שונא שחורים", "שונא סנלים", "שונא אדומים", "אנטישמי",
        "מצ'ו מצ'ו", "מצ'ואיסט"
    )

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

        setupChips()
        setupClickListeners()
    }

    private fun setupChips() {
        hardSkills.forEach { skill ->
            addChip(binding.flexHardSkills, skill)
        }
        softSkills.forEach { skill ->
            addChip(binding.flexSoftSkills, skill)
        }
    }

    // Creates a single chip TextView and adds it to the FlexboxLayout.
    // Replace the sample data above with Firebase data and call this the same way.
    private fun addChip(container: FlexboxLayout, text: String) {
        val chip = TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_dark, null))
            fontFamily
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
                resources.getDimensionPixelSize(R.dimen.chip_margin)
            )
        }
        chip.layoutParams = lp
        container.addView(chip)
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            Toast.makeText(requireContext(), "עריכת פרופיל (TODO)", Toast.LENGTH_SHORT).show()
        }

        binding.btnPhone.setOnClickListener {
            Toast.makeText(requireContext(), "חיוג (TODO)", Toast.LENGTH_SHORT).show()
        }

        binding.btnSocial.setOnClickListener {
            Toast.makeText(requireContext(), "פתיחת רשת חברתית (TODO)", Toast.LENGTH_SHORT).show()
        }

        binding.btnDocuments.setOnClickListener {
            Toast.makeText(requireContext(), "קו\"ח ומסמכים (TODO)", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "התנתקות (TODO)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}