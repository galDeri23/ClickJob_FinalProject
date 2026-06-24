package com.example.clickjob_finalproject.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R
import com.google.android.material.textfield.TextInputEditText

class RegisterStep1Fragment : Fragment() {

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
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etAddress = view.findViewById<TextInputEditText>(R.id.etAddress)

        // Save to ViewModel as user types
        etName.doAfterTextChanged { viewModel.name = it.toString().trim() }
        etPhone.doAfterTextChanged { viewModel.phone = it.toString().trim() }
        etEmail.doAfterTextChanged { viewModel.email = it.toString().trim() }
        etAddress.doAfterTextChanged { viewModel.address = it.toString().trim() }

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