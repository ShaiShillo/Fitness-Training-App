package com.example.exerciseappapi

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApiService {
    @GET("exercises")
    suspend fun getExercises(@Query("offset") offset: Int = 0, @Query("limit") limit: Int = 0): List<Exercise>

    @GET("exercises/{id}")
    suspend fun getExerciseById(@Path("id") id: String): Exercise

    @GET("exercises/name/{name}")
    suspend fun getExercisesByName(@Path("name") name: String, @Query("offset") offset: Int = 0, @Query("limit") limit: Int = 0): List<Exercise>

    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(@Path("bodyPart") bodyPart: String, @Query("offset") offset: Int = 0, @Query("limit") limit: Int = 0): List<Exercise>

    @GET("exercises/equipment/{equipment}")
    suspend fun getExercisesByEquipment(@Path("equipment") equipment: String, @Query("offset") offset: Int = 0, @Query("limit") limit: Int = 0): List<Exercise>

    @GET("exercises/target/{target}")
    suspend fun getExercisesByTarget(@Path("target") target: String, @Query("offset") offset: Int = 0, @Query("limit") limit: Int = 0): List<Exercise>
}
