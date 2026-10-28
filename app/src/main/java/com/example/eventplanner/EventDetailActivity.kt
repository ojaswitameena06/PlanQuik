package com.example.eventplanner

import android.content.res.ColorStateList
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.adapter.ChecklistAdapter
import com.example.eventplanner.adapter.ExpenseAdapter
import com.example.eventplanner.adapter.GuestAdapter
import com.example.eventplanner.data.entity.ChecklistItem
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.data.entity.Expense
import com.example.eventplanner.data.entity.Guest
import com.example.eventplanner.notifications.ReminderScheduler
import com.example.eventplanner.ui.budget.DonutChartView
import com.example.eventplanner.ui.budget.ExpenseViewModel
import com.example.eventplanner.ui.checklist.ChecklistViewModel
import com.example.eventplanner.ui.guests.GuestViewModel
import com.example.eventplanner.ui.home.EventViewModel
import com.example.eventplanner.util.EventUi
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import com.example.eventplanner.sync.InviteSync

class EventDetailActivity : AppCompatActivity() {

    private val eventViewModel: EventViewModel by viewModels()
    private val guestViewModel: GuestViewModel by viewModels()
    private val checklistViewModel: ChecklistViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()

    // Views (looked up on first use, after setContentView)
    private val headerContainer by lazy { findViewById<View>(R.id.headerContainer) }
    private val btnBack by lazy { findViewById<ImageButton>(R.id.btnBack) }
    private val btnEdit by lazy { findViewById<ImageButton>(R.id.btnEdit) }
    private val btnDeleteEvent by lazy { findViewById<ImageButton>(R.id.btnDeleteEvent) }
    private val tvDetailCategory by lazy { findViewById<TextView>(R.id.tvDetailCategory) }
    private val tvDetailTitle by lazy { findViewById<TextView>(R.id.tvDetailTitle) }
    private val tvDetailDateTime by lazy { findViewById<TextView>(R.id.tvDetailDateTime) }
    private val tvDetailLocation by lazy { findViewById<TextView>(R.id.tvDetailLocation) }
    private val tvDetailCountdown by lazy { findViewById<TextView>(R.id.tvDetailCountdown) }
    private val tvDetailDescription by lazy { findViewById<TextView>(R.id.tvDetailDescription) }
    private val btnSetReminder by lazy { findViewById<MaterialButton>(R.id.btnSetReminder) }

    private val btnAddGuest by lazy { findViewById<MaterialButton>(R.id.btnAddGuest) }
    private val tvGuestSummary by lazy { findViewById<TextView>(R.id.tvGuestSummary) }
    private val recyclerViewGuests by lazy { findViewById<RecyclerView>(R.id.recyclerViewGuests) }
    private val tvGuestsEmpty by lazy { findViewById<TextView>(R.id.tvGuestsEmpty) }

    private val tvBudgetTotal by lazy { findViewById<TextView>(R.id.tvBudgetTotal) }
    private val tvBudgetSpent by lazy { findViewById<TextView>(R.id.tvBudgetSpent) }
    private val tvBudgetRemaining by lazy { findViewById<TextView>(R.id.tvBudgetRemaining) }
    private val tvBudgetRemainingLabel by lazy { findViewById<TextView>(R.id.tvBudgetRemainingLabel) }
    private val progressBudget by lazy { findViewById<LinearProgressIndicator>(R.id.progressBudget) }
    private val tvCostPerGuest by lazy { findViewById<TextView>(R.id.tvCostPerGuest) }
    private val chartRow by lazy { findViewById<LinearLayout>(R.id.chartRow) }
    private val donutChart by lazy { findViewById<DonutChartView>(R.id.donutChart) }
    private val legendContainer by lazy { findViewById<LinearLayout>(R.id.legendContainer) }
    private val btnAddExpense by lazy { findViewById<MaterialButton>(R.id.btnAddExpense) }
    private val recyclerViewExpenses by lazy { findViewById<RecyclerView>(R.id.recyclerViewExpenses) }
    private val tvExpensesEmpty by lazy { findViewById<TextView>(R.id.tvExpensesEmpty) }

