package com.rd927.scholopia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class HomeFragment : Fragment() {

    private lateinit var scheduleDao: ScheduleDao
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var userId by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Get the ID of the logged-in user from shared preferences
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", 0)

        // Get the schedules from the database
        val appDatabase = AppDatabase.getDatabase(requireContext())
        scheduleDao = appDatabase.scheduleDao()

        // Create a RecyclerView to display the schedules
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create a ScheduleAdapter to bind the schedules to the RecyclerView
        scheduleAdapter = ScheduleAdapter(requireActivity())
        recyclerView.adapter = scheduleAdapter

        // Get the schedules from the database and display them in the RecyclerView
        lifecycleScope.launch(Dispatchers.IO) {
            val schedules = scheduleDao.getSchedulesByUserId(userId)
            // After fetching the schedules, switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
                scheduleAdapter.submitList(schedules)
            }
        }

        return view


    }

    // Method to load schedules from the database
    private fun loadSchedules() {
        lifecycleScope.launch(Dispatchers.IO) {
            val schedules = scheduleDao.getSchedulesByUserId(userId)
            withContext(Dispatchers.Main) {
                scheduleAdapter.submitList(schedules)
            }
        }
    }
    // Refresh the schedule list when the fragment is resumed
    override fun onResume() {
        super.onResume()
        loadSchedules()  // Reload schedules whenever the fragment is resumed
    }
}
