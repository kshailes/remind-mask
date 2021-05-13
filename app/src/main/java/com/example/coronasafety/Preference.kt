package com.example.coronasafety

import android.content.Context
import android.content.SharedPreferences

const val LAST_NOTIFIED_TIME = "LAST_NOTIFIED_TIME"

class Preference(context: Context) {
    private val sharedPref: SharedPreferences by lazy {
        context?.getSharedPreferences(
            "preference_file", Context.MODE_PRIVATE
        )
    }

    fun putLong(key: String, value: Long) {
        with(sharedPref.edit()) {
            putLong(key, value)
            apply()
        }
    }

    fun getLong(key: String) = sharedPref.getLong(key, -1L)

}