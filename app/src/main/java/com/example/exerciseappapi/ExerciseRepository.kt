package com.example.exerciseappapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepository(private val exerciseDao: ExerciseDao, private val apiService: ExerciseApiService) {

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    suspend fun fetchExercisesFromApi() {
        withContext(Dispatchers.IO) {
            try {
                val fetchedExercises = apiService.getExercises()
                val exerciseEntities = fetchedExercises.map { ExerciseEntity.fromExercise(it) }
                exerciseDao.insertAllExercises(exerciseEntities)
                _exercises.postValue(exerciseEntities.map { it.toExercise() })
            } catch (e: Exception) {
                _exercises.postValue(emptyList())
            }
        }
    }

    suspend fun getBodyParts(): List<String> {
        return withContext(Dispatchers.IO) {
            exerciseDao.getAllBodyParts()
        }
    }

    suspend fun getTargetsByBodyPart(bodyPart: String): List<String> {
        return withContext(Dispatchers.IO) {
            exerciseDao.getTargetsByBodyPart(bodyPart)
        }
    }

    suspend fun getEquipmentByBodyPartAndTarget(bodyPart: String, target: String): List<String> {
        return withContext(Dispatchers.IO) {
            exerciseDao.getEquipmentByBodyPartAndTarget(bodyPart, target)
        }
    }

    suspend fun getExercises(name: String, bodyPart: String?, target: String?, equipment: String?): List<Exercise> {
        return withContext(Dispatchers.IO) {
            val allExercises = exerciseDao.getAllExercises()
            val filteredExercises = allExercises.filter {
                (name.isEmpty() || it.name.contains(name, ignoreCase = true)) &&
                        (bodyPart == null || it.bodyPart.equals(bodyPart, ignoreCase = true)) &&
                        (target == null || it.target.equals(target, ignoreCase = true)) &&
                        (equipment == null || it.equipment.equals(equipment, ignoreCase = true))
            }
            filteredExercises.map { it.toExercise() }
        }
    }
}
