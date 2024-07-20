package com.example.exerciseappapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT DISTINCT bodyPart FROM exercises")
    suspend fun getAllBodyParts(): List<String>

    @Query("SELECT DISTINCT target FROM exercises WHERE bodyPart = :bodyPart")
    suspend fun getTargetsByBodyPart(bodyPart: String): List<String>

    @Query("SELECT DISTINCT equipment FROM exercises WHERE bodyPart = :bodyPart AND target = :target")
    suspend fun getEquipmentByBodyPartAndTarget(bodyPart: String, target: String): List<String>
}
