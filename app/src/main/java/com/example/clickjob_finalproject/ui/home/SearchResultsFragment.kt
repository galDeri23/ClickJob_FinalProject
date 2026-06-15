package com.example.clickjob_finalproject.ui.home

import android.os.Bundle
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
        ResultItem("שם משרה", "₪50", "נופי הכפר 93, כפר מנחם", "יום שני, 28.09", "4.7", "2.2 ק״מ", "אבטחה וביטחון"),
        ResultItem("שם משרה", "₪50", "נופי הכפר 93, כפר מנחם", "יום שני, 28.09", "4.7", "2.2 ק״מ", "משלוחים ותחבורה"),
        ResultItem("שם משרה", "₪50", "נופי הכפר 93, כפר מנחם", "יום שני, 28.09", "4.7", "2.2 ק״מ", "חינוך והוראה"),
        ResultItem("שם משרה", "₪50", "נופי הכפר 93, כפר מנחם", "יום שני, 28.09", "4.7", "2.2 ק״מ", "בעלי חיים"),
        ResultItem("שם משרה", "₪50", "נופי הכפר 93, כפר מנחם", "יום שני, 28.09", "4.7", "2.2 ק״מ", "הפקה ואירועים")
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
                tabs.forEach { setTabUnselected(it) }
                setTabSelected(tab)
                // TODO: filter list by tab
                adapter.updateItems(allItems)
            }
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