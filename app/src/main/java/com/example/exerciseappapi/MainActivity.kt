package com.example.exerciseappapi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var searchEditText: EditText
    private lateinit var filterIcon: ImageView
    private lateinit var noExercisesTextView: TextView

    private var selectedBodyPart: String? = null
    private var selectedEquipment: String? = null
    private var selectedTarget: String? = null
    private var searchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        filterIcon = findViewById(R.id.filterIcon)
        noExercisesTextView = findViewById(R.id.noExercisesTextView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        filterIcon.setOnClickListener {
            showFilterBottomSheet()
        }

        setupObservers()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                filterExercises()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        viewModel.filteredExercises.observe(this, Observer { exercises ->
            if (exercises.isEmpty()) {
                noExercisesTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                noExercisesTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter = ExerciseAdapter(exercises) { exercise ->
                    showExerciseDetails(exercise)
                }
                recyclerView.adapter = adapter
            }
        })
    }

    private fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        bottomSheetDialog.setContentView(view)

        val bodyPartSpinner: Spinner = view.findViewById(R.id.bodyPartSpinner)
        val targetSpinner: Spinner = view.findViewById(R.id.targetSpinner)
        val equipmentSpinner: Spinner = view.findViewById(R.id.equipmentSpinner)
        val applyFiltersButton: Button = view.findViewById(R.id.applyFiltersButton)

        viewModel.bodyParts.observe(this, Observer { bodyParts ->
            val bodyPartNames = arrayOf("All") + bodyParts.map { it.name }
            val bodyPartAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bodyPartNames)
            bodyPartSpinner.adapter = bodyPartAdapter
        })

        viewModel.equipment.observe(this, Observer { equipment ->
            val equipmentNames = arrayOf("All") + equipment.map { it.name }
            val equipmentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, equipmentNames)
            equipmentSpinner.adapter = equipmentAdapter
        })

        viewModel.targets.observe(this, Observer { targets ->
            val targetNames = arrayOf("All") + targets.map { it.name }
            val targetAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, targetNames)
            targetSpinner.adapter = targetAdapter
        })

        applyFiltersButton.setOnClickListener {
            selectedBodyPart = if (bodyPartSpinner.selectedItemPosition == 0) null else bodyPartSpinner.selectedItem.toString()
            selectedTarget = if (targetSpinner.selectedItemPosition == 0) null else targetSpinner.selectedItem.toString()
            selectedEquipment = if (equipmentSpinner.selectedItemPosition == 0) null else equipmentSpinner.selectedItem.toString()

            filterExercises()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun filterExercises() {
        viewModel.searchExercises(searchText, selectedBodyPart, selectedEquipment, selectedTarget)
    }

    private fun showExerciseDetails(exercise: Exercise) {
        val intent = Intent(this, ExerciseDetailActivity::class.java).apply {
            putExtra("exercise", exercise)
        }
        startActivity(intent)
    }
}
