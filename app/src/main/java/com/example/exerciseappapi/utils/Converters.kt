package com.example.exerciseappapi.utils

import androidx.room.TypeConverter
import com.example.exerciseappapi.models.Exercise
import com.example.exerciseappapi.models.ExerciseEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

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

    @TypeConverter
    fun fromExerciseList(value: String?): List<Exercise>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Exercise>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromExerciseListToString(list: List<Exercise>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromExerciseEntityList(value: List<ExerciseEntity>?): String? {
        val type = object : TypeToken<List<ExerciseEntity>>() {}.type
        return Gson().toJson(value, type)
    }

    @TypeConverter
    fun toExerciseEntityList(value: String?): List<ExerciseEntity>? {
        val type = object : TypeToken<List<ExerciseEntity>>() {}.type
        return Gson().fromJson(value, type)
    }

    fun ExerciseEntity.toExercise(): Exercise {
        return Exercise(
            id = this.id,
            name = this.name,
            bodyPart = this.bodyPart,
            equipment = this.equipment,
            gifUrl = this.gifUrl,
            target = this.target,
            secondaryMuscles = this.secondaryMuscles,
            instructions = this.instructions
        )
    }
}
