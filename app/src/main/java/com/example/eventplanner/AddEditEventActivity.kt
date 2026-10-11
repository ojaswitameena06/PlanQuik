package com.example.eventplanner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.ui.home.EventViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import com.google.firebase.auth.FirebaseAuth

class AddEditEventActivity : AppCompatActivity() {

    private val eventViewModel: EventViewModel by viewModels()

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var etBudget: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvFormTitle: TextView

    private var selectedDate = ""
    private var selectedTime = ""
    private var editingEventId: Long = -1L

    private val categories = arrayOf("Birthday", "Wedding", "Meeting", "Party", "Conference", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_event)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etLocation = findViewById(R.id.etLocation)
        etBudget = findViewById(R.id.etBudget)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        tvFormTitle = findViewById(R.id.tvFormTitle)

        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        btnBack.setOnClickListener { finish() }
        etDate.setOnClickListener { showDatePicker() }
        etTime.setOnClickListener { showTimePicker() }
        btnSave.setOnClickListener { saveEvent() }

        editingEventId = intent.getLongExtra("EVENT_ID", -1L)
        if (editingEventId != -1L) {
            tvFormTitle.text = "Edit Event"
            loadEventData(editingEventId)
        }
    }

    private fun loadEventData(eventId: Long) {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId)
            event?.let {
                etTitle.setText(it.title)
                etDescription.setText(it.description)
                etDate.setText(it.date)
                etTime.setText(it.time)
                etLocation.setText(it.location)
                etBudget.setText(if (it.budget == 0.0) "" else it.budget.toString())
                selectedDate = it.date
                selectedTime = it.time
                val categoryIndex = categories.indexOf(it.category)
                if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                etTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveEvent() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val budgetText = etBudget.text.toString().trim()
        val budget = if (budgetText.isEmpty()) 0.0 else budgetText.toDoubleOrNull() ?: 0.0

        if (title.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill title, date, time, and location", Toast.LENGTH_SHORT).show()
            return
        }

        val event = Event(
            id = if (editingEventId != -1L) editingEventId else 0,
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            title = title,
            description = description,
            date = selectedDate,
            time = selectedTime,
            location = location,
            category = category,
            budget = budget
        )

        if (editingEventId != -1L) {
            eventViewModel.updateEvent(event)
            Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show()
        } else {
            eventViewModel.insertEvent(event)
            Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}