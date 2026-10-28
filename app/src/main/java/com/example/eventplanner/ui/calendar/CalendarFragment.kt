
package com.example.eventplanner.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.CalendarDay
import com.example.eventplanner.R
import com.example.eventplanner.adapter.CalendarAdapter
import com.example.eventplanner.adapter.CalendarEventAdapter
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.ui.home.EventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private lateinit var monthYearText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: CalendarEventAdapter

    private lateinit var eventViewModel: EventViewModel

    private var displayedMonth = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()
    private var allEvents: List<Event> = emptyList()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthYearText = view.findViewById(R.id.monthYearText)
        selectedDateText = view.findViewById(R.id.selectedDateText)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView)

        val previousButton =
            view.findViewById<ImageButton>(R.id.previousMonthButton)
        val nextButton =
            view.findViewById<ImageButton>(R.id.nextMonthButton)

        calendarRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 7)

        eventsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        eventAdapter = CalendarEventAdapter(emptyList())
        eventsRecyclerView.adapter = eventAdapter

        eventViewModel = ViewModelProvider(this)
            .get(EventViewModel::class.java)

        eventViewModel.allEvents.observe(viewLifecycleOwner) { events ->
            allEvents = events ?: emptyList()
            showCalendar()
        }

        previousButton.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, -1)
            showCalendar()
        }

        nextButton.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, 1)
            showCalendar()
        }

        showCalendar()
    }

    private fun showCalendar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearText.text = monthFormat.format(displayedMonth.time)

        val monthKey = SimpleDateFormat("yyyy-MM", Locale.US)
            .format(displayedMonth.time)

        val eventDays = allEvents
            .filter { it.date.startsWith("$monthKey-") }
            .mapNotNull { it.date.substringAfterLast("-").toIntOrNull() }
            .toSet()

        val adapter = CalendarAdapter(
            days = generateCalendarDays(),
            selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH),
            selectedMonth = selectedDate.get(Calendar.MONTH),
            selectedYear = selectedDate.get(Calendar.YEAR),
            eventDays = eventDays
        ) { day ->
            if (day.isCurrentMonth) {
                selectedDate.set(
                    day.year,
                    day.month,
                    day.day
                )
                showCalendar()
            }
        }

        calendarRecyclerView.adapter = adapter

        val selectedDateString = dateFormat.format(selectedDate.time)

        selectedDateText.text = "Events on " +
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    .format(selectedDate.time)

        val selectedEvents = allEvents.filter { event ->
            event.date == selectedDateString
        }

        eventAdapter.updateEvents(selectedEvents)
    }

    private fun generateCalendarDays(): List<CalendarDay> {
        val result = mutableListOf<CalendarDay>()
        val calendar = displayedMonth.clone() as Calendar

        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until firstDay) {
            result.add(
                CalendarDay(
                    0,
                    displayedMonth.get(Calendar.MONTH),
                    displayedMonth.get(Calendar.YEAR),
                    false
                )
            )
        }

        for (day in 1..daysInMonth) {
            result.add(
                CalendarDay(
                    day,
                    displayedMonth.get(Calendar.MONTH),
                    displayedMonth.get(Calendar.YEAR),
                    true
                )
            )
        }

        while (result.size % 7 != 0) {
            result.add(
                CalendarDay(
                    0,
                    displayedMonth.get(Calendar.MONTH),
                    displayedMonth.get(Calendar.YEAR),
                    false
                )
            )
        }

        return result
    }
}
