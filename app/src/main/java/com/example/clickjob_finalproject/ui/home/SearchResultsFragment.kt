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
                val todayCalendar = Calendar.getInstance()
                val tomorrowCalendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }

                allItems = jobs.map { job ->
                    val jobCalendar = Calendar.getInstance().apply { timeInMillis = job.date }
                    val dateLabel = when {
                        jobCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> "היום"
                        jobCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(Calendar.DAY_OF_YEAR) -> "מחר"
                        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(job.date))
                    }

                    ResultItem(
                        id = job.id,
                        title = job.title,
                        price = "₪${job.salary}",
                        salary = job.salary.toIntOrNull() ?: 0,
                        address = job.address,
                        day = dateLabel,
                        distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                        category = job.category,
                        isUrgent = job.isUrgent,
                        date = job.date
                    )
                }

                adapter.updateItems(allItems)
                updateResultsCount(allItems.size)
            },
            onFailure = { }
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

                // Figma style: light gray pill, no stroke, dark text
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#E9E7E9")
                )
                chipStrokeWidth = 0f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.DarkDeep))
                textSize = 13f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.ploni_regular_aaa)

                // Fully rounded pill shape
                chipCornerRadius = 50f

                // Small dark close icon
                closeIconTint = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.DarkDeep)
                )
                closeIconSize = 16f * resources.displayMetrics.density / 2.5f
                chipStartPadding = 10f
                chipEndPadding = 10f

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