package com.example.clickjob_finalproject.ui.myjobs

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.EmployerJobsAdapter
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QrCodeDialog : DialogFragment() {

    private var jobTitle: String = ""
    private var jobCompany: String = ""
    private var category: String = ""

    companion object {
        fun newInstance(
            jobTitle: String,
            jobCompany: String,
            category: String
        ): QrCodeDialog {
            return QrCodeDialog().apply {
                arguments = Bundle().apply {
                    putString("jobTitle", jobTitle)
                    putString("jobCompany", jobCompany)
                    putString("category", category)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jobTitle = arguments?.getString("jobTitle") ?: ""
        jobCompany = arguments?.getString("jobCompany") ?: ""
        category = arguments?.getString("category") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_qr_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show dialog in center of screen
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(android.view.Gravity.CENTER)
            setDimAmount(0.7f)
        }

        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvJobName = view.findViewById<TextView>(R.id.tvJobName)
        val imgCategory = view.findViewById<ImageView>(R.id.imgCategory)
        val ivQrCode = view.findViewById<ImageView>(R.id.ivQrCode)

        // Set current time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvTime.text = "שעה: ${timeFormat.format(Date())}"

        // Set job details
        tvJobName.text = jobTitle
        imgCategory.setImageResource(EmployerJobsAdapter.getCategoryImage(category))

        // Generate QR code
        val qrContent = "job:$jobTitle|company:$jobCompany|time:${timeFormat.format(Date())}"
        generateQrCode(qrContent)?.let { bitmap ->
            ivQrCode.setImageBitmap(bitmap)
        }

        btnClose.setOnClickListener { dismiss() }
    }

    private fun generateQrCode(content: String): Bitmap? {
        return try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 600, 600)
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            null
        }
    }
}