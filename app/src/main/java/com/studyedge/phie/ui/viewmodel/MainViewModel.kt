package com.studyedge.phie.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyedge.phie.data.model.ApiResponse
import com.studyedge.phie.data.repository.AppRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = AppRepository()

    private val _appSettings = MutableLiveData<ApiResponse>()
    val appSettings: LiveData<ApiResponse> = _appSettings

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _searchResults = MutableLiveData<List<String>>()
    val searchResults: LiveData<List<String>> = _searchResults

    init {
        fetchAppSettings()
    }

    private fun fetchAppSettings() {
        viewModelScope.launch {
            repository.getAppSettings()
                .onSuccess { _appSettings.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun searchCourses(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        // Get current page content for search
        val currentContent = getCurrentPageContent()
        
        _searchResults.value = currentContent.filter { 
            it.contains(query, ignoreCase = true) 
        }
    }

    private fun getCurrentPageContent(): List<String> {
        // This will be populated from the actual UI content
        val content = mutableListOf<String>()

        // Add content from Continue Watching section if available
        appSettings.value?.settings?.let { settings ->
            content.add("Continue Watching")
            content.add("Pick up where you left off")
        }

        // Add content from PW section
        content.add("PW Courses")
        content.add("Start your journey to success")

        // Add content from NT section
        content.add("NT Courses")
        content.add("Start your journey to success")

        // Add navigation items
        content.addAll(listOf(
            "Dashboard",
            "PhysicsWallah",
            "Next Toppers",
            "Apni Kaksha",
            "Telegram",
            "Network Stream",
            "Support"
        ))

        return content
    }
} 