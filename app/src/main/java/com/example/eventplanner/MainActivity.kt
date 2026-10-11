package com.example.eventplanner

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.adapter.EventAdapter
import com.example.eventplanner.notifications.NotificationHelper
import com.example.eventplanner.ui.home.EventViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val eventViewModel: EventViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var fabAddEvent: FloatingActionButton

    private lateinit var btnSettings: android.widget.ImageButton

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createNotificationChannel(this)
        askNotificationPermission()

        recyclerView = findViewById(R.id.recyclerViewEvents)
        fabAddEvent = findViewById(R.id.fabAddEvent)
        btnSettings = findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        adapter = EventAdapter(
            emptyList(),
            onItemClick = { event ->
                val intent = Intent(this, EventDetailActivity::class.java)
                intent.putExtra("EVENT_ID", event.id)
                startActivity(intent)
            },
            onItemLongClick = { event ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Delete \"${event.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        eventViewModel.deleteEvent(event)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        eventViewModel.allEvents.observe(this) { events ->
            adapter.updateEvents(events)
        }

        fabAddEvent.setOnClickListener {
            startActivity(Intent(this, AddEditEventActivity::class.java))
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}