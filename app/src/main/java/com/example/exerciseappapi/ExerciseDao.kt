package com.example.exerciseappapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises")
    suspend fun deleteAll()
}
