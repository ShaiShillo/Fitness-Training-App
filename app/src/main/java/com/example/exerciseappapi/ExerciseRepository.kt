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
                val fetchedExercises = apiService.getExercises(limit = 0)
                val exerciseEntities = fetchedExercises.map { ExerciseEntity.fromExercise(it) }
                exerciseDao.insertAllExercises(exerciseEntities)
                _exercises.postValue(exerciseEntities.map { it.toExercise() })
            } catch (e: Exception) {
                _exercises.postValue(emptyList())
            }
        }
    }

    suspend fun getExercises(name: String, bodyPart: String?, equipment: String?, target: String?): List<Exercise> {
        return withContext(Dispatchers.IO) {
            val allExercises = exerciseDao.getAllExercises()
            val filteredExercises = allExercises.filter {
                (name.isEmpty() || it.name.contains(name, ignoreCase = true)) &&
                        (bodyPart == null || it.bodyPart.equals(bodyPart, ignoreCase = true)) &&
                        (equipment == null || it.equipment.equals(equipment, ignoreCase = true)) &&
                        (target == null || it.target.equals(target, ignoreCase = true))
            }
            filteredExercises.map { it.toExercise() }
        }
    }

    suspend fun refreshGifUrls() {
        withContext(Dispatchers.IO) {
            val exercises = exerciseDao.getAllExercises()
            exercises.forEach { exercise ->
                try {
                    val updatedExercise = apiService.getExerciseById(exercise.id)
                    exercise.gifUrl = updatedExercise.gifUrl
                    exerciseDao.updateExercise(exercise)
                } catch (e: Exception) {
                    // Handle the error appropriately
                }
            }
        }
    }

    suspend fun fetchCategoriesFromApi() {
        withContext(Dispatchers.IO) {
            try {
                val bodyParts = apiService.getBodyParts().map { BodyPart(it) }
                exerciseDao.insertAllBodyParts(bodyParts)

                val equipment = apiService.getEquipment().map { Equipment(it) }
                exerciseDao.insertAllEquipment(equipment)

                val targets = apiService.getTargets().map { Target(it) }
                exerciseDao.insertAllTargets(targets)
            } catch (e: Exception) {
                // Handle the error appropriately
            }
        }
    }

    suspend fun getBodyParts(): List<BodyPart> {
        return withContext(Dispatchers.IO) {
            exerciseDao.getAllBodyParts()
        }
    }

    suspend fun getEquipment(): List<Equipment> {
        return withContext(Dispatchers.IO) {
            exerciseDao.getAllEquipment()
        }
    }

    suspend fun getTargets(): List<Target> {
        return withContext(Dispatchers.IO) {
            exerciseDao.getAllTargets()
        }
    }
}
