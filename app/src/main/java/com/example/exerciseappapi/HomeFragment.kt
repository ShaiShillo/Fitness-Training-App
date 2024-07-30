package com.example.exerciseappapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.ExerciseViewModel
import com.example.exerciseappapi.R
import com.example.exerciseappapi.WorkoutAdapter
import com.example.exerciseappapi.WorkoutEntity
import com.example.exerciseappapi.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var exerciseViewModel: ExerciseViewModel
    private lateinit var selectedDate: String
    private lateinit var adapter: WorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        exerciseViewModel = ViewModelProvider(this).get(ExerciseViewModel::class.java)

        setupCalendarView()
        setupRecyclerView()

        binding.fab.setOnClickListener {
            showBottomSheetDialog()
        }

        return binding.root
    }

    private fun setupCalendarView() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            exerciseViewModel.setSelectedDate(selectedDate)
            updateWorkoutsForSelectedDate()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = WorkoutAdapter(mutableListOf(), { workout ->
            // Handle item click here
            showWorkoutDetails(workout)
        }, { workout ->
            // Handle edit click
        }, { workout, position ->
            // Handle delete click
        }, hideButtons = true) // Hide buttons in HomeFragment
        binding.recyclerView.adapter = adapter

        exerciseViewModel.workoutsForSelectedDate.observe(viewLifecycleOwner) { workouts ->
            adapter.setWorkouts(workouts)
        }

        // Add swipe-to-delete functionality
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val workout = adapter.getWorkoutAt(position)
                exerciseViewModel.removeWorkoutFromDate(workout, selectedDate)
                updateWorkoutsForSelectedDate()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun updateWorkoutsForSelectedDate() {
        exerciseViewModel.getWorkoutsForDate(selectedDate).observe(viewLifecycleOwner, { workouts ->
            adapter.setWorkouts(workouts)
            binding.cardView.visibility = if (workouts.isEmpty()) View.GONE else View.VISIBLE
        })
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_select_workout, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Setup RecyclerView inside the BottomSheetDialog
        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = WorkoutAdapter(mutableListOf(), { workout ->
            exerciseViewModel.addWorkoutToDate(workout, selectedDate)
            bottomSheetDialog.dismiss()
            updateWorkoutsForSelectedDate()
        }, { workout ->
            // Handle edit click
        }, { workout, position ->
            // Handle delete click
        })
        recyclerView.adapter = adapter

        exerciseViewModel.getAllWorkoutsLiveData().observe(viewLifecycleOwner) { workouts ->
            adapter.setWorkouts(workouts)
        }

        bottomSheetDialog.show()
    }

    private fun showWorkoutDetails(workout: WorkoutEntity) {
        // Display the workout details, you can navigate to another fragment or show a dialog
        // For demonstration, let's show a dialog
        val workoutDetailsDialog = BottomSheetDialog(requireContext())
        workoutDetailsDialog.setContentView(R.layout.fragment_workout_detail)

        // Populate the details into the dialog
        // Assuming fragment_workout_detail.xml has TextViews with ids workoutNameTextView, workoutDateTextView, etc.
        workoutDetailsDialog.findViewById<TextView>(R.id.workoutNameTextView)?.text = workout.workoutName
        workoutDetailsDialog.findViewById<TextView>(R.id.workoutDateTextView)?.text = workout.creationDate.toString()
        // Add more details as necessary

        workoutDetailsDialog.show()
    }
}
