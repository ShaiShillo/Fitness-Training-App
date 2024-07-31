package com.example.exerciseappapi.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object PreferenceManager {

    private const val PREFS_NAME = "exercise_app_prefs"
    private const val KEY_LAST_SELECTED_DATE = "last_selected_date"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLastSelectedDate(context: Context, date: String) {
        getPreferences(context).edit().putString(KEY_LAST_SELECTED_DATE, date).apply()
    }

    fun getLastSelectedDate(context: Context): String? {
        return getPreferences(context).getString(KEY_LAST_SELECTED_DATE, null)
    }
}
