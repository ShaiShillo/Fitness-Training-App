package com.example.exerciseappapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {

    private val _filteredExercises = MutableLiveData<List<Exercise>>()
    val filteredExercises: LiveData<List<Exercise>> = _filteredExercises

    fun searchExercises(name: String, bodyPart: String?, equipment: String?, target: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allExercises = ApiClient.apiService.getExercises(limit = 0)
                val filteredExercises = allExercises.filter {
                    (name.isEmpty() || it.name.contains(name, ignoreCase = true)) &&
                            (bodyPart == null || it.bodyPart.equals(bodyPart, ignoreCase = true)) &&
                            (equipment == null || it.equipment.equals(equipment, ignoreCase = true)) &&
                            (target == null || it.target.equals(target, ignoreCase = true))
                }
                _filteredExercises.postValue(filteredExercises)
            } catch (e: Exception) {
                _filteredExercises.postValue(emptyList())
            }
        }
    }
}
