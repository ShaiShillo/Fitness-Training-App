package com.example.exerciseappapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var exerciseViewModel: ExerciseViewModel
    private lateinit var selectedDate: String
    private lateinit var calendarAdapter: WeeklyCalendarAdapter

    private var currentWeekStart: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        exerciseViewModel = ViewModelProvider(this).get(ExerciseViewModel::class.java)

        setupWeeklyCalendar()
        setupRecyclerView()

        binding.fab.setOnClickListener {
            showBottomSheetDialog()
        }

        return binding.root
    }

    private fun setupWeeklyCalendar() {
        val yearMonthTextView = binding.weeklyCalendarContainer.textViewYearMonth
        updateWeeklyCalendar(yearMonthTextView)

        binding.weeklyCalendarContainer.previousWeekButton.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeeklyCalendar(yearMonthTextView)
        }

        binding.weeklyCalendarContainer.nextWeekButton.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeeklyCalendar(yearMonthTextView)
        }
    }

    private fun updateWeeklyCalendar(yearMonthTextView: TextView) {
        val daysOfWeek = getDaysOfWeek(currentWeekStart.time)
        val recyclerViewWeek = binding.weeklyCalendarContainer.recyclerViewWeek

        if (!::calendarAdapter.isInitialized) {
            calendarAdapter = WeeklyCalendarAdapter(daysOfWeek) { date ->
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                exerciseViewModel.setSelectedDate(selectedDate)
                updateWorkoutsForSelectedDate()
            }
            recyclerViewWeek.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerViewWeek.adapter = calendarAdapter
        } else {
            calendarAdapter.updateDays(daysOfWeek)
        }

        val yearMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        yearMonthTextView.text = yearMonthFormat.format(currentWeekStart.time)
    }

    private fun getDaysOfWeek(startDate: Date): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val days = mutableListOf<Date>()
        for (i in 0..6) {
            days.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = WorkoutAdapter(mutableListOf(), { workout ->
            showWorkoutDetails(workout)
        }, { workout ->
            // Handle edit click
        }, { workout, position ->
            exerciseViewModel.removeWorkoutFromDate(workout, selectedDate)
            updateWorkoutsForSelectedDate()
        })
        binding.recyclerView.adapter = adapter

        exerciseViewModel.workoutsForSelectedDate.observe(viewLifecycleOwner) { workouts ->
            adapter.setWorkouts(workouts)
        }

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
        exerciseViewModel.getWorkoutsForDate(selectedDate).observe(viewLifecycleOwner) { workouts ->
            (binding.recyclerView.adapter as WorkoutAdapter).setWorkouts(workouts)
            binding.cardView.visibility = if (workouts.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_select_workout, null)
        bottomSheetDialog.setContentView(bottomSheetView)

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
        val workoutDetailsDialog = BottomSheetDialog(requireContext())
        workoutDetailsDialog.setContentView(R.layout.fragment_workout_detail)

        workoutDetailsDialog.findViewById<TextView>(R.id.workoutNameTextView)?.text = workout.workoutName
        workoutDetailsDialog.findViewById<TextView>(R.id.workoutDateTextView)?.text = workout.creationDate.toString()

        workoutDetailsDialog.show()
    }
}
