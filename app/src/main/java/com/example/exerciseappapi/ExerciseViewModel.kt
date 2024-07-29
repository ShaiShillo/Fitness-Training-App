package com.example.exerciseappapi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isEditMode = MutableLiveData<Boolean>()
    val isEditMode: LiveData<Boolean> get() = _isEditMode

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    private val _workouts = MutableStateFlow<List<WorkoutEntity>>(emptyList())
    val workouts: StateFlow<List<WorkoutEntity>> = _workouts

    init {
        val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()
        val workoutDao = AppDatabase.getDatabase(application).workoutDao()
        val apiService = ApiClient.apiService
        val converters = Converters()
        exerciseRepository = ExerciseRepository(exerciseDao, workoutDao, apiService, converters)
        fetchBodyParts()
        fetchExercises()
        fetchAllEquipment()
        _isEditMode.value = false
    }

    fun fetchWorkouts() {
        viewModelScope.launch {
            exerciseRepository.getAllWorkouts().collect { workoutList ->
                _workouts.value = workoutList
            }
        }
    }

    fun addWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            exerciseRepository.addWorkout(workout)
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            exerciseRepository.deleteWorkout(workout)
        }
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            exerciseRepository.updateWorkout(workout)
        }
    }

    fun setExercises(exerciseList: List<Exercise>) {
        _exercises.value = exerciseList
    }

    fun loadExercises() {
        _isLoading.value = true
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { exerciseEntities ->
                _isLoading.postValue(false)
                val exerciseList = exerciseEntities.map { it }
                _exercises.postValue(exerciseList)
            }
        }
    }

    fun setEditMode(isEdit: Boolean) {
        _isEditMode.value = isEdit
    }

    private fun fetchBodyParts() {
        viewModelScope.launch {
            exerciseRepository.getBodyParts().collect { bodyParts ->
                _bodyParts.postValue(bodyParts)
            }
        }
    }

    private fun fetchExercises() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            exerciseRepository.fetchExercisesFromApi()
            exerciseRepository.getAllExercises().collect { exercises ->
                _filteredExercises.postValue(exercises)
                _isLoading.postValue(false)
            }
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
        fetchExercises() // Reset filters and fetch all exercises
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
        _isLoading.postValue(true)
        viewModelScope.launch {
            exerciseRepository.getExercises(name, bodyPart, target, equipment).collect { exercises ->
                _filteredExercises.postValue(exercises)
                _isLoading.postValue(false)
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseRepository.addExercise(exercise)
            loadExercises() // Reload exercises after adding a new one
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exercise)
            _filteredExercises.value = _filteredExercises.value?.filter { it.id != exercise.id }
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseRepository.updateExercise(exercise)
        }
    }
}
