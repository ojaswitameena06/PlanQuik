package com.example.eventplanner.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.AddEditEventActivity
import com.example.eventplanner.EventDetailActivity
import com.example.eventplanner.R
import com.example.eventplanner.adapter.EventAdapter
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.util.EventUi
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val eventViewModel: EventViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    private lateinit var tvGreeting: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvHeaderAvatar: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var heroCard: View
    private lateinit var tvHeroTitle: TextView
    private lateinit var tvHeroWhen: TextView
    private lateinit var tvHeroLocation: TextView
    private lateinit var tvHeroCountdown: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView

    private var allEvents: List<Event> = emptyList()
    private var selectedFilter = "All"
    private var query = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvHeaderAvatar = view.findViewById(R.id.tvHeaderAvatar)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)
        chipGroup = view.findViewById(R.id.chipGroup)
        heroCard = view.findViewById(R.id.heroCard)
        tvHeroTitle = view.findViewById(R.id.tvHeroTitle)
        tvHeroWhen = view.findViewById(R.id.tvHeroWhen)
        tvHeroLocation = view.findViewById(R.id.tvHeroLocation)
        tvHeroCountdown = view.findViewById(R.id.tvHeroCountdown)
        recyclerView = view.findViewById(R.id.recyclerViewEvents)
        emptyState = view.findViewById(R.id.emptyState)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle)

        adapter = EventAdapter(
            emptyList(),
            onItemClick = { openDetail(it) },
            onItemLongClick = { event ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete event")
                    .setMessage("Delete \"${event.title}\"?")
                    .setPositiveButton("Delete") { _, _ -> eventViewModel.deleteEvent(event) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<TextInputEditText>(R.id.etSearch).doAfterTextChanged {
            query = it?.toString().orEmpty()
            applyFilters()
        }

        setupChips()

        view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddEvent).setOnClickListener {
            startActivity(Intent(requireContext(), AddEditEventActivity::class.java))
        }

        tvHeaderAvatar.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        eventViewModel.allEvents.observe(viewLifecycleOwner) { events ->
            allEvents = events
            applyFilters()
        }

        bindHeader()
    }

    override fun onResume() {
        super.onResume()
        // Refresh greeting and countdowns when coming back to this screen
        if (::adapter.isInitialized) {
            bindHeader()
            applyFilters()
        }
    }

    private fun bindHeader() {
        val fullName = EventUi.userName()
        tvGreeting.text = EventUi.greeting()
        tvUserName.text = fullName.substringBefore(" ")
        tvHeaderAvatar.text = fullName.firstOrNull()?.uppercase() ?: "?"
    }

    private fun setupChips() {
        val filters = listOf(
            "All", "Upcoming", "Past",
            "Birthday", "Wedding", "Meeting", "Party", "Conference", "Other"
        )
        filters.forEach { label ->
            val chip = layoutInflater.inflate(R.layout.chip_filter, chipGroup, false) as Chip
            chip.id = View.generateViewId()
            chip.text = label
            chip.tag = label
            chipGroup.addView(chip)
            if (label == "All") chip.isChecked = true
        }
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            selectedFilter = chip?.tag as? String ?: "All"
            applyFilters()
        }
    }

    private fun applyFilters() {
        val now = System.currentTimeMillis()
        val q = query.trim().lowercase()
        val pairs = allEvents.map { it to EventUi.eventMillis(it) }

        val filtered = pairs.filter { (event, millis) ->
            val matchesChip = when (selectedFilter) {
                "All" -> true
                "Upcoming" -> millis != null && millis >= now
                "Past" -> millis != null && millis < now
                else -> event.category == selectedFilter
            }
            val matchesQuery = q.isEmpty() ||
                    event.title.lowercase().contains(q) ||
                    event.location.lowercase().contains(q)
            matchesChip && matchesQuery
        }

        // Upcoming first (soonest on top), then past events (most recent first)
        val upcoming = filtered.filter { (it.second ?: 0L) >= now }.sortedBy { it.second }
        val past = filtered.filter { (it.second ?: 0L) < now }.sortedByDescending { it.second }
        val shown = (upcoming + past).map { it.first }
        adapter.updateEvents(shown)

        // Subtitle
        val upcomingCount = pairs.count { (it.second ?: 0L) >= now }
        tvSubtitle.text = when (upcomingCount) {
            0 -> "No upcoming events"
            1 -> "You have 1 upcoming event"
            else -> "You have $upcomingCount upcoming events"
        }

        // Next up card (only on the unfiltered view)
        val next = pairs
            .mapNotNull { (event, millis) -> if (millis != null && millis >= now) event to millis else null }
            .minByOrNull { it.second }
        if (next != null && selectedFilter == "All" && q.isEmpty()) {
            val (event, millis) = next
            heroCard.visibility = View.VISIBLE
            tvHeroTitle.text = event.title
            tvHeroWhen.text = EventUi.format(millis, "EEE, d MMM • h:mm a")
            tvHeroLocation.text = event.location
            tvHeroCountdown.text = EventUi.countdown(millis, now)
            heroCard.setOnClickListener { openDetail(event) }
        } else {
            heroCard.visibility = View.GONE
        }

        // Empty state
        if (shown.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            if (allEvents.isEmpty()) {
                tvEmptyTitle.text = "No events yet"
                tvEmptySubtitle.text = "Tap New event to plan your first one."
            } else {
                tvEmptyTitle.text = "Nothing matches"
                tvEmptySubtitle.text = "Try a different search or filter."
            }
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun openDetail(event: Event) {
        val intent = Intent(requireContext(), EventDetailActivity::class.java)
        intent.putExtra("EVENT_ID", event.id)
        startActivity(intent)
    }
}