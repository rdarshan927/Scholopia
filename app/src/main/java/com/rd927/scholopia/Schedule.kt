package com.rd927.scholopia

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priority: String,
    val category: String,
    val recurrence: String,
    val notes: String?,
    val dateTime: String, // <-- New field for storing date and time
    val userId: Int // Link the schedule to a specific user
)
