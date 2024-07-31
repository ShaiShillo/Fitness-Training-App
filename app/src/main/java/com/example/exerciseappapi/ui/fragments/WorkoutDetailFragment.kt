package com.example.exerciseappapi.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exerciseappapi.models.Exercise
import com.example.exerciseappapi.ui.adapters.ExerciseAdapter
import com.example.exerciseappapi.viewmodels.ViewModel
import com.example.exerciseappapi.databinding.FragmentWorkoutDetailBinding

class WorkoutDetailFragment : Fragment() {

    private var _binding: FragmentWorkoutDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModel by viewModels()
    private val args: WorkoutDetailFragmentArgs by navArgs()
    private lateinit var adapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workout = args.workout
        workout?.let {
            binding.workoutNameTextView.text = it.workoutName
            binding.workoutDateTextView.text = it.creationDate.toString()
            setupRecyclerView(it.exercises)
        }

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            adapter.setExercises(exercises)
        }
    }

    private fun setupRecyclerView(exercises: List<Exercise>) {
        binding.exercisesRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ExerciseAdapter(
            exercises.toMutableList(),
            onItemClick = { exercise ->
                val action = WorkoutDetailFragmentDirections.actionWorkoutDetailFragmentToExerciseDetailFragment(exercise)
                findNavController().navigate(action)
            },
            onEditClick = {}, // No-op lambda for edit click
            isSelectingExercises = false,
            showEditButton = false // Pass the flag as false to hide the edit button
        )
        binding.exercisesRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
