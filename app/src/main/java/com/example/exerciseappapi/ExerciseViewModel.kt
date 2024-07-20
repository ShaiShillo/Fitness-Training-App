package com.example.exerciseappapi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val exerciseRepository: ExerciseRepository
    private val _filteredExercises = MutableLiveData<List<Exercise>>()
    val filteredExercises: LiveData<List<Exercise>> = _filteredExercises

    private val _bodyParts = MutableLiveData<List<String>>()
    val bodyParts: LiveData<List<String>> = _bodyParts

    private val _targets = MutableLiveData<List<String>>()
    val targets: LiveData<List<String>> = _targets

    private val _equipment = MutableLiveData<List<String>>()
    val equipment: LiveData<List<String>> = _equipment

    init {
        val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()
        val apiService = ApiClient.apiService
        exerciseRepository = ExerciseRepository(exerciseDao, apiService)
        fetchBodyParts()
        fetchExercises()
    }

    fun searchExercises(name: String, bodyPart: String?, target: String?, equipment: String?) {
        viewModelScope.launch {
            val exercises = exerciseRepository.getExercises(name, bodyPart, target, equipment)
            _filteredExercises.postValue(exercises)
        }
    }

    private fun fetchBodyParts() {
        viewModelScope.launch {
            val bodyParts = exerciseRepository.getBodyParts()
            _bodyParts.postValue(bodyParts)
        }
    }

    private fun fetchExercises() {
        viewModelScope.launch {
            exerciseRepository.fetchExercisesFromApi()
        }
    }

    fun loadTargets(bodyPart: String) {
        viewModelScope.launch {
            val targets = exerciseRepository.getTargetsByBodyPart(bodyPart)
            _targets.postValue(targets)
        }
    }

    fun loadEquipment(bodyPart: String, target: String) {
        viewModelScope.launch {
            val equipment = exerciseRepository.getEquipmentByBodyPartAndTarget(bodyPart, target)
            _equipment.postValue(equipment)
        }
    }
}


