package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R

data class ShiftItem(
    val id: String = "",
    val title: String,
    val company: String,
    val time: String,
    val date: String,
    val address: String,
    val category: String = "מסעדנות"
)

class ShiftAdapter(
    private val items: List<ShiftItem>,
    private val onItemClick: (ShiftItem) -> Unit = {}
) : RecyclerView.Adapter<ShiftAdapter.ShiftViewHolder>() {

    inner class ShiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView   = itemView.findViewById(R.id.tvJobTitle)
        val tvCompany: TextView = itemView.findViewById(R.id.tvCompanyName)
        val tvTime: TextView    = itemView.findViewById(R.id.tvHours)
        val tvDate: TextView    = itemView.findViewById(R.id.tvDate)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val imgJob: ImageView   = itemView.findViewById(R.id.imgJob)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_shift, parent, false)
        return ShiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text   = item.title
        holder.tvCompany.text = item.company
        holder.tvTime.text    = item.time
        holder.tvDate.text    = item.date
        holder.tvAddress.text = item.address
        holder.imgJob.setImageResource(getCategoryCircleImage(item.category))
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    private fun getCategoryCircleImage(category: String): Int {
        return when (category) {
            "אבטחה וביטחון"            -> R.drawable.img_cat_circle_security_white
            "משלוחים ותחבורה"           -> R.drawable.img_cat_circle_delivery_white
            "בניין, תעשייה וייצור"      -> R.drawable.img_cat_circle_construction_white
            "חינוך והוראה"              -> R.drawable.img_cat_circle_education_white
            "בעלי חיים"                -> R.drawable.img_cat_circle_pets_white
            "אפסנאות ולוגיסטיקה"        -> R.drawable.img_cat_circle_transportation_white
            "מסעדנות"                  -> R.drawable.img_cat_circle_hospitality_white
            "אחזקה"                    -> R.drawable.img_cat_circle_logistics_white
            "בריאות ורווחה"            -> R.drawable.img_cat_circle_health_white
            "הפקה ואירועים"            -> R.drawable.img_cat_circle_events_white
            "טכנולוגיה ותוכנה"          -> R.drawable.img_cat_circle_tech_white
            "שירות לקוחות ותמיכה"      -> R.drawable.img_cat_circle_service_white
            "מכירות ואופנה"            -> R.drawable.img_cat_circle_sales_white
            "קריאייטיב, עיצוב ומדיה"   -> R.drawable.img_cat_circle_creative_white
            else                       -> R.drawable.img_cat_circle_service_white
        }
    }
}