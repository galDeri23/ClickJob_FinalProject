package com.example.clickjob_finalproject.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.JobAdapter
import com.example.clickjob_finalproject.adapters.JobItem
import com.example.clickjob_finalproject.adapters.ShiftAdapter
import com.example.clickjob_finalproject.adapters.ShiftItem
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var loadedCount = 0
    private val totalToLoad = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading state
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.INVISIBLE
        loadedCount = 0

        setupSearchBar()
        setupJobPosting()
        setupEmptyShiftCard()
        loadUserLocation()
        setupUpcomingShifts()
        setupBestMatchList()
        setupUrgentList()
    }

    // Called when each section finishes loading
    private fun onSectionLoaded() {
        loadedCount++
        if (loadedCount >= totalToLoad) {
            binding.progressBar.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
        }
    }

    private fun setupJobPosting() {
        binding.btnPostJob.setOnClickListener {
            val args = bundleOf("openEmployerHistory" to true)
            findNavController().navigate(R.id.action_homeFragment_to_myJobsFragment, args)
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.isFocusable = false
        binding.etSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setupEmptyShiftCard() {
        binding.cardNoUpcomingShifts.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun loadUserLocation() {
        UserRepository.getUserProfile(
            onSuccess = { profile ->
                if (profile.address.isNotEmpty()) {
                    binding.tvLocation.text = profile.address
                }
            },
            onFailure = { }
        )
    }

    private fun setupUpcomingShifts() {
        UserRepository.getUpcomingShifts(
            onSuccess = { jobs ->
                if (jobs.isEmpty()) {
                    binding.tvSectionUpcoming.visibility    = View.GONE
                    binding.vpUpcoming.visibility           = View.GONE
                    binding.layoutDots.visibility           = View.GONE
                    binding.cardNoUpcomingShifts.visibility = View.VISIBLE
                    onSectionLoaded()
                    return@getUpcomingShifts
                }

                val dateFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                val todayCalendar = java.util.Calendar.getInstance()
                val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }

                val items = jobs.map { job ->
                    val jobCalendar = java.util.Calendar.getInstance().apply {
                        timeInMillis = job.date
                    }
                    val dateLabel = when {
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                        else -> dateFormat.format(java.util.Date(job.date))
                    }
                    ShiftItem(
                        id = job.id,
                        title = job.title,
                        company = job.company,
                        time = "${job.startTime} - ${job.endTime}",
                        date = dateLabel,
                        address = job.address,
                        category = job.category
                    )
                }

                binding.cardNoUpcomingShifts.visibility = View.GONE
                binding.tvSectionUpcoming.visibility    = View.VISIBLE
                binding.vpUpcoming.visibility           = View.VISIBLE
                binding.layoutDots.visibility           = View.VISIBLE

                binding.vpUpcoming.adapter = ShiftAdapter(items) { shift ->
                    val args = bundleOf("jobId" to shift.id, "isViewOnly" to true)
                    findNavController().navigate(R.id.action_homeFragment_to_jobDetailsFragment, args)
                }

                setupDots(items.size)
                binding.vpUpcoming.registerOnPageChangeCallback(
                    object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            updateDots(position)
                        }
                    }
                )
                onSectionLoaded()
            },
            onFailure = { onSectionLoaded() }
        )
    }

    private fun setupDots(count: Int) {
        binding.layoutDots.removeAllViews()
        val size = (8 * resources.displayMetrics.density).toInt()
        val margin = (4 * resources.displayMetrics.density).toInt()
        for (i in 0 until count) {
            val dot = ImageView(requireContext())
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            dot.layoutParams = params
            dot.setImageResource(if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive)
            binding.layoutDots.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        for (i in 0 until binding.layoutDots.childCount) {
            val dot = binding.layoutDots.getChildAt(i) as ImageView
            dot.setImageResource(if (i == selected) R.drawable.dot_active else R.drawable.dot_inactive)
        }
    }

    private fun setupBestMatchList() {
        UserRepository.getUserProfile(
            onSuccess = { profile ->
                if (profile.jobMatches.isEmpty()) {
                    binding.tvSectionBestMatch.visibility = View.GONE
                    binding.rvBestMatch.visibility        = View.GONE
                    onSectionLoaded()
                    return@getUserProfile
                }

                UserRepository.getBestMatchJobs(
                    jobMatches = profile.jobMatches,
                    onSuccess = { jobsWithMatch ->
                        val todayCalendar = java.util.Calendar.getInstance()
                        val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                            add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }

                        val items = jobsWithMatch.map { (job, matchPercent) ->
                            val jobCalendar = java.util.Calendar.getInstance().apply {
                                timeInMillis = job.date
                            }
                            val dateLabel = when {
                                jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                                jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                                else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(job.date))
                            }
                            JobItem(
                                title = job.title,
                                company = job.company,
                                price = "₪${job.salary}",
                                rating = "",
                                distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                                date = dateLabel,
                                matchPercent = "$matchPercent%",
                                isUrgent = job.isUrgent,
                                category = job.category,
                                id = job.id
                            )
                        }

                        binding.tvSectionBestMatch.visibility = View.VISIBLE
                        binding.rvBestMatch.visibility        = View.VISIBLE
                        binding.rvBestMatch.layoutManager = LinearLayoutManager(
                            requireContext(), LinearLayoutManager.HORIZONTAL, false
                        )
                        binding.rvBestMatch.adapter = JobAdapter(items) { job -> openJobDetails(job) }
                        onSectionLoaded()
                    },
                    onFailure = { onSectionLoaded() }
                )
            },
            onFailure = { onSectionLoaded() }
        )
    }

    private fun setupUrgentList() {
        UserRepository.getUrgentJobs(
            onSuccess = { jobs ->
                if (jobs.isEmpty()) {
                    binding.tvSectionUrgent.visibility = View.GONE
                    binding.rvUrgent.visibility        = View.GONE
                    onSectionLoaded()
                    return@getUrgentJobs
                }

                val todayCalendar = java.util.Calendar.getInstance()
                val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }

                val items = jobs.map { job ->
                    val jobCalendar = java.util.Calendar.getInstance().apply {
                        timeInMillis = job.date
                    }
                    val dateLabel = when {
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                        else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(job.date))
                    }
                    JobItem(
                        title = job.title,
                        company = job.company,
                        price = "₪${job.salary}",
                        rating = "",
                        distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                        date = dateLabel,
                        matchPercent = null,
                        isUrgent = true,
                        category = job.category,
                        id = job.id
                    )
                }

                binding.tvSectionUrgent.visibility = View.VISIBLE
                binding.rvUrgent.visibility        = View.VISIBLE
                binding.rvUrgent.layoutManager = LinearLayoutManager(
                    requireContext(), LinearLayoutManager.HORIZONTAL, false
                )
                binding.rvUrgent.adapter = JobAdapter(items) { job -> openJobDetails(job) }
                onSectionLoaded()
            },
            onFailure = { onSectionLoaded() }
        )
    }

    private fun openJobDetails(job: JobItem) {
        val args = bundleOf("jobId" to job.id)
        findNavController().navigate(R.id.action_homeFragment_to_jobDetailsFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}