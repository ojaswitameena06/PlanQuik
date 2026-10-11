package com.example.eventplanner

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.adapter.GuestAdapter
import com.example.eventplanner.data.entity.Guest
import com.example.eventplanner.ui.guests.GuestViewModel
import com.example.eventplanner.ui.home.EventViewModel
import kotlinx.coroutines.launch
import android.widget.Toast
import android.widget.Button
import com.example.eventplanner.notifications.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.ProgressBar
import com.example.eventplanner.adapter.ChecklistAdapter
import com.example.eventplanner.data.entity.ChecklistItem
import com.example.eventplanner.ui.checklist.ChecklistViewModel

class EventDetailActivity : AppCompatActivity() {

    private val eventViewModel: EventViewModel by viewModels()
    private val guestViewModel: GuestViewModel by viewModels()

    private lateinit var tvTitle: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: ImageButton
    private lateinit var btnAddGuest: ImageButton
    private lateinit var recyclerViewGuests: RecyclerView
    private lateinit var guestAdapter: GuestAdapter
    private lateinit var btnSetReminder: Button

    private var eventId: Long = -1L
    private var eventTitle: String = ""
    private var eventDateTime: String = ""
    private var eventLocation: String = ""

    private val checklistViewModel: ChecklistViewModel by viewModels()
    private lateinit var recyclerViewChecklist: RecyclerView
    private lateinit var checklistAdapter: ChecklistAdapter
    private lateinit var btnAddTask: ImageButton
    private lateinit var progressChecklist: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        tvTitle = findViewById(R.id.tvDetailTitle)
        tvDateTime = findViewById(R.id.tvDetailDateTime)
        tvLocation = findViewById(R.id.tvDetailLocation)
        tvDescription = findViewById(R.id.tvDetailDescription)
        btnBack = findViewById(R.id.btnBack)
        btnEdit = findViewById(R.id.btnEdit)
        btnAddGuest = findViewById(R.id.btnAddGuest)
        btnSetReminder = findViewById(R.id.btnSetReminder)
        btnSetReminder.setOnClickListener { setReminder() }
        recyclerViewGuests = findViewById(R.id.recyclerViewGuests)
        recyclerViewChecklist = findViewById(R.id.recyclerViewChecklist)
        btnAddTask = findViewById(R.id.btnAddTask)
        progressChecklist = findViewById(R.id.progressChecklist)

        eventId = intent.getLongExtra("EVENT_ID", -1L)

        if (eventId == -1L) {
            finish()
            return
        }

        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            val editIntent = Intent(this, AddEditEventActivity::class.java)
            editIntent.putExtra("EVENT_ID", eventId)
            startActivity(editIntent)
        }

        btnAddGuest.setOnClickListener { showAddGuestDialog() }
        btnAddTask.setOnClickListener { showAddTaskDialog() }

        guestAdapter = GuestAdapter(
            emptyList(),
            onRsvpClick = { guest -> cycleRsvpStatus(guest) },
            onInviteClick = { guest -> sendInvite(guest) },
            onDeleteClick = { guest -> confirmDeleteGuest(guest) }
        )

        recyclerViewGuests.layoutManager = LinearLayoutManager(this)
        recyclerViewGuests.adapter = guestAdapter

        checklistAdapter = ChecklistAdapter(
            emptyList(),
            onCheckChange = { item, isChecked ->
                checklistViewModel.updateItem(item.copy(isCompleted = isChecked))
            },
            onDeleteClick = { item ->
                checklistViewModel.deleteItem(item)
            }
        )

        recyclerViewChecklist.layoutManager = LinearLayoutManager(this)
        recyclerViewChecklist.adapter = checklistAdapter

        loadEventDetails()

        guestViewModel.loadGuestsForEvent(eventId)
        guestViewModel.guests.observe(this) { guests ->
            guestAdapter.updateGuests(guests)
        }

        checklistViewModel.loadItemsForEvent(eventId)
        checklistViewModel.items.observe(this) { items ->
            checklistAdapter.updateItems(items)
            updateProgress(items)
        }
    }

    private fun loadEventDetails() {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId)
            event?.let {
                eventTitle = it.title
                eventDateTime = "${it.date} • ${it.time}"
                eventLocation = it.location

                tvTitle.text = it.title
                tvDateTime.text = eventDateTime
                tvLocation.text = it.location
                tvDescription.text = it.description
            }
        }
    }

    private fun setReminder() {
        val options = arrayOf("10 minutes before", "30 minutes before", "1 hour before", "1 day before")
        val millisBefore = longArrayOf(
            10 * 60 * 1000L,
            30 * 60 * 1000L,
            60 * 60 * 1000L,
            24 * 60 * 60 * 1000L
        )

        AlertDialog.Builder(this)
            .setTitle("Remind me")
            .setItems(options) { _, which ->
                scheduleReminderWithOffset(millisBefore[which], options[which])
            }
            .show()
    }

    private fun scheduleReminderWithOffset(offsetMillis: Long, label: String) {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId)
            event?.let {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val eventDateTimeMillis = sdf.parse("${it.date} ${it.time}")?.time ?: return@let

                    val reminderTime = eventDateTimeMillis - offsetMillis

                    if (reminderTime <= System.currentTimeMillis()) {
                        Toast.makeText(this@EventDetailActivity, "That reminder time has already passed", Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    ReminderScheduler.scheduleReminder(
                        this@EventDetailActivity,
                        it.id,
                        it.title,
                        it.location,
                        reminderTime
                    )

                    Toast.makeText(this@EventDetailActivity, "Reminder set: $label", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@EventDetailActivity, "Could not set reminder", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cycleRsvpStatus(guest: Guest) {
        val nextStatus = when (guest.rsvpStatus) {
            "Pending" -> "Accepted"
            "Accepted" -> "Declined"
            else -> "Pending"
        }
        guestViewModel.updateGuest(guest.copy(rsvpStatus = nextStatus))
    }

    private fun sendInvite(guest: Guest) {
        val message = "Hi ${guest.name}! You're invited to \"$eventTitle\" on $eventDateTime at $eventLocation. Hope to see you there!"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)

        try {
            startActivity(Intent.createChooser(intent, "Send invite via"))
        } catch (e: Exception) {
            Toast.makeText(this, "No app available to send invite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteGuest(guest: Guest) {
        AlertDialog.Builder(this)
            .setTitle("Remove Guest")
            .setMessage("Remove ${guest.name} from this event?")
            .setPositiveButton("Remove") { _, _ ->
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

        AlertDialog.Builder(this)
            .setTitle("Add Guest")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val email = etEmail.text.toString().trim()

                if (name.isNotEmpty()) {
                    val guest = Guest(
                        eventId = eventId,
                        name = name,
                        phone = phone,
                        email = email
                    )
                    guestViewModel.insertGuest(guest)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProgress(items: List<ChecklistItem>) {
        if (items.isEmpty()) {
            progressChecklist.progress = 0
            return
        }
        val completed = items.count { it.isCompleted }
        val percent = (completed * 100) / items.size
        progressChecklist.progress = percent
    }

    private fun showAddTaskDialog() {
        val input = EditText(this)
        input.hint = "Task description"

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val task = input.text.toString().trim()
                if (task.isNotEmpty()) {
                    checklistViewModel.insertItem(
                        ChecklistItem(eventId = eventId, task = task)
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}