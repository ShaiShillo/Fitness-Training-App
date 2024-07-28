package com.example.exerciseappapi

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    fun exerciseToEntity(exercise: Exercise): ExerciseEntity {
        return ExerciseEntity(
            id = exercise.id,
            name = exercise.name,
            bodyPart = exercise.bodyPart,
            equipment = exercise.equipment,
            target = exercise.target,
            gifUrl = exercise.gifUrl,
            secondaryMuscles = exercise.secondaryMuscles,
            instructions = exercise.instructions,
            createdByUser = exercise.createdByUser
        )
    }

    fun entityToExercise(entity: ExerciseEntity): Exercise {
        return Exercise(
            id = entity.id,
            name = entity.name,
            bodyPart = entity.bodyPart,
            equipment = entity.equipment,
            target = entity.target,
            gifUrl = entity.gifUrl,
            secondaryMuscles = entity.secondaryMuscles,
            instructions = entity.instructions,
            createdByUser = entity.createdByUser
        )
    }

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return Gson().toJson(list)
    }


    fun exerciseListToEntityList(exercises: List<Exercise>): List<ExerciseEntity> {
        return exercises.map { exerciseToEntity(it) }
    }

    fun entityListToExerciseList(entities: List<ExerciseEntity>): List<Exercise> {
        return entities.map { entityToExercise(it) }
    }
}
