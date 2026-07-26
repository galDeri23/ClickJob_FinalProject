package com.example.clickjob_finalproject.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.ResultItem
import com.example.clickjob_finalproject.adapters.SearchResultsAdapter
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentSearchResultsBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SearchResultsFragment : Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchResultsAdapter
    private val selectedCategories = mutableListOf<String>()
    private var allItems = listOf<ResultItem>()

    // User's city from profile - used by the "near" tab
    private var userCity: String = ""
    private var selectedWorkFrequency: String = ""
    private var selectedSalaryType: String = ""
    private var minSalary: Int = 0
    private var maxDistanceKm: Int = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getStringArrayList("selectedCategories")?.let {
            selectedCategories.addAll(it)
        }
        selectedWorkFrequency = arguments?.getString("workFrequency") ?: ""
        selectedSalaryType = arguments?.getString("salaryType") ?: ""
        minSalary = arguments?.getInt("minSalary", 0) ?: 0
        maxDistanceKm = arguments?.getInt("maxDistanceKm", 10) ?: 10

        setupBackButton()
        setupAdapter()
        setupTabs()
        setupCategoryChips()
        loadUserCity()
        loadJobs()
    }

    private fun setupBackButton() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAdapter() {
        adapter = SearchResultsAdapter(emptyList()) { item ->
            val args = bundleOf("jobId" to item.id)
            findNavController().navigate(
                R.id.action_searchResultsFragment_to_jobDetailsFragment, args
            )
        }
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter
    }

    // Loads the user's city once for the "near" tab sorting
    private fun loadUserCity() {
        UserRepository.getUserProfile(
            onSuccess = { profile ->
                userCity = profile.city
            },
            onFailure = { }
        )
    }

    // Loads jobs from Firestore filtered by selected categories
    private fun loadJobs() {
        UserRepository.searchJobs(
            categories = selectedCategories,
            onSuccess = { jobs ->

                android.util.Log.d("SEARCH_DEBUG", "jobs from repository = ${jobs.size}")
                android.util.Log.d("SEARCH_DEBUG", "selectedCategories = $selectedCategories")
                android.util.Log.d("SEARCH_DEBUG", "selectedWorkFrequency = $selectedWorkFrequency")
                android.util.Log.d("SEARCH_DEBUG", "selectedSalaryType = $selectedSalaryType")
                android.util.Log.d("SEARCH_DEBUG", "minSalary = $minSalary")
                android.util.Log.d("SEARCH_DEBUG", "maxDistanceKm = $maxDistanceKm")

                jobs.forEach { job ->
                    android.util.Log.d(
                        "SEARCH_DEBUG",
                        "job=${job.title}, category=${job.category}, workFrequency=${job.workFrequency}, salaryType=${job.salaryType}, salary=${job.salary}"
                    )
                }

                val filteredJobs = jobs.filter { job ->

                    val matchesWorkFrequency =
                        selectedWorkFrequency.isEmpty() ||
                                job.workFrequency.isEmpty() ||
                                job.workFrequency == selectedWorkFrequency

                    val matchesSalaryType =
                        selectedSalaryType.isEmpty() ||
                                job.salaryType.isEmpty() ||
                                job.salaryType == selectedSalaryType

                    val salaryValue = job.salary.filter { it.isDigit() }.toIntOrNull() ?: 0

                    val matchesSalary =
                        minSalary == 0 || salaryValue == 0 || salaryValue >= minSalary

                    android.util.Log.d(
                        "SEARCH_DEBUG",
                        "FILTER job=${job.title}: freq=$matchesWorkFrequency, salaryType=$matchesSalaryType, salary=$matchesSalary"
                    )

                    matchesWorkFrequency && matchesSalaryType && matchesSalary
                }

                android.util.Log.d("SEARCH_DEBUG", "filteredJobs = ${filteredJobs.size}")

                val todayCalendar = Calendar.getInstance()
                val tomorrowCalendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }

                allItems = filteredJobs.map { job ->
                    val jobCalendar = Calendar.getInstance().apply {
                        timeInMillis = job.date
                    }

                    val dateLabel = when {
                        jobCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> "היום"
                        jobCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(Calendar.DAY_OF_YEAR) -> "מחר"
                        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(job.date))
                    }

                    ResultItem(
                        id = job.id,
                        title = job.title,
                        company = job.company,
                        price = "₪${job.salary}",
                        salary = job.salary.filter { it.isDigit() }.toIntOrNull() ?: 0,
                        day = dateLabel,
                        distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                        category = job.category,
                        isUrgent = job.isUrgent,
                        date = job.date
                    )
                }

                val sorted = allItems.sortedBy { it.date }
                adapter.updateItems(sorted)
                updateResultsCount(sorted.size)
            },
            onFailure = { e ->
                android.util.Log.e("SEARCH_DEBUG", "searchJobs failed: ${e.message}")
            }
        )
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.removeAllViews()

        if (selectedCategories.isEmpty()) {
            binding.chipGroupCategories.visibility = View.GONE
            return
        }

        binding.chipGroupCategories.visibility = View.VISIBLE

        selectedCategories.toList().forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCloseIconVisible = true
                isClickable = false
                isCheckable = false

                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#E9E7E9")
                )
                chipStrokeWidth = 0f
                chipCornerRadius = 50f

                setTextColor(ContextCompat.getColor(requireContext(), R.color.DarkDeep))
                textSize = 13f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.ploni_regular_aaa)

                closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.close)
                closeIconTint = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.DarkDeep)
                )
                closeIconSize = 18f

                chipStartPadding = 12f
                chipEndPadding = 12f
                textStartPadding = 4f
                textEndPadding = 4f
                closeIconStartPadding = 6f
                closeIconEndPadding = 4f

                minHeight = 34
                height = 34

                setOnCloseIconClickListener {
                    selectedCategories.remove(category)
                    setupCategoryChips()
                    loadJobs()
                }
            }

            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun updateResultsCount(count: Int) {
        binding.tvResultsCount.text = "תוצאות חיפוש ($count)"
    }

    private fun setupTabs() {
        val tabs = listOf(binding.tabAll, binding.tabNear, binding.tabHighSalary, binding.tabUrgent)

        // Default - select "all" tab
        setTabSelected(binding.tabAll)
        tabs.drop(1).forEach { setTabUnselected(it) }

        binding.tabAll.setOnClickListener {
            tabs.forEach { setTabUnselected(it) }
            setTabSelected(binding.tabAll)
            // Sort by date - closest first
            val sorted = allItems.sortedBy { it.date }
            adapter.updateItems(sorted)
            updateResultsCount(sorted.size)
        }

        binding.tabNear.setOnClickListener {
            tabs.forEach { setTabUnselected(it) }
            setTabSelected(binding.tabNear)
            // Jobs in the user's city first, then the rest, each group by date
            val sorted = allItems.sortedWith(
                compareByDescending<ResultItem> { userCity.isNotEmpty() && it.distance == userCity }
                    .thenBy { it.date }
            )
            adapter.updateItems(sorted)
            updateResultsCount(sorted.size)
        }

        binding.tabHighSalary.setOnClickListener {
            tabs.forEach { setTabUnselected(it) }
            setTabSelected(binding.tabHighSalary)
            // Sort by salary - highest first
            val sorted = allItems.sortedByDescending { it.salary }
            adapter.updateItems(sorted)
            updateResultsCount(sorted.size)
        }

        binding.tabUrgent.setOnClickListener {
            tabs.forEach { setTabUnselected(it) }
            setTabSelected(binding.tabUrgent)
            // Filter urgent only, closest date first
            val urgent = allItems.filter { it.isUrgent }.sortedBy { it.date }
            adapter.updateItems(urgent)
            updateResultsCount(urgent.size)
        }
    }

    private fun setTabSelected(tab: TextView) {
        tab.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_selected)
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        tab.typeface = ResourcesCompat.getFont(requireContext(), R.font.ploni_bold_aaa)
    }

    private fun setTabUnselected(tab: TextView) {
        tab.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_unselected_box)
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
        tab.typeface = ResourcesCompat.getFont(requireContext(), R.font.ploni_regular_aaa)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}