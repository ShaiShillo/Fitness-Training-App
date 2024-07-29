package com.example.exerciseappapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.Workout
import com.example.exerciseappapi.WorkoutAdapter
import com.example.exerciseappapi.databinding.FragmentWorkoutsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WorkoutsFragment : Fragment() {

    private var _binding: FragmentWorkoutsBinding? = null
    private val binding get() = _binding!!
    private val workouts = mutableListOf<Workout>()
    private lateinit var adapter: WorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupSwipeToDelete()

        // Handle the received new workout from CreateWorkoutFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("newWorkoutBundle")
            ?.observe(viewLifecycleOwner, { bundle ->
                val workout = bundle?.getParcelable<Workout>("newWorkout")
                if (workout != null) {
                    Log.d("WorkoutsFragment", "New workout received: ${workout.workoutName}")
                    workouts.add(workout)
                    adapter.notifyDataSetChanged()
                }
            })
    }

    private fun setupRecyclerView() {
        binding.workoutsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WorkoutAdapter(
            workouts,
            onEditClick = { workout ->
                editWorkout(workout)
            },
            onDeleteClick = { workout, position ->
                showDeleteConfirmationDialog(workout, position)
            }
        )
        binding.workoutsRecyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddWorkout.setOnClickListener {
            findNavController().navigate(R.id.action_workoutsFragment_to_createWorkoutFragment)
        }
    }

    private fun editWorkout(workout: Workout) {
        // Implement edit workout functionality here
        // Navigate to a fragment to edit the workout
        // You can add appropriate arguments to the navigation action
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val workout = adapter.getWorkoutAt(position)
                showDeleteConfirmationDialog(workout, position)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.workoutsRecyclerView)
    }

    private fun showDeleteConfirmationDialog(workout: Workout, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyItemChanged(position)
            }
            .setPositiveButton("Delete") { dialog, _ ->
                workouts.removeAt(position)
                adapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
