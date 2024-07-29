package com.example.exerciseappapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.FragmentWorkoutsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WorkoutsFragment : Fragment() {

    private val viewModel: ExerciseViewModel by viewModels()
    private var _binding: FragmentWorkoutsBinding? = null
    private val binding get() = _binding!!
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
        setupObservers()

        // Handle the received new workout from CreateWorkoutFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("workoutAdded")
            ?.observe(viewLifecycleOwner) { workoutAdded ->
                if (workoutAdded == true) {
                    Log.d("WorkoutsFragment", "New workout added")
                    viewModel.fetchWorkouts()
                }
            }
    }

    private fun setupRecyclerView() {
        binding.workoutsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WorkoutAdapter(
            mutableListOf(),
            onItemClick = { workout ->
                val action = WorkoutsFragmentDirections.actionWorkoutsFragmentToWorkoutDetailFragment(workout)
                findNavController().navigate(action)
            },
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

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.workouts.collect { workouts ->
                adapter.setWorkouts(workouts.map { it.toWorkout() })
            }
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
                viewModel.deleteWorkout(workout.toWorkoutEntity())
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
