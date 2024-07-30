package com.example.exerciseappapi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.BottomSheetFilterBinding
import com.example.exerciseappapi.databinding.FragmentMainBinding
import com.example.exerciseappapi.utils.DialogUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainFragment : Fragment() {

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ExerciseAdapter

    private var selectedBodyPart: String? = null
    private var selectedTarget: String? = null
    private var selectedEquipment: String? = null
    private var searchText: String = ""
    private var isFiltered: Boolean = false
    private var isSelectingExercises: Boolean = false
    private val selectedExercises = mutableListOf<Exercise>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        isSelectingExercises = arguments?.getBoolean("isSelectingExercises") ?: false

        // Handle the back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isSelectingExercises) {
                        findNavController().navigateUp()
                    } else {
                        requireActivity().finish()
                    }
                }
            }
        )

        // Retrieve existing selected exercises
        val existingSelectedExercises = arguments?.getParcelableArrayList<Exercise>("selectedExercises")
        if (existingSelectedExercises != null) {
            selectedExercises.addAll(existingSelectedExercises)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        setupObservers()
        setupSearch()
        setupFilterIcon()
        setupAddExerciseIcon()
        setupNoExercisesCreateNewButton()
        setupListeners()
        setupSwipeToDelete()

        // Initialize the adapter before setting it to the RecyclerView
        adapter = ExerciseAdapter(
            mutableListOf(),
            onItemClick = { exercise ->
                showExerciseDetails(exercise)
            },
            onEditClick = { exercise ->
                editExercise(exercise)
            },
            isSelectingExercises = isSelectingExercises,
            onExerciseSelected = { exercise, isChecked ->
                if (isChecked) {
                    selectedExercises.add(exercise)
                } else {
                    selectedExercises.remove(exercise)
                }
            }
        )

        binding.recyclerView.adapter = adapter

        if (isSelectingExercises) {
            setupExerciseSelection()
        }

        viewModel.loadExercises() // Load exercises when the fragment starts

        return binding.root
    }

    private fun setupExerciseSelection() {
        binding.fabConfirmSelection.visibility = View.VISIBLE
        binding.fabConfirmSelection.setOnClickListener {
            // Log selected exercises
            Log.d("MainFragment", "Selected exercises: ${selectedExercises.size}")

            // Pass selected exercises back to the previous fragment
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedExercises", selectedExercises.toList())
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.filteredExercises.observe(viewLifecycleOwner, Observer { exercises ->
            binding.loadingProgressBar.visibility = View.GONE
            Log.d("MainFragment", "Filtered exercises: ${exercises.size}")
            if (exercises.isEmpty()) {
                binding.noExercisesTextView.visibility = View.VISIBLE
                binding.noExercisesCreateNewButton.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.noExercisesTextView.visibility = View.GONE
                binding.noExercisesCreateNewButton.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.setExercises(exercises)
            }
        })

        // Show the loading indicator while data is being fetched
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun setupListeners() {
        parentFragmentManager.setFragmentResultListener("addExerciseResult", this) { _, bundle ->
            val shouldReset = bundle.getBoolean("shouldReset")
            if (shouldReset) {
                clearSearchAndFilters()
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("exerciseAdded")
            ?.observe(viewLifecycleOwner) { exerciseAdded ->
                if (exerciseAdded == true) {
                    viewModel.loadExercises()
                }
            }
    }

    private fun clearSearchAndFilters() {
        binding.searchEditText.text.clear()
        viewModel.resetFilters()
        // Add any additional logic to reset filters if needed
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                filterExercises()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilterIcon() {
        binding.filterIcon.setOnClickListener {
            if (isFiltered) {
                resetFilters()
            } else {
                showFilterBottomSheet()
            }
        }
    }

    private fun setupAddExerciseIcon() {
        binding.addExerciseIcon.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddExerciseFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun setupNoExercisesCreateNewButton() {
        binding.noExercisesCreateNewButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddExerciseFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetFilterBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        val bodyPartSpinner: Spinner = bottomSheetBinding.bodyPartSpinner
        val targetSpinner: Spinner = bottomSheetBinding.targetSpinner
        val equipmentSpinner: Spinner = bottomSheetBinding.equipmentSpinner

        targetSpinner.isEnabled = false
        targetSpinner.alpha = 0.5f
        equipmentSpinner.isEnabled = false
        equipmentSpinner.alpha = 0.5f

        viewModel.bodyParts.observe(viewLifecycleOwner) { bodyParts ->
            val bodyPartNames = listOf(getString(R.string.select_body_part)) + bodyParts
            val bodyPartAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bodyPartNames)
            bodyPartSpinner.adapter = bodyPartAdapter
        }

        bodyPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBodyPart = bodyPartSpinner.selectedItem as String
                if (selectedBodyPart != getString(R.string.select_body_part)) {
                    viewModel.loadTargets(selectedBodyPart!!)
                    targetSpinner.isEnabled = true
                    targetSpinner.alpha = 1.0f
                } else {
                    targetSpinner.isEnabled = false
                    targetSpinner.alpha = 0.5f
                    equipmentSpinner.isEnabled = false
                    equipmentSpinner.alpha = 0.5f
                    selectedTarget = null
                    selectedEquipment = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.targets.observe(viewLifecycleOwner) { targets ->
            val targetNames = listOf(getString(R.string.select_target)) + targets
            val targetAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, targetNames)
            targetSpinner.adapter = targetAdapter
        }

        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTarget = targetSpinner.selectedItem as String
                if (selectedTarget != getString(R.string.select_target)) {
                    viewModel.loadEquipment(selectedBodyPart!!, selectedTarget!!)
                    equipmentSpinner.isEnabled = true
                    equipmentSpinner.alpha = 1.0f
                } else {
                    equipmentSpinner.isEnabled = false
                    equipmentSpinner.alpha = 0.5f
                    selectedEquipment = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.equipment.observe(viewLifecycleOwner) { equipment ->
            val equipmentNames = listOf(getString(R.string.select_equipment)) + equipment
            val equipmentAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, equipmentNames)
            equipmentSpinner.adapter = equipmentAdapter
        }

        equipmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedEquipment = equipmentSpinner.selectedItem as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bottomSheetBinding.applyFiltersButton.setOnClickListener {
            filterExercises()
            bottomSheetDialog.dismiss()
            isFiltered = true
            binding.filterIcon.setImageResource(R.drawable.ic_unfilter)
        }

        bottomSheetDialog.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadExercises()
    }

    private fun filterExercises() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        Log.d("MainFragment", "Filter - searchText: $searchText, bodyPart: $selectedBodyPart, target: $selectedTarget, equipment: $selectedEquipment")
        viewModel.searchExercises(searchText, selectedBodyPart, selectedTarget, selectedEquipment)
    }

    private fun resetFilters() {
        selectedBodyPart = null
        selectedTarget = null
        selectedEquipment = null
        searchText = ""
        binding.searchEditText.setText("")
        isFiltered = false
        binding.filterIcon.setImageResource(R.drawable.ic_filter)
        filterExercises()
    }

    private fun showExerciseDetails(exercise: Exercise) {
        val action = MainFragmentDirections.actionMainFragmentToExerciseDetailFragment(exercise)
        findNavController().navigate(action)
    }

    private fun editExercise(exercise: Exercise) {
        val action = MainFragmentDirections.actionMainFragmentToAddExerciseFragment(exercise)
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

                if (exercise.createdByUser) {
                    showDeleteConfirmationDialog(exercise, position)
                } else {
                    adapter.notifyItemChanged(position)
                    // Optionally show a message that only user-created exercises can be deleted
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
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
}
