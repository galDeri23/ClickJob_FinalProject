package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R

data class ResultItem(
    val title: String,
    val price: String,
    val address: String,
    val day: String,
    val rating: String,
    val distance: String,
    val category: String = "מסעדות"
)

class SearchResultsAdapter(
    private var items: List<ResultItem>
) : RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // tvCategory is the ShapeableImageView in item_result_card.xml
        val imgCategory: ImageView  = itemView.findViewById(R.id.tvCategory)
        val tvPrice: TextView       = itemView.findViewById(R.id.tvPrice)
        val tvPerHour: TextView     = itemView.findViewById(R.id.tvPerHour)
        val tvJobTitle: TextView    = itemView.findViewById(R.id.tvJobTitle)
        val tvAddress: TextView     = itemView.findViewById(R.id.tvAddress)
        val tvDay: TextView         = itemView.findViewById(R.id.tvDay)
        val tvRating: TextView      = itemView.findViewById(R.id.tvRating)
        val tvDistance: TextView    = itemView.findViewById(R.id.tvDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result_card, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = items[position]

        holder.tvPrice.text    = item.price
        holder.tvPerHour.text  = "לשעה"
        holder.tvJobTitle.text = item.title
        holder.tvAddress.text  = item.address
        holder.tvDay.text      = item.day
        holder.tvRating.text   = item.rating
        holder.tvDistance.text = item.distance
        holder.imgCategory.setImageResource(getCategoryCircleImage(item.category))
    }

    private fun getCategoryCircleImage(category: String): Int {
        return when (category) {
            "אבטחה וביטחון"          -> R.drawable.img_cat_circle_security
            "משלוחים ותחבורה"        -> R.drawable.img_cat_circle_delivery
            "בניין וייצור"            -> R.drawable.img_cat_circle_construction
            "חינוך והוראה"            -> R.drawable.img_cat_circle_education
            "בעלי חיים"              -> R.drawable.img_cat_circle_pets
            "אפסנאות ולוגיסטיקה"     -> R.drawable.img_cat_circle_logistics
            "מסעדות"                 -> R.drawable.img_cat_circle_hospitality
            "אחזקה"                  -> R.drawable.img_cat_circle_maintenance
            "רפואה ובריאות"          -> R.drawable.img_cat_circle_health
            "הפקה ואירועים"          -> R.drawable.img_cat_circle_events
            "טכנולוגיה"              -> R.drawable.img_cat_circle_tech
            "שירות לקוחות"           -> R.drawable.img_cat_circle_service
            "מכירות ואופנה"          -> R.drawable.img_cat_circle_sales
            "עיצוב וקריאייטיב"       -> R.drawable.img_cat_circle_creative
            else                     -> R.drawable.img_cat_circle_service
        }
    }

    fun updateItems(newItems: List<ResultItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}