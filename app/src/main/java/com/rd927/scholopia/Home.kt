package com.rd927.scholopia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Check if the fragment is already added
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            // Add the HomeFragment to the fragment container
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, HomeFragment())
                .commit()
        }

        // Find the FloatingActionButton and set an OnClickListener in the Activity
        val fab = findViewById<FloatingActionButton>(R.id.fab_add_schedule)
        fab.setOnClickListener {
            // Navigate to CreateSchedule activity when the FAB is clicked
            val intent = Intent(this, CreateSchedule::class.java)
            startActivity(intent)
        }
    }
}
