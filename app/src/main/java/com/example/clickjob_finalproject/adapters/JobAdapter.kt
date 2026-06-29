package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R
import com.google.android.material.imageview.ShapeableImageView

data class JobItem(
    val title: String,
    val company: String,
    val price: String,
    val rating: String,
    val distance: String,
    val date: String,
    val matchPercent: String? = null,
    val isUrgent: Boolean = false,
    val category: String = "משלוחים ותחבורה",
    val id: String = ""
)

class JobAdapter(
    private val items: List<JobItem>,
    private val onItemClick: (JobItem) -> Unit = {}
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobTitle: TextView       = itemView.findViewById(R.id.tvJobTitle)
        val tvCompany: TextView        = itemView.findViewById(R.id.tvCompanyName)
        val tvPrice: TextView          = itemView.findViewById(R.id.tvPrice)
        val tvPerHour: TextView        = itemView.findViewById(R.id.tvPerHour)
        val tvCategory: TextView       = itemView.findViewById(R.id.tvCategory)
        val tvRating: TextView         = itemView.findViewById(R.id.tvRating)
        val tvDistance: TextView       = itemView.findViewById(R.id.tvDistance)
        val tvDay: TextView            = itemView.findViewById(R.id.tvDay)
        val tvMatchPercent: TextView   = itemView.findViewById(R.id.tvMatchPercent)
        val progressMatch: ProgressBar = itemView.findViewById(R.id.progressMatch)
        val layoutMatch: FrameLayout   = itemView.findViewById(R.id.layoutMatch)
        val layoutRating: LinearLayout = itemView.findViewById(R.id.layoutRating)
        val imgJob: ImageView          = itemView.findViewById(R.id.imgJob)
        val imgCompanyAvatar: ShapeableImageView = itemView.findViewById(R.id.imgCompanyAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_card, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val item = items[position]

        holder.tvJobTitle.text = item.title
        holder.tvCompany.text  = item.company
        holder.tvPrice.text    = item.price
        holder.tvPerHour.text  = "לשעה"
        holder.tvCategory.text = item.category
        holder.tvDistance.text = item.distance
        holder.tvDay.text      = item.date
        holder.imgJob.setImageResource(getCategoryImage(item.category))

        holder.imgCompanyAvatar.setImageResource(R.drawable.img_cat_circle_creative)

        // Hide rating if empty
        if (item.rating.isEmpty()) {
            holder.layoutRating.visibility = View.INVISIBLE
        } else {
            holder.layoutRating.visibility = View.VISIBLE
            holder.tvRating.text = item.rating
        }

        // Show match percent if available
        if (item.matchPercent != null) {
            holder.layoutMatch.visibility = View.VISIBLE
            holder.tvMatchPercent.text = item.matchPercent
            val progress = item.matchPercent.replace("%", "").toIntOrNull() ?: 0
            holder.progressMatch.progress = progress
        } else {
            holder.layoutMatch.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    private fun getCategoryImage(category: String): Int {
        return when (category) {
            "אבטחה וביטחון"            -> R.drawable.img_cat_security
            "משלוחים ותחבורה"           -> R.drawable.img_cat_delivery
            "בניין, תעשייה וייצור"      -> R.drawable.img_cat_construction
            "חינוך והוראה"              -> R.drawable.img_cat_education
            "בעלי חיים"                -> R.drawable.img_cat_pets
            "אפסנאות ולוגיסטיקה"        -> R.drawable.img_cat_transportation
            "מסעדנות"                  -> R.drawable.img_cat_hospitality
            "אחזקה"                    -> R.drawable.img_cat_logistics
            "בריאות ורווחה"            -> R.drawable.img_cat_health
            "הפקה ואירועים"            -> R.drawable.img_cat_events
            "טכנולוגיה ותוכנה"          -> R.drawable.img_cat_tech
            "שירות לקוחות ותמיכה"      -> R.drawable.img_cat_service
            "מכירות ואופנה"            -> R.drawable.img_cat_sales
            "קריאייטיב, עיצוב ומדיה"   -> R.drawable.img_cat_creative
            else                       -> R.drawable.img_cat_service
        }
    }
}