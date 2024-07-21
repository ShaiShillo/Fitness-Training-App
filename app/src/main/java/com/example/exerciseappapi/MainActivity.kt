package com.example.exerciseappapi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private var selectedTarget: String? = null
    private var selectedEquipment: String? = null
    private var searchText: String = ""
    private var isFiltered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        filterIcon = findViewById(R.id.filterIcon)
        noExercisesTextView = findViewById(R.id.noExercisesTextView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        filterIcon.setOnClickListener {
            if (isFiltered) {
                resetFilters()
            } else {
                showFilterBottomSheet()
            }
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

        targetSpinner.isEnabled = false
        targetSpinner.alpha = 0.5f
        equipmentSpinner.isEnabled = false
        equipmentSpinner.alpha = 0.5f

        // Create a custom ArrayAdapter that includes the hint as the first item
        val bodyPartAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf<String>()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.hint_color))
                }
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        bodyPartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bodyPartSpinner.adapter = bodyPartAdapter

        viewModel.bodyParts.observe(this, Observer { bodyParts ->
            val bodyPartNames = listOf(getString(R.string.select_body_part)) + bodyParts
            bodyPartAdapter.clear()
            bodyPartAdapter.addAll(bodyPartNames)
            bodyPartAdapter.notifyDataSetChanged()
        })

        bodyPartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedBodyPart = if (position == 0) null else bodyPartSpinner.selectedItem as String
                if (selectedBodyPart != null) {
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

        val targetAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf<String>()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).setTextColor(resources.getColor(R.color.hint_color))
                }
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        targetSpinner.adapter = targetAdapter

        viewModel.targets.observe(this, Observer { targets ->
            val targetNames = listOf(getString(R.string.select_target)) + targets
            targetAdapter.clear()
            targetAdapter.addAll(targetNames)
            targetAdapter.notifyDataSetChanged()
        })

        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTarget = if (position == 0) null else targetSpinner.selectedItem as String
                if (selectedTarget != null) {
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

        val equipmentAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf<String>()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).setTextColor(resources.getColor(R.color.hint_color))
                }
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        equipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        equipmentSpinner.adapter = equipmentAdapter

        viewModel.equipment.observe(this, Observer { equipment ->
            val equipmentNames = listOf(getString(R.string.select_equipment)) + equipment
            equipmentAdapter.clear()
            equipmentAdapter.addAll(equipmentNames)
            equipmentAdapter.notifyDataSetChanged()
        })

        equipmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedEquipment = if (position == 0) null else equipmentSpinner.selectedItem as String
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
        searchEditText.setText("")
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
