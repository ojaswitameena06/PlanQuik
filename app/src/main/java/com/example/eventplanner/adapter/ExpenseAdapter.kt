package com.example.eventplanner.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.R
import com.example.eventplanner.data.entity.Expense
import com.example.eventplanner.util.EventUi

class ExpenseAdapter(
    private var items: List<Expense>,
    private val onPaidChange: (Expense, Boolean) -> Unit,
    private val onClick: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkboxPaid: CheckBox = itemView.findViewById(R.id.checkboxPaid)
        val tvTitle: TextView = itemView.findViewById(R.id.tvExpenseTitle)
        val categoryDot: View = itemView.findViewById(R.id.categoryDot)
        val tvCategory: TextView = itemView.findViewById(R.id.tvExpenseCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteExpense)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = items[position]
        val color = EventUi.expenseColor(holder.itemView.context, item.category)

        holder.tvTitle.text = item.title
        holder.tvCategory.text = "${item.category} • ${if (item.isPaid) "Paid" else "Unpaid"}"
        holder.tvAmount.text = EventUi.money(item.amount)
        holder.categoryDot.backgroundTintList = ColorStateList.valueOf(color)

        holder.checkboxPaid.setOnCheckedChangeListener(null)
        holder.checkboxPaid.isChecked = item.isPaid
        holder.checkboxPaid.setOnCheckedChangeListener { _, checked -> onPaidChange(item, checked) }

        holder.itemView.setOnClickListener { onClick(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Expense>) {
        items = newItems
        notifyDataSetChanged()
    }
}