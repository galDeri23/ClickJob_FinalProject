package com.example.clickjob_finalproject.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.databinding.FragmentSearchBinding
import com.google.android.flexbox.FlexboxLayout

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var bottomNav: View? = null
    data class Category(val name: String, val iconRes: Int)

    private val categories by lazy {
        listOf(
            Category("חינוך והוראה", R.drawable.ic_education),
            Category("בעלי חיים", R.drawable.ic_animals),
            Category("הפקה ואירועים", R.drawable.ic_events),
            Category("טכנולוגיה", R.drawable.ic_technology),
            Category("רפואה ובריאות", R.drawable.ic_medical),
            Category("בניין וייצור", R.drawable.ic_construction),
            Category("משלוחים ותחבורה", R.drawable.ic_delivery),
            Category("מסעדות", R.drawable.ic_restaurants),
            Category("אבטחה וביטחון", R.drawable.ic_security),
            Category("אפסנאות ולוגיסטיקה", R.drawable.ic_logistics),
            Category("שירות לקוחות", R.drawable.ic_customer_service),
            Category("עיצוב וקריאייטיב", R.drawable.ic_design),
            Category("מכירות ואופנה", R.drawable.ic_fashion),
            Category("אחזקה", R.drawable.ic_maintenance)
        )
    }

    private val distanceOptions = listOf(
        "עד 2 ק\"מ",
        "עד 5 ק\"מ",
        "עד 10 ק\"מ",
        "עד 20 ק\"מ",
        "עד 50 ק\"מ"
    )

    private val selectedCategories = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackButton()
        setupCategoryChips()
        setupSalarySeekBar()
        setupDistanceSpinner()
        setupSearchButton()
    }

    private fun setupBackButton() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategoryChips() {
        binding.flexboxCategories.removeAllViews()

        categories.forEach { category ->
            val chip = buildChipView(category)
            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 6, 0, 0)
            }
            binding.flexboxCategories.addView(chip, params)
        }
    }

    private fun buildChipView(category: Category): LinearLayout {
        val chip = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            // RTL: icon on right, text on left
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER_VERTICAL
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip)
            setPadding(dp(12), dp(6), dp(12), dp(6))
            isClickable = true
            isFocusable = true
        }

        val icon = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dp(20), dp(20)).apply {
                marginEnd = dp(5)
            }
            setImageResource(category.iconRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val label = TextView(requireContext()).apply {
            text = category.name
            textSize = 11f
            maxLines = 1
            // Prevent text from being clipped
            setSingleLine(true)
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        }

        chip.addView(icon)
        chip.addView(label)

        var selected = false
        chip.setOnClickListener {
            selected = !selected
            if (selected) {
                selectedCategories.add(category.name)
                chip.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_selected)
                // Keep text visible with pink color — do NOT change background of icon
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_pink))
            } else {
                selectedCategories.remove(category.name)
                chip.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip)
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
            }
        }

        return chip
    }

    private fun setupSalarySeekBar() {
        binding.seekBarSalary.progress = 50
        binding.tvSalaryValue.text = "50 ש״ח"

        binding.seekBarSalary.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSalaryValue.text = "$progress ש״ח"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupDistanceSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            distanceOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistance.adapter = adapter
        binding.spinnerDistance.setSelection(2)
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val bundle = Bundle().apply {
                putStringArrayList("selectedCategories", ArrayList(selectedCategories))
            }
            findNavController().navigate(R.id.action_searchFragment_to_searchResultsFragment, bundle)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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