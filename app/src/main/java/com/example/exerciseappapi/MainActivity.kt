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
import android.widget.TextView
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
        bodyPartSpinner = findViewById(R.id.bodyPartSpinner)
        equipmentSpinner = findViewById(R.id.equipmentSpinner)
        targetSpinner = findViewById(R.id.targetSpinner)
        noExercisesTextView = findViewById(R.id.noExercisesTextView)

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

    private fun setupSpinners() {
        viewModel.bodyParts.observe(this, Observer { bodyParts ->
            val bodyPartNames = arrayOf("All") + bodyParts.map { it.name }
            bodyPartSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bodyPartNames)
        })

        viewModel.equipment.observe(this, Observer { equipment ->
            val equipmentNames = arrayOf("All") + equipment.map { it.name }
            equipmentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, equipmentNames)
        })

        viewModel.targets.observe(this, Observer { targets ->
            val targetNames = arrayOf("All") + targets.map { it.name }
            targetSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, targetNames)
        })
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
