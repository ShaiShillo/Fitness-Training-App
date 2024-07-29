package com.example.exerciseappapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.FragmentCreateWorkoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Date

class CreateWorkoutFragment : Fragment() {

    private var _binding: FragmentCreateWorkoutBinding? = null
    private val binding get() = _binding!!
    private val selectedExercises = mutableListOf<Exercise>()
    private lateinit var adapter: ExerciseAdapter

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

        binding.fabAddExercise.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("isSelectingExercises", true)
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
            onItemClick = {}, // No-op lambda for item click
            onEditClick = { exercise -> editExercise(exercise) },  // Make the edit button functional
            isSelectingExercises = false // Ensure the checkboxes are hidden
        )
        binding.exercisesRecyclerView.adapter = adapter
        setupSwipeToDelete()
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Exercise")
            .setMessage("Are you sure you want to delete this exercise?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyItemChanged(position)
            }
            .setPositiveButton("Delete") { dialog, _ ->
                selectedExercises.removeAt(position)
                adapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .show()
    }

    private fun setupObserver() {
        // Handle the received exercises from MainFragment
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<List<Exercise>>("selectedExercises")
            ?.observe(viewLifecycleOwner, Observer { exercises ->
                Log.d("CreateWorkoutFragment", "Received exercises: ${exercises?.size ?: 0}")
                if (exercises != null) {
                    selectedExercises.clear()
                    selectedExercises.addAll(exercises)
                    Log.d("CreateWorkoutFragment", "Selected exercises after update: ${selectedExercises.size}")
                    adapter.notifyDataSetChanged()
                }
            })
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

        val workout = Workout(
            workoutName = workoutName,
            creationDate = Date(),
            exercises = selectedExercises
        )

        // Pass the new workout back to the WorkoutsFragment
        val bundle = Bundle().apply {
            putParcelable("newWorkout", workout)
        }
        findNavController().previousBackStackEntry?.savedStateHandle?.set("newWorkoutBundle", bundle)
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
