package com.rd927.scholopia

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // Foreign key to associate tasks with users
    val title: String,
    val description: String,
    val scheduledTime: Long // Store time in milliseconds
)
