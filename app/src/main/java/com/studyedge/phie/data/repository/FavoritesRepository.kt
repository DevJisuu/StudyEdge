package com.studyedge.phie.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyedge.phie.StudyEdgeApp
import com.studyedge.phie.data.model.Course

class FavoritesRepository {
    private val context: Context = StudyEdgeApp.instance
    private val sharedPrefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    suspend fun getFavorites(): List<Course> {
        val json = sharedPrefs.getString("favorite_courses", "[]")
        val type = object : TypeToken<List<Course>>() {}.type
        return gson.fromJson(json, type)
    }

    suspend fun addFavorite(course: Course) {
        val favorites = getFavorites().toMutableList()
        if (!favorites.any { it.id == course.id }) {
            favorites.add(course)
            saveFavorites(favorites)
        }
    }

    suspend fun removeFavorite(course: Course) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.id == course.id }
        saveFavorites(favorites)
    }

    private fun saveFavorites(favorites: List<Course>) {
        val json = gson.toJson(favorites)
        sharedPrefs.edit().putString("favorite_courses", json).apply()
    }
} 