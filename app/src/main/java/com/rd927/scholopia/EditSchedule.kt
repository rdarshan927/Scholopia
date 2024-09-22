package com.rd927.scholopia

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class EditSchedule : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editPriority: EditText
    private lateinit var editCategory: EditText
    private lateinit var editRecurrence: EditText
    private lateinit var editNotes: EditText
    private lateinit var editDateTime: EditText
    private lateinit var btnSave: Button
    private var scheduleId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_scedule)

        // Get the schedule ID passed from the previous activity
        scheduleId = intent.getIntExtra("schedule_id", 0)

        // Initialize views
        editTitle = findViewById(R.id.editTitle)
        editPriority = findViewById(R.id.editPriority)
        editCategory = findViewById(R.id.editCategory)
        editRecurrence = findViewById(R.id.editRecurrence)
        editNotes = findViewById(R.id.editNotes)
        editDateTime = findViewById(R.id.editDateTime)
        btnSave = findViewById(R.id.btnSave)

        // Load existing schedule details
        loadScheduleDetails()

        // Set click listener for the save button
        btnSave.setOnClickListener {
            updateSchedule()
        }
    }

    private fun loadScheduleDetails() {
        val appDatabase = AppDatabase.getDatabase(this)
        val scheduleDao = appDatabase.scheduleDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val schedule = scheduleDao.getScheduleById(scheduleId)

            withContext(Dispatchers.Main) {
                editTitle.setText(schedule.title)
                editPriority.setText(schedule.priority)
                editCategory.setText(schedule.category)
                editRecurrence.setText(schedule.recurrence)
                editNotes.setText(schedule.notes)
                editDateTime.setText(schedule.dateTime)
            }
        }
    }

    private fun updateSchedule() {
        val updatedSchedule = Schedule(
            id = scheduleId,
            title = editTitle.text.toString(),
            priority = editPriority.text.toString(),
            category = editCategory.text.toString(),
            recurrence = editRecurrence.text.toString(),
            notes = editNotes.text.toString(),
            dateTime = editDateTime.text.toString(),
            userId = getUserIdFromPreferences() // Assuming you have a method to get the userId
        )

        val appDatabase = AppDatabase.getDatabase(this)
        val scheduleDao = appDatabase.scheduleDao()

        lifecycleScope.launch(Dispatchers.IO) {
            scheduleDao.updateSchedule(updatedSchedule)

            // Cancel any existing notifications
            val notificationHelper = NotificationHelper(this@EditSchedule)
            notificationHelper.cancelAllNotifications()

            // Schedule a new notification
            val triggerTime = getTriggerTime(editDateTime.text.toString())
            notificationHelper.createNotificationChannel()
            notificationHelper.scheduleNotification(updatedSchedule.title,
                updatedSchedule.notes.toString(), triggerTime)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditSchedule, "Schedule updated", Toast.LENGTH_SHORT).show()
                finish() // Close the activity and return to previous screen
            }
        }
    }

    private fun getUserIdFromPreferences(): Int {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", 0)
    }

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
}
