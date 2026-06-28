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
import com.example.clickjob_finalproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        setupSearchBar()
        setupJobPosting()
        setupUpcomingShifts()
        setupBestMatchList()
        setupUrgentList()
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

    private fun setupUpcomingShifts() {
        // TODO: replace with the real list of upcoming shifts (from ViewModel/repository).
        val items = emptyList<ShiftItem>()

        // No open shifts -> hide section title, carousel and dots, show the prompt card instead
        if (items.isEmpty()) {
            binding.tvSectionUpcoming.visibility = View.GONE
            binding.vpUpcoming.visibility = View.GONE
            binding.layoutDots.visibility = View.GONE
            binding.cardNoUpcomingShifts.visibility = View.VISIBLE
            return
        }

        // Design only supports up to 3 cards/dots in this carousel
        val shiftsToShow = items.take(3)

        binding.cardNoUpcomingShifts.visibility = View.GONE
        binding.tvSectionUpcoming.visibility = View.VISIBLE
        binding.vpUpcoming.visibility = View.VISIBLE
        binding.layoutDots.visibility = View.VISIBLE

        binding.vpUpcoming.adapter = ShiftAdapter(shiftsToShow)

        // Build the dots manually - one per shift, matching shiftsToShow.size (max 3)
        setupDots(shiftsToShow.size)
        binding.vpUpcoming.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateDots(position)
                }
            }
        )
    }

    // Create one dot ImageView per shift
    private fun setupDots(count: Int) {
        binding.layoutDots.removeAllViews()
        val size = (8 * resources.displayMetrics.density).toInt()
        val margin = (4 * resources.displayMetrics.density).toInt()
        for (i in 0 until count) {
            val dot = ImageView(requireContext())
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            dot.layoutParams = params
            dot.setImageResource(
                if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive
            )
            binding.layoutDots.addView(dot)
        }
    }

    // Highlight the dot for the current page
    private fun updateDots(selected: Int) {
        for (i in 0 until binding.layoutDots.childCount) {
            val dot = binding.layoutDots.getChildAt(i) as ImageView
            dot.setImageResource(
                if (i == selected) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }

    private fun setupBestMatchList() {
        val items = listOf(
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "מחר", "90%",false ,"אחזקה", id = "best_1"),
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "מחר", null, false ,"בעלי חיים",id = "best_2"),
            JobItem("שם משרה", "EventPro הפקות", "₪45", "4.2", "1.5 ק״מ", "היום", "87%",false ,"אבטחה וביטחון", id = "best_3")
        )

        binding.rvBestMatch.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvBestMatch.adapter = JobAdapter(items) { job ->
            openJobDetails(job)
        }
    }

    private fun setupUrgentList() {
        val items = listOf(
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "היום", null, id = "urgent_1"),
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "מחר", null, id = "urgent_2"),
            JobItem("שם משרה", "EventPro הפקות", "₪55", "4.5", "0.8 ק״מ", "היום", null, id = "urgent_3")
        )

        binding.rvUrgent.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvUrgent.adapter = JobAdapter(items) { job ->
            openJobDetails(job)
        }
    }

    // Navigates to the job details screen, passing the clicked job's id as an argument.
    // Requires action_homeFragment_to_jobDetailsFragment to exist in the nav graph.
    private fun openJobDetails(job: JobItem) {
        val args = bundleOf("jobId" to job.id)
        findNavController().navigate(R.id.action_homeFragment_to_jobDetailsFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}