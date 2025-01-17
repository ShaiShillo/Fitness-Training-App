package com.example.exerciseappapi.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.models.Exercise
import com.example.exerciseappapi.ui.adapters.ExerciseAdapter
import com.example.exerciseappapi.viewmodels.ViewModel
import com.example.exerciseappapi.R
import com.example.exerciseappapi.models.Workout
import com.example.exerciseappapi.models.WorkoutEntity
import com.example.exerciseappapi.databinding.FragmentCreateWorkoutBinding
import com.example.exerciseappapi.utils.DialogUtils
import java.util.Date

class CreateWorkoutFragment : Fragment() {

    private var _binding: FragmentCreateWorkoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModel by viewModels()
    private val selectedExercises = mutableListOf<Exercise>()
    private lateinit var adapter: ExerciseAdapter
    private var workoutToEdit: Workout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObserver()

        arguments?.let {
            workoutToEdit = CreateWorkoutFragmentArgs.fromBundle(it).workout
        }

        if (workoutToEdit != null) {
            populateWorkoutDetails(workoutToEdit!!)
        }

        binding.fabAddExercise.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("isSelectingExercises", true)
                putParcelableArrayList("selectedExercises", ArrayList(selectedExercises))
            }
            findNavController().navigate(R.id.action_createWorkoutFragment_to_mainFragment, bundle)
        }

        binding.saveWorkoutButton.setOnClickListener {
            saveWorkout()
        }
    }

    private fun setupRecyclerView() {
        binding.exercisesRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ExerciseAdapter(
            selectedExercises,
            onItemClick = { exercise ->
                showExerciseDetails(exercise)
            },
            onEditClick = { exercise -> editExercise(exercise) },
            isSelectingExercises = true,
            showCheckboxes = false // Ensure checkboxes are hidden
        )
        binding.exercisesRecyclerView.adapter = adapter
        setupSwipeToDelete()
    }

    private fun showExerciseDetails(exercise: Exercise) {
        val action = CreateWorkoutFragmentDirections.actionCreateWorkoutFragmentToExerciseDetailFragment(exercise)
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
                val exercise = adapter.getExerciseAt(position)
                showDeleteConfirmationDialog(exercise, position)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.exercisesRecyclerView)
    }

    private fun showDeleteConfirmationDialog(exercise: Exercise, position: Int) {
        DialogUtils.showDeleteConfirmationDialog(
            context = requireContext(),
            title = "Delete Exercise",
            message = "Are you sure you want to delete this exercise?",
            onConfirm = {
                selectedExercises.removeAt(position)
                adapter.notifyItemRemoved(position)
                viewModel.deleteExercise(exercise)
            },
            onCancel = {
                adapter.notifyItemChanged(position)
            }
        )
    }

    private fun setupObserver() {
        // Handle the received exercises from MainFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<List<Exercise>>("selectedExercises")
            ?.observe(viewLifecycleOwner) { exercises ->
                if (exercises != null) {
                    selectedExercises.clear()
                    selectedExercises.addAll(exercises)
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun populateWorkoutDetails(workout: Workout) {
        binding.workoutNameEditText.setText(workout.workoutName)
        selectedExercises.clear()
        selectedExercises.addAll(workout.exercises)
        adapter.notifyDataSetChanged()
    }

    private fun saveWorkout() {
        val workoutName = binding.workoutNameEditText.text.toString()
        if (workoutName.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a workout name", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedExercises.isEmpty()) {
            Toast.makeText(requireContext(), "Please add exercises to the workout", Toast.LENGTH_SHORT).show()
            return
        }

        val workoutEntity = WorkoutEntity(
            id = workoutToEdit?.id ?: 0, // Use the existing ID if editing
            workoutName = workoutName,
            creationDate = workoutToEdit?.creationDate ?: Date(), // Use existing creation date if editing
            exercises = selectedExercises.map { it }
        )

        if (workoutToEdit == null) {
            viewModel.addWorkout(workoutEntity)
        } else {
            viewModel.updateWorkout(workoutEntity)
        }

        // Notify WorkoutsFragment of the updated workout
        findNavController().previousBackStackEntry?.savedStateHandle?.set("workoutAdded", true)
        findNavController().navigateUp()
    }

    private fun editExercise(exercise: Exercise) {
        val action = CreateWorkoutFragmentDirections.actionCreateWorkoutFragmentToAddExerciseFragment(exercise)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
