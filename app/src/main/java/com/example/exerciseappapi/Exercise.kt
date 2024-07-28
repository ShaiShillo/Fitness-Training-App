package com.example.exerciseappapi

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val target: String,
    val gifUrl: String,
    val secondaryMuscles: List<String>,
    val instructions: List<String>,
    val createdByUser: Boolean = false
) : Parcelable
