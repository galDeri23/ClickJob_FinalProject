package com.example.clickjob_finalproject.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R
import com.google.android.material.textfield.TextInputEditText

class RegisterStep1Fragment : Fragment() {

    private val cities = listOf(
        "תל אביב", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה",
        "אשדוד", "נתניה", "באר שבע", "בני ברק", "חולון",
        "רמת גן", "אשקלון", "רחובות", "בת ים", "הרצליה",
        "כפר סבא", "מודיעין", "רעננה", "לוד", "רמלה",
        "נצרת", "עכו", "אילת", "טבריה", "חדרה",
        "גבעתיים", "קריית גת", "נהריה", "ראש העין", "יבנה"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = (requireActivity() as RegisterActivity).registerViewModel

        val seekBar = view.findViewById<SeekBar>(R.id.seekBarRadius)
        val tvRadiusValue = view.findViewById<TextView>(R.id.tvRadiusValue)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)
        val etAddress = view.findViewById<TextInputEditText>(R.id.etAddress)
        val spinnerCity = view.findViewById<Spinner>(R.id.spinnerCity)

        // Save to ViewModel as user types
        etName.doAfterTextChanged { viewModel.name = it.toString().trim() }
        etPhone.doAfterTextChanged { viewModel.phone = it.toString().trim() }
        etAddress.doAfterTextChanged { viewModel.address = it.toString().trim() }

        // City spinner setup
        val cityAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = cityAdapter

        // Restore previous selection if user navigated back, otherwise default to first city
        val savedIndex = cities.indexOf(viewModel.city)
        spinnerCity.setSelection(if (savedIndex >= 0) savedIndex else 0)
        viewModel.city = cities[spinnerCity.selectedItemPosition]

        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                viewModel.city = cities[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvRadiusValue.text = "$progress ק״מ"
                viewModel.searchRadius = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}