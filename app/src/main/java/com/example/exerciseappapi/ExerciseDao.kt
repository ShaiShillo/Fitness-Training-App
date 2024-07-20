package com.example.exerciseappapi

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): LiveData<List<ExerciseEntity>>

    @Query("DELETE FROM exercises")
    suspend fun deleteAll()
}
