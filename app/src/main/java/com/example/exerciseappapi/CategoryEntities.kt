package com.example.exerciseappapi

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_parts")
data class BodyPart(
    @PrimaryKey val name: String
)

@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey val name: String
)

@Entity(tableName = "targets")
data class Target(
    @PrimaryKey val name: String
)
