package com.example.clickjob_finalproject.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R

class RegisterStep2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step2, container, false)
    }

    // Returns list of selected available days
    fun getSelectedDays(): List<String> {
        val view = requireView()
        val selectedDays = mutableListOf<String>()

        if (view.findViewById<CheckBox>(R.id.cbSunday).isChecked) selectedDays.add("ראשון")
        if (view.findViewById<CheckBox>(R.id.cbMonday).isChecked) selectedDays.add("שני")
        if (view.findViewById<CheckBox>(R.id.cbTuesday).isChecked) selectedDays.add("שלישי")
        if (view.findViewById<CheckBox>(R.id.cbWednesday).isChecked) selectedDays.add("רביעי")
        if (view.findViewById<CheckBox>(R.id.cbThursday).isChecked) selectedDays.add("חמישי")
        if (view.findViewById<CheckBox>(R.id.cbFriday).isChecked) selectedDays.add("שישי")
        if (view.findViewById<CheckBox>(R.id.cbSaturday).isChecked) selectedDays.add("שבת")

        // If nothing selected → available all days
        return if (selectedDays.isEmpty()) {
            listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת")
        } else {
            selectedDays
        }
    }
}