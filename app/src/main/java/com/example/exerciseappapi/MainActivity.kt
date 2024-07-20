package com.example.exerciseappapi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var searchEditText: EditText
    private lateinit var bodyPartSpinner: Spinner
    private lateinit var equipmentSpinner: Spinner
    private lateinit var targetSpinner: Spinner

    private var selectedBodyPart: String? = null
    private var selectedEquipment: String? = null
    private var selectedTarget: String? = null
    private var searchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        bodyPartSpinner = findViewById(R.id.bodyPartSpinner)
        equipmentSpinner = findViewById(R.id.equipmentSpinner)
        targetSpinner = findViewById(R.id.targetSpinner)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSpinners()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                filterExercises()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        bodyPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedBodyPart = if (position == 0) null else parent.getItemAtPosition(position).toString()
                filterExercises()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        equipmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedEquipment = if (position == 0) null else parent.getItemAtPosition(position).toString()
                filterExercises()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTarget = if (position == 0) null else parent.getItemAtPosition(position).toString()
                filterExercises()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        viewModel.filteredExercises.observe(this, Observer { exercises ->
            adapter = ExerciseAdapter(exercises) { exercise ->
                showExerciseDetails(exercise)
            }
            recyclerView.adapter = adapter
        })
    }

    private fun setupSpinners() {
        val bodyParts = arrayOf("All", "back", "cardio", "chest", "lower arms", "lower legs", "neck", "shoulders", "upper arms", "upper legs", "waist")
        val equipment = arrayOf("All", "assisted", "band", "barbell", "body weight", "bosu ball", "cable", "dumbbell", "elliptical machine", "ez barbell", "hammer", "kettlebell", "leverage machine", "medicine ball", "olympic barbell", "resistance band", "roller", "rope", "skierg machine", "sled machine", "smith machine", "stability ball", "stationary bike", "stepmill machine", "tire", "trap bar", "upper body ergometer", "weighted", "wheel roller")
        val targets = arrayOf("All", "abductors", "abs", "adductors", "biceps", "calves", "cardiovascular system", "delts", "forearms", "glutes", "hamstrings", "lats", "levator scapulae", "pectorals", "quads", "serratus anterior", "spine", "traps", "triceps", "upper back")

        bodyPartSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bodyParts)
        equipmentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, equipment)
        targetSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, targets)
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
