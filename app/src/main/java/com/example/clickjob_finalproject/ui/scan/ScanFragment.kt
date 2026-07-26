package com.example.clickjob_finalproject.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private var isScanned = false // Prevent multiple scans

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else Toast.makeText(requireContext(), "נדרשת הרשאת מצלמה", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Image analysis for QR scanning
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val executor = Executors.newSingleThreadExecutor()
            val scanner = BarcodeScanning.getClient()

            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                if (isScanned) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                    val rawValue = barcode.rawValue ?: continue
                                    if (rawValue.startsWith("clickjob://scan?jobId=")) {
                                        val jobId = rawValue.removePrefix("clickjob://scan?jobId=")
                                        if (jobId.isNotEmpty()) {
                                            isScanned = true
                                            handleQrScanned(jobId)
                                        }
                                    }
                                }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "שגיאה בפתיחת המצלמה", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Handles successful QR scan - records worker arrival
    private fun handleQrScanned(jobId: String) {
        activity?.runOnUiThread {
            UserRepository.recordWorkerArrival(
                jobId = jobId,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        "✓ סריקה לתחילת עבודה התבצעה בהצלחה!",
                        Toast.LENGTH_LONG
                    ).show()
                    // Reset after 3 seconds to allow another scan
                    previewView.postDelayed({ isScanned = false }, 3000)
                },
                onFailure = {
                    Toast.makeText(
                        requireContext(),
                        "שגיאה בסריקה, נסה שוב",
                        Toast.LENGTH_SHORT
                    ).show()
                    isScanned = false
                }
            )
        }
    }
}