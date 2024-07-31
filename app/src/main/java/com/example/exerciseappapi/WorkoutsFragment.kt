package com.example.exerciseappapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.FragmentWorkoutsBinding
import com.example.exerciseappapi.utils.DialogUtils

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

    override fun onResume() {
        super.onResume()
        viewModel.fetchWorkouts() // Ensure workouts are loaded when the fragment becomes visible
    }

    private fun setupRecyclerView() {
        binding.workoutsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WorkoutAdapter(
            mutableListOf(),
            onItemClick = { workoutEntity ->
                val action = WorkoutsFragmentDirections.actionWorkoutsFragmentToWorkoutDetailFragment(workoutEntity.toWorkout())
                findNavController().navigate(action)
            },
            onEditClick = { workoutEntity ->
                editWorkout(workoutEntity.toWorkout())
            },
            onDeleteClick = { workoutEntity, position ->
                showDeleteConfirmationDialog(workoutEntity, position)
            },
            showEditButton = true,
            showDeleteButton = true
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
            viewModel.workouts.asFlow().collect { workouts ->
                adapter.setWorkouts(workouts)
            }
        }
    }

    private fun editWorkout(workout: Workout) {
        val action = WorkoutsFragmentDirections.actionWorkoutsFragmentToCreateWorkoutFragment(workout)
        findNavController().navigate(action)
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
                val workoutEntity = adapter.getWorkoutAt(position)
                showDeleteConfirmationDialog(workoutEntity, position)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.workoutsRecyclerView)
    }

    private fun showDeleteConfirmationDialog(workoutEntity: WorkoutEntity, position: Int) {
        DialogUtils.showDeleteConfirmationDialog(
            context = requireContext(),
            title = "Delete Workout",
            message = "Are you sure you want to delete this workout?",
            onConfirm = {
                viewModel.deleteWorkout(workoutEntity)
                adapter.notifyItemRemoved(position)
            },
            onCancel = {
                adapter.notifyItemChanged(position)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
