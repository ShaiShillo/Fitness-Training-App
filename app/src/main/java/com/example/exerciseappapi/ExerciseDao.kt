package com.example.exerciseappapi

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT DISTINCT bodyPart FROM exercises")
    suspend fun getAllBodyParts(): List<String>

    @Query("SELECT DISTINCT target FROM exercises WHERE bodyPart = :bodyPart")
    suspend fun getTargetsByBodyPart(bodyPart: String): List<String>

    @Query("SELECT DISTINCT equipment FROM exercises WHERE bodyPart = :bodyPart AND target = :target")
    suspend fun getEquipmentByBodyPartAndTarget(bodyPart: String, target: String): List<String>

    @Query("SELECT DISTINCT equipment FROM exercises")
    suspend fun getAllEquipment(): List<String>

    @Delete
    suspend fun delete(exercise: ExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
}