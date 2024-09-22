package com.rd927.scholopia

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CreateSchedule : AppCompatActivity() {

    private var selectedDateTime: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get references to form fields
        val titleInput = findViewById<EditText>(R.id.titleInput)
        val prioritySpinner = findViewById<Spinner>(R.id.prioritySpinner)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val recurrenceSpinner = findViewById<Spinner>(R.id.recurrenceSpinner)
        val notesInput = findViewById<EditText>(R.id.notesInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val pickDateTimeButton = findViewById<Button>(R.id.pickDateTime)
        val dateTimeLabel = findViewById<TextView>(R.id.dateTimeLabel)

        pickDateTimeButton.setOnClickListener {
            showDateTimePicker { dateTime ->
                selectedDateTime = dateTime
                dateTimeLabel.text = "Selected Date & Time: $dateTime"
            }
        }

        saveButton.setOnClickListener {
            // Gather the form data
            val title = titleInput.text.toString().trim()
            val priority = prioritySpinner.selectedItem.toString()
            val category = categorySpinner.selectedItem.toString()
            val recurrence = recurrenceSpinner.selectedItem.toString()
            val notes = notesInput.text.toString().trim()

            if (title.isNotEmpty()) {
                // Save schedule to the Room database
                saveScheduleToDatabase(title, priority, category, recurrence, notes)
            } else {
                Toast.makeText(this, "Please enter a title for the schedule", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save the form data to the Room database
    private fun saveScheduleToDatabase(title: String, priority: String, category: String, recurrence: String, notes: String) {
        val scheduleDao = AppDatabase.getDatabase(this).scheduleDao()

        CoroutineScope(Dispatchers.IO).launch {
            val schedule = Schedule(
                title = title,
                priority = priority,
                category = category,
                recurrence = recurrence,
                notes = notes,
                dateTime = selectedDateTime,  // <-- Use the selectedDateTime here
                userId = getCurrentUserId()   // Get the currently logged-in user's ID
            )
            scheduleDao.insertSchedule(schedule)

            // Schedule a notification for the selected date and time
            val notificationHelper = NotificationHelper(this@CreateSchedule)
//            notificationHelper.createNotificationChannel()
            val triggerTime = getTriggerTime(selectedDateTime)
            notificationHelper.scheduleNotification(title, notes, triggerTime)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CreateSchedule, "Schedule saved successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after saving
            }
        }
    }

    // Helper function to convert the selected date and time to a trigger time
    private fun getTriggerTime(selectedDateTime: String): Long {
        val calendar = Calendar.getInstance()
        val parts = selectedDateTime.split(" ")
        val dateParts = parts[0].split("/")
        val timeParts = parts[1].split(":")
        calendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
        calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1) // Months are 0-based
        calendar.set(Calendar.YEAR, dateParts[2].toInt())
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())
        return calendar.timeInMillis
    }

    private fun getCurrentUserId(): Int {
        // Retrieve the logged-in user ID from SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1) // Assuming user_id was stored during login
    }

    private fun showDateTimePicker(onDateTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Once the date is picked, show the TimePicker
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val selectedDateTime = "$selectedDay/${selectedMonth + 1}/$selectedYear $selectedHour:$selectedMinute"
                onDateTimeSelected(selectedDateTime)
            }, hour, minute, true).show()

        }, year, month, day).show()
    }
}