    private val btnAddTask by lazy { findViewById<MaterialButton>(R.id.btnAddTask) }
    private val tvChecklistSummary by lazy { findViewById<TextView>(R.id.tvChecklistSummary) }
    private val progressChecklist by lazy { findViewById<LinearProgressIndicator>(R.id.progressChecklist) }
    private val recyclerViewChecklist by lazy { findViewById<RecyclerView>(R.id.recyclerViewChecklist) }
    private val tvChecklistEmpty by lazy { findViewById<TextView>(R.id.tvChecklistEmpty) }

    private lateinit var guestAdapter: GuestAdapter
    private lateinit var checklistAdapter: ChecklistAdapter
    private lateinit var expenseAdapter: ExpenseAdapter

    private var eventId: Long = -1L
    private var currentEvent: Event? = null
    private var guests: List<Guest> = emptyList()
    private var expenses: List<Expense> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnGenerateInvitation
        ).setOnClickListener {
            val intent = Intent(this, InvitationActivity::class.java)
            intent.putExtra("EVENT_ID", eventId)
            startActivity(intent)
        }

        eventId = intent.getLongExtra("EVENT_ID", -1L)
        if (eventId == -1L) {
            finish()
            return
        }

        btnBack.setOnClickListener { finish() }
        btnEdit.setOnClickListener {
            startActivity(
                Intent(this, AddEditEventActivity::class.java).putExtra("EVENT_ID", eventId)
            )
        }
        btnDeleteEvent.setOnClickListener { confirmDeleteEvent() }
        btnSetReminder.setOnClickListener { setReminder() }
        btnAddGuest.setOnClickListener { showAddGuestDialog() }
        btnAddExpense.setOnClickListener { showExpenseDialog(null) }
        btnAddTask.setOnClickListener { showAddTaskDialog() }

        setupLists()

        guestViewModel.loadGuestsForEvent(eventId)
        guestViewModel.guests.observe(this) { list ->
            guests = list
            guestAdapter.updateGuests(list)
            updateGuestSummary()
            updateBudgetUi()
        }

        checklistViewModel.loadItemsForEvent(eventId)
        checklistViewModel.items.observe(this) { items ->
            checklistAdapter.updateItems(items)
            updateChecklistUi(items)
        }

        expenseViewModel.loadExpensesForEvent(eventId)
        expenseViewModel.expenses.observe(this) { list ->
            expenses = list
            expenseAdapter.updateItems(list)
            updateBudgetUi()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload so edits made in the edit screen show up when we come back
        loadEvent()
    }

    private fun setupLists() {
        guestAdapter = GuestAdapter(
            emptyList(),
            onRsvpClick = { cycleRsvpStatus(it) },
            onInviteClick = { sendInvite(it) },
            onDeleteClick = { confirmDeleteGuest(it) }
        )
        recyclerViewGuests.layoutManager = LinearLayoutManager(this)
        recyclerViewGuests.adapter = guestAdapter

        expenseAdapter = ExpenseAdapter(
            emptyList(),
            onPaidChange = { expense, paid -> expenseViewModel.updateExpense(expense.copy(isPaid = paid)) },
            onClick = { showExpenseDialog(it) },
            onDelete = { confirmDeleteExpense(it) }
        )
        recyclerViewExpenses.layoutManager = LinearLayoutManager(this)
        recyclerViewExpenses.adapter = expenseAdapter

        checklistAdapter = ChecklistAdapter(
            emptyList(),
            onCheckChange = { item, isChecked ->
                checklistViewModel.updateItem(item.copy(isCompleted = isChecked))
            },
            onDeleteClick = { checklistViewModel.deleteItem(it) }
        )
        recyclerViewChecklist.layoutManager = LinearLayoutManager(this)
        recyclerViewChecklist.adapter = checklistAdapter
    }

    // ---------------------------------------------------------------- event

    private fun loadEvent() {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId)
            if (event == null) {
                finish()
                return@launch
            }
            currentEvent = event
            bindHeader(event)
            updateBudgetUi()
        }
    }

    private fun bindHeader(event: Event) {
        val color = EventUi.categoryColor(this, event.category)
        val dark = ColorUtils.blendARGB(color, Color.BLACK, 0.25f)

        headerContainer.backgroundTintList = ColorStateList.valueOf(dark)
        window.statusBarColor = dark
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val millis = EventUi.eventMillis(event)
        tvDetailCategory.text = event.category
        tvDetailTitle.text = event.title
        tvDetailDateTime.text = millis?.let { EventUi.format(it, "EEEE, d MMM yyyy • h:mm a") }
            ?: "${event.date} • ${event.time}"
        tvDetailLocation.text = event.location

        if (millis != null) {
            tvDetailCountdown.visibility = View.VISIBLE
            tvDetailCountdown.text =
                if (millis >= System.currentTimeMillis()) EventUi.countdown(millis) else "This event has passed"
        } else {
            tvDetailCountdown.visibility = View.GONE
        }

        if (event.description.isBlank()) {
            tvDetailDescription.visibility = View.GONE
        } else {
            tvDetailDescription.visibility = View.VISIBLE
            tvDetailDescription.text = event.description
        }
    }

    private fun confirmDeleteEvent() {
        val event = currentEvent ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete event")
            .setMessage("Delete \"${event.title}\"? This can't be undone.")
            .setPositiveButton("Delete") { _, _ ->
                eventViewModel.deleteEvent(event)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ------------------------------------------------------------- reminders

    private fun setReminder() {
        val options = arrayOf("10 minutes before", "30 minutes before", "1 hour before", "1 day before")
        val millisBefore = longArrayOf(
            10 * 60 * 1000L,
            30 * 60 * 1000L,
            60 * 60 * 1000L,
            24 * 60 * 60 * 1000L
        )
        MaterialAlertDialogBuilder(this)
            .setTitle("Remind me")
            .setItems(options) { _, which ->
                scheduleReminderWithOffset(millisBefore[which], options[which])
            }
            .show()
    }

    private fun scheduleReminderWithOffset(offsetMillis: Long, label: String) {
        val event = currentEvent ?: return
        val eventMillis = EventUi.eventMillis(event)
        if (eventMillis == null) {
            Toast.makeText(this, "Could not set reminder", Toast.LENGTH_SHORT).show()
            return
        }
        val reminderTime = eventMillis - offsetMillis
        if (reminderTime <= System.currentTimeMillis()) {
            Toast.makeText(this, "That reminder time has already passed", Toast.LENGTH_SHORT).show()
            return
        }
        ReminderScheduler.scheduleReminder(this, event.id, event.title, event.location, reminderTime)
        Toast.makeText(this, "Reminder set: $label", Toast.LENGTH_SHORT).show()
    }

    // ---------------------------------------------------------------- guests

    private fun updateGuestSummary() {
        val accepted = guests.count { it.rsvpStatus == "Accepted" }
        val declined = guests.count { it.rsvpStatus == "Declined" }
        val pending = guests.size - accepted - declined

        tvGuestSummary.text =
            if (guests.isEmpty()) "" else "$accepted accepted · $pending pending · $declined declined"
        tvGuestSummary.visibility = if (guests.isEmpty()) View.GONE else View.VISIBLE
        tvGuestsEmpty.visibility = if (guests.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun cycleRsvpStatus(guest: Guest) {
        val nextStatus = when (guest.rsvpStatus) {
            "Pending" -> "Accepted"
            "Accepted" -> "Declined"
            else -> "Pending"
        }
        guestViewModel.updateGuest(guest.copy(rsvpStatus = nextStatus))
        // Keep the cloud copy in step so a later sync doesn't undo this change
        if (guest.inviteToken.isNotBlank()) InviteSync.setStatus(guest.inviteToken, nextStatus)
    }

    private fun sendInvite(guest: Guest) {
        val event = currentEvent ?: return

        lifecycleScope.launch {
            // Give the guest a secret RSVP token the first time
            val isNew = guest.inviteToken.isBlank()
            val ready = if (isNew) {
                val withToken = guest.copy(inviteToken = InviteSync.newToken())
                guestViewModel.updateGuest(withToken).join()
                withToken
            } else {
                guest
            }

            val hostName = EventUi.userName()
            InviteSync.publishInvite(event, ready, hostName, isNew)

            val millis = EventUi.eventMillis(event)
            val whenText = millis?.let { EventUi.format(it, "EEE, d MMM yyyy 'at' h:mm a") }
                ?: "${event.date} ${event.time}"
            val link = InviteSync.inviteLink(ready.inviteToken)
            val message = "Hi ${ready.name}! You're invited to \"${event.title}\" on $whenText " +
                    "at ${event.location}.\n\nWill you be there? Reply here:\n$link\n\n- $hostName"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            try {
                startActivity(Intent.createChooser(intent, "Send invite via"))
            } catch (e: Exception) {
                Toast.makeText(this@EventDetailActivity, "No app available to send invite", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteGuest(guest: Guest) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Remove guest")
            .setMessage("Remove ${guest.name} from this event?")
            .setPositiveButton("Remove") { _, _ ->
                if (guest.inviteToken.isNotBlank()) InviteSync.deleteInvite(guest.inviteToken)
                guestViewModel.deleteGuest(guest)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddGuestDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_guest, null)
        val etName = dialogView.findViewById<EditText>(R.id.etGuestName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etGuestPhone)
        val etEmail = dialogView.findViewById<EditText>(R.id.etGuestEmail)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add guest")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    guestViewModel.insertGuest(
                        Guest(
                            eventId = eventId,
                            name = name,
                            phone = etPhone.text.toString().trim(),
                            email = etEmail.text.toString().trim()
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------------------------------------------------------- budget

    private fun updateBudgetUi() {
        val event = currentEvent ?: return
        val budget = event.budget
        val spent = expenses.sumOf { it.amount }
        val remaining = budget - spent
        val over = budget > 0 && remaining < 0

        tvBudgetTotal.text = if (budget > 0) EventUi.money(budget) else "Not set"
        tvBudgetSpent.text = EventUi.money(spent)
        tvBudgetRemainingLabel.text = if (over) "Over by" else "Remaining"
        tvBudgetRemaining.text = when {
            budget <= 0 -> "—"
            over -> EventUi.money(-remaining)
            else -> EventUi.money(remaining)
        }
        tvBudgetRemaining.setTextColor(
            if (over) ContextCompat.getColor(this, R.color.rsvp_declined)
            else MaterialColors.getColor(tvBudgetRemaining, com.google.android.material.R.attr.colorOnSurface)
        )

        // Progress bar: normal, amber from 80%, red when over budget
        if (budget > 0) {
            progressBudget.visibility = View.VISIBLE
            val fraction = spent / budget
            progressBudget.setIndicatorColor(
                when {
                    fraction > 1.0 -> ContextCompat.getColor(this, R.color.rsvp_declined)
                    fraction >= 0.8 -> ContextCompat.getColor(this, R.color.rsvp_pending)
                    else -> MaterialColors.getColor(progressBudget, com.google.android.material.R.attr.colorPrimary)
                }
            )
            progressBudget.setProgressCompat((fraction * 100).toInt().coerceIn(0, 100), true)
        } else {
            progressBudget.visibility = View.GONE
        }

        // Cost per accepted guest
        val headcount = guests
            .filter { it.rsvpStatus == "Accepted" }
            .sumOf { it.partySize.coerceAtLeast(1) }
        when {
            expenses.isEmpty() -> tvCostPerGuest.visibility = View.GONE
            headcount > 0 -> {
                tvCostPerGuest.visibility = View.VISIBLE
                tvCostPerGuest.text = "${EventUi.money(spent / headcount)} per accepted guest ($headcount going)"
            }
            else -> {
                tvCostPerGuest.visibility = View.VISIBLE
                tvCostPerGuest.text = "Cost per guest appears once guests accept"
            }
        }

        tvExpensesEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE

        // Donut chart + legend by category
        val byCategory = expenses
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        if (spent <= 0.0) {
            chartRow.visibility = View.GONE
        } else {
            chartRow.visibility = View.VISIBLE
            val slices = byCategory.map { (category, sum) ->
                sum.toFloat() to EventUi.expenseColor(this, category)
            }
            donutChart.setData(slices, EventUi.money(spent), "spent")

            legendContainer.removeAllViews()
            byCategory.entries.sortedByDescending { it.value }.forEach { (category, sum) ->
                val row = layoutInflater.inflate(R.layout.item_legend, legendContainer, false)
                row.findViewById<View>(R.id.legendDot).backgroundTintList =
                    ColorStateList.valueOf(EventUi.expenseColor(this, category))
                row.findViewById<TextView>(R.id.tvLegendName).text = category
                row.findViewById<TextView>(R.id.tvLegendAmount).text = EventUi.money(sum)
                legendContainer.addView(row)
            }
        }
    }

    private fun showExpenseDialog(existing: Expense?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val etTitle = view.findViewById<EditText>(R.id.etExpenseTitle)
        val etAmount = view.findViewById<EditText>(R.id.etExpenseAmount)
        val actvCategory = view.findViewById<AutoCompleteTextView>(R.id.actvExpenseCategory)

        actvCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, EventUi.EXPENSE_CATEGORIES)
        )
        actvCategory.setText(existing?.category ?: EventUi.EXPENSE_CATEGORIES[0], false)

        existing?.let {
            etTitle.setText(it.title)
            etAmount.setText(
                if (it.amount % 1.0 == 0.0) it.amount.toLong().toString() else it.amount.toString()
            )
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (existing == null) "Add expense" else "Edit expense")
            .setView(view)
            .setPositiveButton(if (existing == null) "Add" else "Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val amount = etAmount.text.toString().trim().toDoubleOrNull()
                val category = actvCategory.text.toString().ifBlank { "Other" }

                if (title.isEmpty() || amount == null || amount <= 0) {
                    Toast.makeText(this, "Enter a name and an amount", Toast.LENGTH_SHORT).show()
                } else if (existing == null) {
                    expenseViewModel.insertExpense(
                        Expense(eventId = eventId, title = title, category = category, amount = amount)
                    )
                } else {
                    expenseViewModel.updateExpense(
                        existing.copy(title = title, category = category, amount = amount)
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteExpense(expense: Expense) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete expense")
            .setMessage("Delete \"${expense.title}\"?")
            .setPositiveButton("Delete") { _, _ -> expenseViewModel.deleteExpense(expense) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ------------------------------------------------------------- checklist

    private fun updateChecklistUi(items: List<ChecklistItem>) {
        val done = items.count { it.isCompleted }
        if (items.isEmpty()) {
            tvChecklistSummary.visibility = View.GONE
            progressChecklist.visibility = View.GONE
            tvChecklistEmpty.visibility = View.VISIBLE
        } else {
            tvChecklistSummary.visibility = View.VISIBLE
            tvChecklistSummary.text = "$done of ${items.size} done"
            progressChecklist.visibility = View.VISIBLE
            progressChecklist.setProgressCompat(done * 100 / items.size, true)
            tvChecklistEmpty.visibility = View.GONE
        }
    }

    private fun showAddTaskDialog() {
        val container = FrameLayout(this)
        val pad = (24 * resources.displayMetrics.density).toInt()
        container.setPadding(pad, pad / 3, pad, 0)
        val input = EditText(this).apply {
            hint = "Task description"
            maxLines = 1
        }
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add task")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val task = input.text.toString().trim()
                if (task.isNotEmpty()) {
                    checklistViewModel.insertItem(ChecklistItem(eventId = eventId, task = task))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}