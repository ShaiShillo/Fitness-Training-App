package com.example.exerciseappapi.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_dates")
data class WorkoutDateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutId: Int,
    val date: String
)