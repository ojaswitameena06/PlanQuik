package com.example.eventplanner.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.CalendarDay
import com.example.eventplanner.R

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val selectedDay: Int,
    private val selectedMonth: Int,
    private val selectedYear: Int,
    private val eventDays: Set<Int>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.dayContainer)
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val eventDot: View = itemView.findViewById(R.id.eventDot)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DayViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)

        return DayViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DayViewHolder,
        position: Int
    ) {

        val day = days[position]

        if (day.day == 0) {
            holder.dayText.text = ""
            holder.eventDot.visibility = View.GONE
            holder.container.setOnClickListener(null)
            return
        }

        holder.dayText.text = day.day.toString()

        if (!day.isCurrentMonth) {
            holder.dayText.setTextColor(Color.LTGRAY)
            holder.eventDot.visibility = View.GONE
        } else {
            holder.dayText.setTextColor(Color.rgb(32, 33, 36))

            if (eventDays.contains(day.day)) {
                holder.eventDot.visibility = View.VISIBLE
            } else {
                holder.eventDot.visibility = View.GONE
            }
        }

        if (
            day.day == selectedDay &&
            day.month == selectedMonth &&
            day.year == selectedYear
        ) {
            holder.dayText.setBackgroundColor(
                Color.rgb(210, 247, 241)
            )

            holder.dayText.setTextColor(
                Color.rgb(20, 20, 20)
            )
        } else {
            holder.dayText.background = null
        }

        holder.container.setOnClickListener {
            onDayClick(day)
        }
    }

    override fun getItemCount(): Int {
        return days.size
    }
}