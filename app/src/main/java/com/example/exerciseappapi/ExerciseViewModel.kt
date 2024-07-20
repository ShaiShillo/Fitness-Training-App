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

    init {
        val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()
        val apiService = ApiClient.apiService
        exerciseRepository = ExerciseRepository(exerciseDao, apiService)
        fetchExercises()
        refreshGifUrls()
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
}
