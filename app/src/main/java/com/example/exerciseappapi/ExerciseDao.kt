package com.example.exerciseappapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBodyParts(bodyParts: List<BodyPart>)

    @Query("SELECT * FROM body_parts")
    suspend fun getAllBodyParts(): List<BodyPart>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEquipment(equipment: List<Equipment>)

    @Query("SELECT * FROM equipment")
    suspend fun getAllEquipment(): List<Equipment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTargets(targets: List<Target>)

    @Query("SELECT * FROM targets")
    suspend fun getAllTargets(): List<Target>
}
