package com.example.exerciseappapi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exerciseappapi.databinding.BottomSheetFilterBinding
import com.example.exerciseappapi.databinding.FragmentMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainFragment : Fragment() {

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ExerciseAdapter
    private lateinit var filterIcon: ImageView

    private var selectedBodyPart: String? = null
    private var selectedTarget: String? = null
    private var selectedEquipment: String? = null
    private var searchText: String = ""
    private var isFiltered: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        filterIcon = binding.filterIcon

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        setupObservers()
        setupSearch()
        setupFilterIcon()
        setupAddExerciseButton()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.filteredExercises.observe(viewLifecycleOwner, Observer { exercises ->
            if (exercises.isEmpty()) {
                binding.noExercisesTextView.visibility = View.VISIBLE
                binding.addExerciseButton.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.noExercisesTextView.visibility = View.GONE
                binding.addExerciseButton.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter = ExerciseAdapter(exercises) { exercise ->
                    showExerciseDetails(exercise)
                }
                binding.recyclerView.adapter = adapter
            }
        })
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
        filterIcon.setOnClickListener {
            if (isFiltered) {
                resetFilters()
            } else {
                showFilterBottomSheet()
            }
        }
    }

    private fun setupAddExerciseButton() {
        binding.addExerciseButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addExerciseFragment)
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
            filterIcon.setImageResource(R.drawable.ic_unfilter)
        }

        bottomSheetDialog.show()
    }

    private fun filterExercises() {
        viewModel.searchExercises(searchText, selectedBodyPart, selectedTarget, selectedEquipment)
    }

    private fun resetFilters() {
        selectedBodyPart = null
        selectedTarget = null
        selectedEquipment = null
        searchText = ""
        binding.searchEditText.setText("")
        isFiltered = false
        filterIcon.setImageResource(R.drawable.ic_filter)
        viewModel.searchExercises(searchText, selectedBodyPart, selectedTarget, selectedEquipment)
    }

    private fun showExerciseDetails(exercise: Exercise) {
        val intent = Intent(requireContext(), ExerciseDetailActivity::class.java).apply {
            putExtra("exercise", exercise)
        }
        startActivity(intent)
    }
}
