package com.example.clickjob_finalproject.ui.notifications

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.clickjob_finalproject.R
import com.google.android.material.button.MaterialButton

class EmployerRatingDialog : DialogFragment() {

    private var workerName: String = ""
    private var workerImageUrl: String = ""
    private var shiftDetails: String = ""
    private var selectedRating = 0
    private var onRatingSubmit: ((Double) -> Unit)? = null

    companion object {
        fun newInstance(
            workerName: String,
            workerImageUrl: String,
            shiftDetails: String,
            onRatingSubmit: ((Double) -> Unit)? = null
        ): EmployerRatingDialog {
            return EmployerRatingDialog().apply {
                this.onRatingSubmit = onRatingSubmit
                arguments = Bundle().apply {
                    putString("workerName", workerName)
                    putString("workerImageUrl", workerImageUrl)
                    putString("shiftDetails", shiftDetails)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workerName = arguments?.getString("workerName") ?: ""
        workerImageUrl = arguments?.getString("workerImageUrl") ?: ""
        shiftDetails = arguments?.getString("shiftDetails") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_employer_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setDimAmount(0.7f)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes = attributes.apply {
                    blurBehindRadius = 20
                }
            }
        }

        val tvQuestion = view.findViewById<TextView>(R.id.tvRatingQuestion)
        val tvShiftDetails = view.findViewById<TextView>(R.id.tvShiftDetails)
        val imgWorker = view.findViewById<ImageView>(R.id.imgWorker)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val btnSubmit = view.findViewById<MaterialButton>(R.id.btnSubmitRating)

        val stars = listOf(
            view.findViewById<ImageView>(R.id.star1),
            view.findViewById<ImageView>(R.id.star2),
            view.findViewById<ImageView>(R.id.star3),
            view.findViewById<ImageView>(R.id.star4),
            view.findViewById<ImageView>(R.id.star5)
        )

        tvQuestion.text = "כמה אתה מרוצה מחוויית העבודה של $workerName?"
        tvShiftDetails.text = shiftDetails

        if (workerImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(workerImageUrl)
                .circleCrop()
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(imgWorker)
        } else {
            imgWorker.setImageResource(R.drawable.user)
        }

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStars(stars, selectedRating)
            }
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        btnSubmit.setOnClickListener {
            if (selectedRating > 0) {
                onRatingSubmit?.invoke(selectedRating.toDouble())
                dismiss()
            }
        }
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.ic_star_filled_employer)
                star.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.employer_primary)
                )
            } else {
                star.setImageResource(R.drawable.ic_star_outline)
                star.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.text_gray)
                )
            }
        }
    }
}