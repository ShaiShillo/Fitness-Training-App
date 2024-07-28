package com.example.exerciseappapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val apiService: ExerciseApiService,
    private val converters: Converters
) {

    suspend fun fetchExercisesFromApi() {
        withContext(Dispatchers.IO) {
            try {
                val fetchedExercises = apiService.getExercises()
                val exerciseEntities = fetchedExercises.map { converters.exerciseToEntity(it) }
                exerciseDao.insertAllExercises(exerciseEntities)
            } catch (e: Exception) {
                // TODO: Handle the error
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
            exerciseDao.getAllExercises().map { converters.entityToExercise(it) }
        }
        val filteredExercises = allExercises.filter {
            (name.isEmpty() || it.name.contains(name, ignoreCase = true)) &&
                    (bodyPart == null || it.bodyPart.equals(bodyPart, ignoreCase = true)) &&
                    (target == null || it.target.equals(target, ignoreCase = true)) &&
                    (equipment == null || it.equipment.equals(equipment, ignoreCase = true))
        }
        emit(filteredExercises)
    }

    fun getAllExercises(): Flow<List<Exercise>> = flow {
        val allExercises = withContext(Dispatchers.IO) {
            exerciseDao.getAllExercises().map { converters.entityToExercise(it) }
        }
        emit(allExercises)
    }

    suspend fun addExercise(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            exerciseDao.insertExercise(converters.exerciseToEntity(exercise))
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            exerciseDao.delete(converters.exerciseToEntity(exercise))
        }
    }
}