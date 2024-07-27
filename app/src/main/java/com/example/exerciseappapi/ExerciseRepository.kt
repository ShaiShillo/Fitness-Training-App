package com.example.exerciseappapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ExerciseRepository(private val exerciseDao: ExerciseDao, private val apiService: ExerciseApiService) {

    suspend fun fetchExercisesFromApi() {
        withContext(Dispatchers.IO) {
            try {
                val fetchedExercises = apiService.getExercises()
                val exerciseEntities = fetchedExercises.map { ExerciseEntity.fromExercise(it) }
                exerciseDao.insertAllExercises(exerciseEntities)
            } catch (e: Exception) {
                // Handle the error
            }
        }
    }

    fun getBodyParts(): Flow<List<String>> = flow {
        val bodyParts = withContext(Dispatchers.IO) {
            exerciseDao.getAllBodyParts()
        }
        emit(bodyParts)
    }

    fun getTargetsByBodyPart(bodyPart: String): Flow<List<String>> = flow {
        val targets = withContext(Dispatchers.IO) {
            exerciseDao.getTargetsByBodyPart(bodyPart)
        }
        emit(targets)
    }

    fun getEquipmentByBodyPartAndTarget(bodyPart: String, target: String): Flow<List<String>> = flow {
        val equipment = withContext(Dispatchers.IO) {
            exerciseDao.getEquipmentByBodyPartAndTarget(bodyPart, target)
        }
        emit(equipment)
    }

    fun getAllEquipment(): Flow<List<String>> = flow {
        val equipmentList = withContext(Dispatchers.IO) {
            exerciseDao.getAllEquipment()
        }
        emit(equipmentList)
    }

    fun getExercises(name: String, bodyPart: String?, target: String?, equipment: String?): Flow<List<Exercise>> = flow {
        val allExercises = withContext(Dispatchers.IO) {
            exerciseDao.getAllExercises()
        }
        val filteredExercises = allExercises.filter {
            (name.isEmpty() || it.name.contains(name, ignoreCase = true)) &&
                    (bodyPart == null || it.bodyPart.equals(bodyPart, ignoreCase = true)) &&
                    (target == null || it.target.equals(target, ignoreCase = true)) &&
                    (equipment == null || it.equipment.equals(equipment, ignoreCase = true))
        }
        emit(filteredExercises.map { it.toExercise() })
    }

    suspend fun addExercise(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            exerciseDao.insertExercise(ExerciseEntity.fromExercise(exercise))
        }
    }
}
