package com.example.clickjob_finalproject.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class RegisterStep4Fragment : Fragment() {

    private var cvUrl: String = ""
    private var cvFileName: String = ""
    private lateinit var tvFileName: TextView

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> uploadCvToStorage(uri) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvFileName = view.findViewById(R.id.tvFileName)
        view.findViewById<LinearLayout>(R.id.llAddCV).setOnClickListener {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
        }
        filePickerLauncher.launch(intent)
    }

    private fun uploadCvToStorage(uri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileName = getFileName(uri)

        tvFileName.text = fileName
        tvFileName.visibility = View.VISIBLE

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("cvs/$userId/$fileName")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    cvUrl = downloadUri.toString()
                    cvFileName = fileName
                    Toast.makeText(requireContext(), "קורות החיים הועלו בהצלחה ✓", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "שגיאה בהעלאה, נסי שוב", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFileName(uri: Uri): String {
        return requireContext().contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex("_display_name")
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "cv_file"
    }

    fun getCvUrl(): String = cvUrl
    fun getCvName(): String = cvFileName
}