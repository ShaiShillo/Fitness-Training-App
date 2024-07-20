package com.example.exerciseappapi

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey val name: String,
    val bodyPart: String,
    val target: String
)
