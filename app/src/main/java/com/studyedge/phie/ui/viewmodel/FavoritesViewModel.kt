package com.studyedge.phie.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyedge.phie.data.model.Course
import com.studyedge.phie.data.repository.FavoritesRepository
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val repository = FavoritesRepository()

    private val _favorites = MutableLiveData<List<Course>>()
    val favorites: LiveData<List<Course>> = _favorites

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _favorites.value = repository.getFavorites()
        }
    }

    fun addFavorite(course: Course) {
        viewModelScope.launch {
            repository.addFavorite(course)
            loadFavorites()
        }
    }

    fun removeFavorite(course: Course) {
        viewModelScope.launch {
            repository.removeFavorite(course)
            loadFavorites()
        }
    }

    fun isFavorite(courseId: Int): Boolean {
        return _favorites.value?.any { it.id == courseId } ?: false
    }
} 