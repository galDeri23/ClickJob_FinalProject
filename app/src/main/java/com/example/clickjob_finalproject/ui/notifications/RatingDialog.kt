package com.example.clickjob_finalproject.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.clickjob_finalproject.R
import com.google.android.material.button.MaterialButton

class RatingDialog : DialogFragment() {

    private var businessName: String = ""
    private var selectedRating = 0
    private var onRatingSubmit: ((Double) -> Unit)? = null

    companion object {
        fun newInstance(
            businessName: String,
            onRatingSubmit: ((Double) -> Unit)? = null
        ): RatingDialog {
            return RatingDialog().apply {
                this.onRatingSubmit = onRatingSubmit
                arguments = Bundle().apply {
                    putString("businessName", businessName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        businessName = arguments?.getString("businessName") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(android.view.Gravity.CENTER)
            setDimAmount(0.7f)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes = attributes.apply { blurBehindRadius = 20 }
            }
        }

        val tvQuestion = view.findViewById<TextView>(R.id.tvRatingQuestion)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val btnRate = view.findViewById<MaterialButton>(R.id.btnSubmitRating)

        val stars = listOf(
            view.findViewById<ImageView>(R.id.star1),
            view.findViewById<ImageView>(R.id.star2),
            view.findViewById<ImageView>(R.id.star3),
            view.findViewById<ImageView>(R.id.star4),
            view.findViewById<ImageView>(R.id.star5)
        )

        tvQuestion.text = "כמה אתה מרוצה מחוויית העבודה ב\"$businessName\"?"

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStars(stars, selectedRating)
            }
        }

        btnClose.setOnClickListener { dismiss() }

        btnRate.setOnClickListener {
            if (selectedRating > 0) {
                onRatingSubmit?.invoke(selectedRating.toDouble())
                dismiss()
            }
        }
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.ic_star_filled)
                star.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.brand_pink)
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