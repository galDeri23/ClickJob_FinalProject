package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R

data class ShiftItem(
    val title: String,
    val company: String,
    val time: String,
    val date: String,
    val category: String = "מסעדנות"
)

class ShiftAdapter(
    private val items: List<ShiftItem>
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
        holder.tvAddress.text = "כתובת"
        holder.imgJob.setImageResource(getCategoryCircleImage(item.category))
    }

    override fun getItemCount() = items.size

    private fun getCategoryCircleImage(category: String): Int {
        return when (category) {
            "אבטחה וביטחון" -> R.drawable.img_cat_circle_security
            "משלוחים ותחבורה" -> R.drawable.img_cat_circle_delivery
            "בניין, תעשייה וייצור" -> R.drawable.img_cat_circle_construction
            "חינוך והוראה" -> R.drawable.img_cat_circle_education
            "בעלי חיים" -> R.drawable.img_cat_circle_pets
            "אפסנאות ולוגיסטיקה" -> R.drawable.img_cat_circle_logistics
            "מסעדנות" -> R.drawable.img_cat_circle_hospitality
            "אחזקה" -> R.drawable.img_cat_circle_maintenance
            "בריאות ורווחה" -> R.drawable.img_cat_circle_health
            "הפקה ואירועים" -> R.drawable.img_cat_circle_events
            "טכנולוגיה ותוכנה" -> R.drawable.img_cat_circle_tech
            "שירות לקוחות ותמיכה" -> R.drawable.img_cat_circle_service
            "מכירות ואופנה" -> R.drawable.img_cat_circle_sales
            "קריאייטיב, עיצוב ומדיה" -> R.drawable.img_cat_circle_creative
            else -> R.drawable.img_cat_circle_service
        }
    }
}