package com.example.clickjob_finalproject.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R

class RegisterStep4Fragment : Fragment() {

    private var cvUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.llAddCV).setOnClickListener {
            // TODO: open file picker and upload to Firebase Storage
            // After upload: cvUrl = downloadUrl
            Toast.makeText(requireContext(), "העלאת קורות חיים - בקרוב", Toast.LENGTH_SHORT).show()
        }
    }

    // Returns CV URL to RegisterActivity
    fun getCvUrl(): String {
        return cvUrl
    }
}