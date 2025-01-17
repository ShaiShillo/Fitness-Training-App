package com.example.exerciseappapi.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.exerciseappapi.models.WorkoutDateEntity
import com.example.exerciseappapi.models.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    fun getWorkouts(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutDate(workoutDate: WorkoutDateEntity)

    @Query("SELECT workoutId FROM workout_dates WHERE date = :date")
    suspend fun getWorkoutIdsByDate(date: String): List<Int>

    @Query("SELECT * FROM workouts WHERE id IN (:workoutIds)")
    suspend fun getWorkoutsByIds(workoutIds: List<Int>): List<WorkoutEntity>

    @Query("DELETE FROM workout_dates WHERE workoutId = :workoutId AND date = :date")
    suspend fun deleteWorkoutDate(workoutId: Int, date: String)

}
