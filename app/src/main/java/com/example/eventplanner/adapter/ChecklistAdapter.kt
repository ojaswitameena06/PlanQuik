package com.example.eventplanner.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.R
import com.example.eventplanner.data.entity.ChecklistItem

class ChecklistAdapter(
    private var items: List<ChecklistItem>,
    private val onCheckChange: (ChecklistItem, Boolean) -> Unit,
    private val onDeleteClick: (ChecklistItem) -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>() {

    class ChecklistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkboxTask)
        val tvTask: TextView = itemView.findViewById(R.id.tvTaskName)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return ChecklistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        val item = items[position]
        holder.tvTask.text = item.task
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = item.isCompleted

        if (item.isCompleted) {
            holder.tvTask.paintFlags = holder.tvTask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvTask.paintFlags = holder.tvTask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckChange(item, isChecked)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ChecklistItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}