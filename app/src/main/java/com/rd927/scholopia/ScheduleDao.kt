package com.rd927.scholopia

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScheduleDao {
    @Insert
    fun insertSchedule(schedule: Schedule)

    @Query("SELECT * FROM schedules WHERE userId = :userId")
    fun getSchedulesByUserId(userId: Int): List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    fun getScheduleById(scheduleId: Int): Schedule

    @Update
    fun updateSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :scheduleId")
    fun deleteSchedule(scheduleId: Int)
}
