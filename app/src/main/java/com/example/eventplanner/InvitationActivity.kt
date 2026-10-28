
package com.example.eventplanner

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eventplanner.ui.home.EventViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class InvitationActivity : AppCompatActivity() {

    private val eventViewModel: EventViewModel by viewModels()

    private lateinit var invitationCard: LinearLayout
    private lateinit var tvInvitationEyebrow: TextView
    private lateinit var tvInvitationTitle: TextView
    private lateinit var tvInvitationMessage: TextView
    private lateinit var tvInvitationDate: TextView
    private lateinit var tvInvitationTime: TextView
    private lateinit var tvInvitationLocation: TextView
    private lateinit var tvInvitationFooter: TextView

    private var eventId: Long = -1L
    private var invitationTitle = "Event Invitation"
    private var invitationDate = ""
    private var invitationTime = ""
    private var invitationLocation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitation)

        eventId = intent.getLongExtra("EVENT_ID", -1L)

        if (eventId == -1L) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        invitationCard = findViewById(R.id.invitationCard)
        tvInvitationEyebrow = findViewById(R.id.tvInvitationEyebrow)
        tvInvitationTitle = findViewById(R.id.tvInvitationTitle)
        tvInvitationMessage = findViewById(R.id.tvInvitationMessage)
        tvInvitationDate = findViewById(R.id.tvInvitationDate)
        tvInvitationTime = findViewById(R.id.tvInvitationTime)
        tvInvitationLocation = findViewById(R.id.tvInvitationLocation)
        tvInvitationFooter = findViewById(R.id.tvInvitationFooter)

        findViewById<ImageButton>(R.id.btnInvitationBack).setOnClickListener {
            finish()
        }

        findViewById<RadioGroup>(R.id.rgInvitationTemplates)
            .setOnCheckedChangeListener { _, checkedId ->
                applyTemplate(checkedId)
            }

        findViewById<MaterialButton>(R.id.btnSaveInvitation)
            .setOnClickListener {
                val uri = saveInvitationImage()
                if (uri != null) {
                    Toast.makeText(
                        this,
                        "Invitation saved to Pictures/EventPlanner",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        findViewById<MaterialButton>(R.id.btnShareInvitation)
            .setOnClickListener {
                val uri = saveInvitationImage()
                if (uri != null) {
                    shareInvitation(uri)
                }
            }

        loadEvent()
    }

    private fun loadEvent() {
        lifecycleScope.launch {
            try {
                val event = eventViewModel.getEventById(eventId)

                if (event == null) {
                    Toast.makeText(
                        this@InvitationActivity,
                        "Could not find this event",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                invitationTitle = event.title
                invitationDate = formatDate(event.date)
                invitationTime = event.time
                invitationLocation = event.location

                tvInvitationTitle.text = event.title
                tvInvitationDate.text = "Date: ${event.date.let { formatDate(it) }}"
                tvInvitationTime.text = "Time: ${event.time}"
                tvInvitationLocation.text = "Location: ${event.location}"

                applyTemplate(R.id.rbElegant)
            } catch (e: Exception) {
                Toast.makeText(
                    this@InvitationActivity,
                    "Unable to load event details",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            outputFormat.format(inputFormat.parse(date)!!)
        } catch (e: Exception) {
            date
        }
    }

    private fun applyTemplate(templateId: Int) {
        when (templateId) {
            R.id.rbCelebration -> {
                invitationCard.setBackgroundColor(Color.rgb(255, 239, 213))
                setInvitationTextColors(
                    Color.rgb(126, 45, 85),
                    Color.rgb(126, 45, 85)
                )
                tvInvitationEyebrow.text = "LET'S CELEBRATE!"
                tvInvitationMessage.text =
                    "A special day deserves special people. Come celebrate with us!"
                tvInvitationFooter.text = "GOOD TIMES • GREAT MEMORIES"
            }

            R.id.rbFormal -> {
                invitationCard.setBackgroundColor(Color.rgb(232, 238, 246))
                setInvitationTextColors(
                    Color.rgb(28, 48, 77),
                    Color.rgb(28, 48, 77)
                )
                tvInvitationEyebrow.text = "FORMAL INVITATION"
                tvInvitationMessage.text =
                    "You are cordially invited to attend this event."
                tvInvitationFooter.text = "YOUR PRESENCE IS APPRECIATED"
            }

            else -> {
                invitationCard.setBackgroundColor(Color.rgb(255, 248, 238))
                setInvitationTextColors(
                    Color.rgb(56, 41, 30),
                    Color.rgb(128, 90, 54)
                )
                tvInvitationEyebrow.text = "YOU ARE INVITED"
                tvInvitationMessage.text =
                    "We would love for you to join us for this special occasion."
                tvInvitationFooter.text = "WE LOOK FORWARD TO SEEING YOU"
            }
        }
    }

    private fun setInvitationTextColors(
        mainColor: Int,
        accentColor: Int
    ) {
        tvInvitationTitle.setTextColor(mainColor)
        tvInvitationDate.setTextColor(mainColor)
        tvInvitationTime.setTextColor(mainColor)
        tvInvitationLocation.setTextColor(mainColor)
        tvInvitationMessage.setTextColor(mainColor)
        tvInvitationEyebrow.setTextColor(accentColor)
        tvInvitationFooter.setTextColor(accentColor)
    }

    private fun saveInvitationImage(): Uri? {
        return try {
            if (invitationCard.width == 0 || invitationCard.height == 0) {
                Toast.makeText(
                    this,
                    "Please wait for the invitation preview to load",
                    Toast.LENGTH_SHORT
                ).show()
                return null
            }

            val bitmap = Bitmap.createBitmap(
                invitationCard.width,
                invitationCard.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            invitationCard.draw(canvas)

            val fileName = "Invitation_${System.currentTimeMillis()}.png"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "Pictures/EventPlanner"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val imageUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: run {
                bitmap.recycle()
                return null
            }

            contentResolver.openOutputStream(imageUri)?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            } ?: run {
                contentResolver.delete(imageUri, null, null)
                bitmap.recycle()
                return null
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val completedValues = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                contentResolver.update(imageUri, completedValues, null, null)
            }

            bitmap.recycle()
            imageUri
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Could not save invitation: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            null
        }
    }

    private fun shareInvitation(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(
            Intent.createChooser(shareIntent, "Share your invitation")
        )
    }
}
