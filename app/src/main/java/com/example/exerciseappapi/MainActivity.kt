package com.example.exerciseappapi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exerciseappapi.databinding.ActivityMainBinding
import com.example.exerciseappapi.databinding.BottomSheetFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ExerciseAdapter
    private lateinit var filterIcon: ImageView
    private lateinit var noExercisesTextView: TextView

    private var selectedBodyPart: String? = null
    private var selectedTarget: String? = null
    private var selectedEquipment: String? = null
    private var searchText: String = ""
    private var isFiltered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        filterIcon = binding.filterIcon
        noExercisesTextView = binding.noExercisesTextView

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        filterIcon.setOnClickListener {
            if (isFiltered) {
                resetFilters()
            } else {
                showFilterBottomSheet()
            }
        }

        setupObservers()

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                filterExercises()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        viewModel.filteredExercises.observe(this) { exercises ->
            if (exercises.isEmpty()) {
                noExercisesTextView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                noExercisesTextView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter = ExerciseAdapter(exercises) { exercise ->
                    showExerciseDetails(exercise)
                }
                binding.recyclerView.adapter = adapter
            }
        }
    }

    private fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val binding = BottomSheetFilterBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        val bodyPartSpinner: Spinner = binding.bodyPartSpinner
        val targetSpinner: Spinner = binding.targetSpinner
        val equipmentSpinner: Spinner = binding.equipmentSpinner
        val applyFiltersButton: Button = binding.applyFiltersButton

        targetSpinner.isEnabled = false
        targetSpinner.alpha = 0.5f
        equipmentSpinner.isEnabled = false
        equipmentSpinner.alpha = 0.5f

        viewModel.bodyParts.observe(this) { bodyParts ->
            val bodyPartNames = listOf(getString(R.string.select_body_part)) + bodyParts
            val bodyPartAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, bodyPartNames)
            bodyPartSpinner.adapter = bodyPartAdapter
        }

        bodyPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBodyPart = bodyPartSpinner.selectedItem as String
                if (selectedBodyPart != getString(R.string.select_body_part)) {
                    targetSpinner.isEnabled = true
                    targetSpinner.alpha = 1.0f
                    viewModel.loadTargets(selectedBodyPart!!)
                } else {
                    targetSpinner.isEnabled = false
                    targetSpinner.alpha = 0.5f
                    equipmentSpinner.isEnabled = false
                    equipmentSpinner.alpha = 0.5f
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.targets.observe(this) { targets ->
            val targetNames = listOf(getString(R.string.select_target)) + targets
            val targetAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, targetNames)
            targetSpinner.adapter = targetAdapter
        }

        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTarget = targetSpinner.selectedItem as String
                if (selectedTarget != getString(R.string.select_target)) {
                    equipmentSpinner.isEnabled = true
                    equipmentSpinner.alpha = 1.0f
                    viewModel.loadEquipment(selectedBodyPart!!, selectedTarget!!)
                } else {
                    equipmentSpinner.isEnabled = false
                    equipmentSpinner.alpha = 0.5f
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.equipment.observe(this) { equipment ->
            val equipmentNames = listOf(getString(R.string.select_equipment)) + equipment
            val equipmentAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, equipmentNames)
            equipmentSpinner.adapter = equipmentAdapter
        }

        equipmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedEquipment = equipmentSpinner.selectedItem as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        applyFiltersButton.setOnClickListener {
            filterExercises()
            bottomSheetDialog.dismiss()
            isFiltered = true
            filterIcon.setImageResource(R.drawable.ic_unfilter) // Change icon to unfilter
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
        filterIcon.setImageResource(R.drawable.ic_filter) // Change icon back to filter
        viewModel.searchExercises(searchText, selectedBodyPart, selectedTarget, selectedEquipment) // Reload all exercises
    }

    private fun showExerciseDetails(exercise: Exercise) {
        val intent = Intent(this, ExerciseDetailActivity::class.java).apply {
            putExtra("exercise", exercise)
        }
        startActivity(intent)
    }
}
