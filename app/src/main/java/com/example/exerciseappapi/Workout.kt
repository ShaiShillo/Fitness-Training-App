package com.example.exerciseappapi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
@Parcelize
data class Workout(
    val workoutName: String,
    val creationDate: Date,
    val exercises: List<Exercise>
) : Parcelable