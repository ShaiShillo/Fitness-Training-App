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

    private val _bodyParts = MutableLiveData<List<BodyPart>>()
    val bodyParts: LiveData<List<BodyPart>> = _bodyParts

    private val _equipment = MutableLiveData<List<Equipment>>()
    val equipment: LiveData<List<Equipment>> = _equipment

    private val _targets = MutableLiveData<List<Target>>()
    val targets: LiveData<List<Target>> = _targets

    init {
        val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()
        val apiService = ApiClient.apiService
        exerciseRepository = ExerciseRepository(exerciseDao, apiService)
        fetchExercises()
        refreshGifUrls()
        fetchCategories()
    }

    private fun fetchExercises() {
        viewModelScope.launch {
            exerciseRepository.fetchExercisesFromApi()
        }
    }

    private fun refreshGifUrls() {
        viewModelScope.launch {
            exerciseRepository.refreshGifUrls()
        }
    }

    fun searchExercises(name: String, bodyPart: String?, equipment: String?, target: String?) {
        viewModelScope.launch {
            val exercises = exerciseRepository.getExercises(name, bodyPart, equipment, target)
            _filteredExercises.postValue(exercises)
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            exerciseRepository.fetchCategoriesFromApi()
            _bodyParts.postValue(exerciseRepository.getBodyParts())
            _equipment.postValue(exerciseRepository.getEquipment())
            _targets.postValue(exerciseRepository.getTargets())
        }
    }
}
