package com.example.exerciseappapi.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date
@Parcelize
data class Workout(
    @PrimaryKey val id: Int,
    val workoutName: String,
    val creationDate: Date,
    val exercises: List<Exercise>
) : Parcelable

