package com.rd927.scholopia

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.*

class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update the widget with the latest schedule data
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Get the schedules from the database
        val appDatabase = AppDatabase.getDatabase(context)
        val scheduleDao = appDatabase.scheduleDao()

        // Create a RemoteViews object to display the schedule data
        val views = RemoteViews(context.packageName, R.layout.schedule_widget)

        // Get the schedules from the database and display them in the RemoteViews object
        CoroutineScope(Dispatchers.IO).launch {
            val schedules = scheduleDao.getSchedulesByUserId(1) // Replace with the actual user ID
            // After fetching the schedules, switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
                // Update the RemoteViews object with the schedule data
                views.setTextViewText(R.id.schedule_text, schedules.joinToString("\n") { it.title })
                // Update the app widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
