package com.rd927.scholopia

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleAdapter(private val activity: FragmentActivity) :
    ListAdapter<Schedule, ScheduleAdapter.ViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var isExpanded = false

        private val title: TextView = itemView.findViewById(R.id.title)
        private val detailsLayout: LinearLayout = itemView.findViewById(R.id.details_layout)
        private val priority: TextView = itemView.findViewById(R.id.priority)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val recurrence: TextView = itemView.findViewById(R.id.recurrence)
        private val notes: TextView = itemView.findViewById(R.id.notes)
        private val dateTime: TextView = itemView.findViewById(R.id.dateTime)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        // Bind data to the view
        fun bind(schedule: Schedule) {
            // Retrieve userId from SharedPreferences here
            val sharedPref = activity.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", 0)

            title.text = schedule.title
            priority.text = "Priority: ${schedule.priority}"
            category.text = "Category: ${schedule.category}"
            recurrence.text = "Recurrence: ${schedule.recurrence}"
            notes.text = "Notes: ${schedule.notes}"
            dateTime.text = "Date: ${schedule.dateTime}"

            // Initially hide the details if collapsed
            detailsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Set a click listener to toggle the expansion/collapse
            title.setOnClickListener {
                isExpanded = !isExpanded
                detailsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            }

            // Handle ImageView click to show the popup modal for edit/delete
            imageView.setOnClickListener {
                showPopupModal(itemView.context, schedule, userId)
            }
        }

        // Function to show a pop-up modal for edit/delete options
        private fun showPopupModal(context: Context, schedule: Schedule, userId: Int) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(schedule.title) // Show the schedule title

            builder.setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                when (which) {
                    0 -> { // Edit option
                        val intent = Intent(context, EditSchedule::class.java)
                        intent.putExtra("schedule_id", schedule.id)
                        context.startActivity(intent)
                    }
                    1 -> { // Delete option
                        deleteSchedule(schedule.id, userId, context)
                    }
                }
                dialog.dismiss()
            }

            // Show the AlertDialog
            builder.create().show()
        }

        // Function to delete the schedule from the database
        private fun deleteSchedule(scheduleId: Int, userId: Int, context: Context) {
            val appDatabase = AppDatabase.getDatabase(context)
            val scheduleDao = appDatabase.scheduleDao()

            // Launch a coroutine to delete the schedule from the database
            activity.lifecycleScope.launch(Dispatchers.IO) {
                scheduleDao.deleteSchedule(scheduleId)

                // Fetch updated list after deletion
                val updatedList = scheduleDao.getSchedulesByUserId(userId)

                withContext(Dispatchers.Main) {
                    // Refresh the list after deletion or notify the user
                    submitList(updatedList)
                    Toast.makeText(context, "Schedule deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// DiffUtil implementation
class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
    override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
        return oldItem == newItem
    }
}
