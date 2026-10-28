package com.example.eventplanner.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.eventplanner.LoginActivity
import com.example.eventplanner.R
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.example.eventplanner.sync.RsvpSync

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val user = FirebaseAuth.getInstance().currentUser

        val email = user?.email ?: ""
        val name = user?.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")

        view.findViewById<TextView>(R.id.tvAvatar).text = name.firstOrNull()?.uppercase() ?: "?"
        view.findViewById<TextView>(R.id.tvProfileName).text = name
        view.findViewById<TextView>(R.id.tvProfileEmail).text = email

        val switchDark = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        switchDark.isChecked = prefs.getBoolean("dark_mode", false)
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            RsvpSync.stop()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}