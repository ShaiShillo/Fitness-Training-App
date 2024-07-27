package com.example.exerciseappapi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val exerciseRepository: ExerciseRepository

    private val _bodyParts = MutableLiveData<List<String>>()
    val bodyParts: LiveData<List<String>> get() = _bodyParts

    private val _targets = MutableLiveData<List<String>>()
    val targets: LiveData<List<String>> get() = _targets

    private val _equipment = MutableLiveData<List<String>>()
    val equipment: LiveData<List<String>> get() = _equipment

    private val _filteredExercises = MutableLiveData<List<Exercise>>()
    val filteredExercises: LiveData<List<Exercise>> get() = _filteredExercises

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    init {
        val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()
        val apiService = ApiClient.apiService
        exerciseRepository = ExerciseRepository(exerciseDao, apiService)
        fetchBodyParts()
        fetchExercises()
        fetchAllEquipment()
    }

    private fun fetchBodyParts() {
        viewModelScope.launch {
            exerciseRepository.getBodyParts().collect { bodyParts ->
                _bodyParts.postValue(bodyParts)
            }
        }
    }

    private fun fetchExercises() {
        viewModelScope.launch {
            exerciseRepository.fetchExercisesFromApi()
        }
    }

    fun fetchAllEquipment() {
        viewModelScope.launch {
            exerciseRepository.getAllEquipment().collect { equipment ->
                _equipment.postValue(equipment)
            }
        }
    }

    fun resetFilters() {
        _searchQuery.value = ""
        // Reset other filters as needed
    }


    fun loadTargets(bodyPart: String) {
        viewModelScope.launch {
            exerciseRepository.getTargetsByBodyPart(bodyPart).collect { targets ->
                _targets.postValue(targets)
            }
        }
    }



    fun loadEquipment(bodyPart: String, target: String) {
        viewModelScope.launch {
            exerciseRepository.getEquipmentByBodyPartAndTarget(bodyPart, target).collect { equipment ->
                _equipment.postValue(equipment)
            }
        }
    }

    fun searchExercises(name: String, bodyPart: String?, target: String?, equipment: String?) {
        viewModelScope.launch {
            exerciseRepository.getExercises(name, bodyPart, target, equipment).collect { exercises ->
                _filteredExercises.postValue(exercises)
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseRepository.addExercise(exercise)
        }
    }
}
