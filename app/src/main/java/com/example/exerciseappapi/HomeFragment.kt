package com.example.exerciseappapi

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

        // Initialize selectedDate to the current date
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        setupWeeklyCalendar()
        setupRecyclerView()

        binding.fab.setOnClickListener {
            showBottomSheetDialog()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Re-initialize the calendar and fetch data when returning to this fragment
        updateCalendarAndWorkouts()
    }

    private fun updateCalendarAndWorkouts() {
        updateWeeklyCalendar()
        updateWorkoutsForSelectedDate()
    }

    private fun setupWeeklyCalendar() {
        val recyclerViewWeek = binding.weeklyCalendarContainer.recyclerViewWeek
        calendarAdapter = WeeklyCalendarAdapter(emptyList()) { date ->
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            exerciseViewModel.setSelectedDate(selectedDate)
            updateWorkoutsForSelectedDate()
        }
        recyclerViewWeek.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewWeek.adapter = calendarAdapter

        updateWeeklyCalendar()

        binding.weeklyCalendarContainer.previousWeekButton.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeeklyCalendar()
        }

        binding.weeklyCalendarContainer.nextWeekButton.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeeklyCalendar()
        }
    }

    private fun updateWeeklyCalendar() {
        val daysOfWeek = getDaysOfWeek(currentWeekStart.time)
        calendarAdapter.updateDays(daysOfWeek)

        // Update year and month display
        val yearMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.weeklyCalendarContainer.textViewYearMonth.text = yearMonthFormat.format(currentWeekStart.time)

        // Ensure the current date is displayed as selected
        calendarAdapter.setSelectedDate(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)!!)
    }

    private fun getDaysOfWeek(startDate: Date): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return (0..6).map {
            calendar.time.also { calendar.add(Calendar.DAY_OF_MONTH, 1) }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = WorkoutAdapter(
            mutableListOf(),
            { workout ->
                showWorkoutDetails(workout)
            },
            { workout ->
                // Handle edit click
            },
            { workout, position ->
                exerciseViewModel.removeWorkoutFromDate(workout, selectedDate)
                updateWorkoutsForSelectedDate()
            },
            showEditButton = false,
            showDeleteButton = false,
            showAddAlarmButton = true,
            onAddAlarmClick = { workout ->
                showSetAlarmDialog(workout)
            }
        )
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
        val adapter = WorkoutAdapter(
            mutableListOf(),
            { workout ->
                if (selectedDate.isNotEmpty()) {
                    exerciseViewModel.addWorkoutToDate(workout, selectedDate)
                    bottomSheetDialog.dismiss()
                    updateWorkoutsForSelectedDate()
                } else {
                    // Show an error message or a Toast to inform the user to select a date first
                }
            },
            { workout ->
                // Handle edit click
            },
            { workout, position ->
                // Handle delete click
            },
            showEditButton = false,
            showDeleteButton = false
        )
        recyclerView.adapter = adapter

        exerciseViewModel.getAllWorkoutsLiveData().observe(viewLifecycleOwner) { workouts ->
            adapter.setWorkouts(workouts)
        }

        bottomSheetDialog.show()
    }

    private fun showWorkoutDetails(workout: WorkoutEntity) {
        val action = HomeFragmentDirections.actionHomeFragmentToWorkoutDetailFragment(workout.toWorkout())
        findNavController().navigate(action)
    }

    private fun showSetAlarmDialog(workout: WorkoutEntity) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_alarm, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)

        AlertDialog.Builder(requireContext())
            .setTitle("Set Alarm")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val hour = timePicker.hour
                val minute = timePicker.minute
                val selectedInterval = when (radioGroup.checkedRadioButtonId) {
                    R.id.radio_15_min -> 15
                    R.id.radio_30_min -> 30
                    R.id.radio_1_hour -> 60
                    R.id.radio_3_hours -> 180
                    else -> 0
                }
                setAlarm(workout, hour, minute, selectedInterval)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setAlarm(workout: WorkoutEntity, hour: Int, minute: Int, reminderInterval: Int) {
        // Schedule the alarm
        scheduleAlarm(workout, hour, minute, reminderInterval)
    }

    @SuppressLint("UnspecifiedImmutableFlag", "ScheduleExactAlarm")
    private fun scheduleAlarm(workout: WorkoutEntity, hour: Int, minute: Int, reminderInterval: Int) {
        val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("workout_name", workout.workoutName)
            putExtra("workout_id", workout.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), workout.id, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis - reminderInterval * 60 * 1000, pendingIntent)

        Toast.makeText(requireContext(), "Alarm set for ${workout.workoutName}", Toast.LENGTH_SHORT).show()
    }
}
