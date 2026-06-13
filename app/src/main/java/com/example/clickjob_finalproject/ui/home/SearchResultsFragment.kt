package com.example.clickjob_finalproject.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.ResultItem
import com.example.clickjob_finalproject.adapters.SearchResultsAdapter
import com.example.clickjob_finalproject.databinding.FragmentSearchResultsBinding

class SearchResultsFragment : Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchResultsAdapter

    // Sample data — replace with real data later
    private val allItems = listOf(
        ResultItem("שם משרה", "שם חברה", "₪50", "4.7", "2.2 ק״מ", "מחר", "בעלי חיים"),
        ResultItem("שם משרה", "שם חברה", "₪50", "4.7", "2.2 ק״מ", "מחר", "מסעדנות"),
        ResultItem("שם משרה", "שם חברה", "₪50", "4.7", "2.2 ק״מ", "מחר", "אחזקה"),
        ResultItem("שם משרה", "שם חברה", "₪50", "4.7", "2.2 ק״מ", "מחר", "בעלי חיים"),
        ResultItem("שם משרה", "שם חברה", "₪50", "4.7", "2.2 ק״מ", "מחר", "בעלי חיים")
    )

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

        setupBackButton()
        setupResultsList()
        setupTabs()
    }

    private fun setupBackButton() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupResultsList() {
        adapter = SearchResultsAdapter(allItems)
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter
    }

    private fun setupTabs() {
        val tabs = listOf(binding.tabAll, binding.tabNear, binding.tabHighSalary, binding.tabUrgent)

        tabs.forEach { tab ->
            tab.setOnClickListener {
                // Update tab appearance
                tabs.forEach { setTabUnselected(it) }
                setTabSelected(tab)

                // Filter list by tab — replace with real filter logic later
                adapter.updateItems(allItems)
            }
        }
    }

    private fun setTabSelected(tab: TextView) {
        tab.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_selected)
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setTabUnselected(tab: TextView) {
        tab.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_unselected)
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}