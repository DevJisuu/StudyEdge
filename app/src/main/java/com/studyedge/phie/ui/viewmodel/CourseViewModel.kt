package com.studyedge.phie.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyedge.phie.data.model.Course
import com.studyedge.phie.data.repository.AppRepository
import com.studyedge.phie.util.CryptoUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CourseViewModel : ViewModel() {
    private val repository = AppRepository()
    private val gson = Gson()

    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    private val _isLastPage = MutableLiveData<Boolean>(false)
    val isLastPage: LiveData<Boolean> = _isLastPage
    
    private var currentPage = 1
    private var isSearchMode = false
    private var currentQuery = ""
    private val allCourses = mutableListOf<Course>()

    fun loadCourses() {
        isSearchMode = false
        currentPage = 1
        _isLastPage.value = false
        allCourses.clear()
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                repository.getCourses()
                    .onSuccess { response ->
                        if (response.encrypted) {
                            val decryptedData = CryptoUtil.decrypt(response.data)
                            val courseType = object : TypeToken<List<Course>>() {}.type
                            val courseList = gson.fromJson<List<Course>>(decryptedData, courseType)
                            
                            if (courseList.isEmpty()) {
                                _isLastPage.value = true
                                android.util.Log.d("CourseViewModel", "Initial load: No courses in response, setting isLastPage to true")
                            } else {
                                allCourses.addAll(courseList)
                                _courses.value = ArrayList(allCourses)
                                android.util.Log.d("CourseViewModel", "Initial load: Loaded ${courseList.size} courses")
                            }
                        } else {
                            _error.value = "Invalid response format"
                            android.util.Log.e("CourseViewModel", "Initial load: Invalid response format")
                        }
                        _isLoading.value = false
                    }
                    .onFailure {
                        _error.value = it.message ?: "Failed to load courses"
                        _isLoading.value = false
                        android.util.Log.e("CourseViewModel", "Initial load failed: ${it.message}")
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
                android.util.Log.e("CourseViewModel", "Initial load exception: ${e.message}")
            }
        }
    }
    
    fun loadMoreCourses() {
        if (_isLoading.value == true || _isLastPage.value == true) return
        
        // Simulate loading more courses by incrementing the page counter
        currentPage++
        _isLoading.value = true
        
        android.util.Log.d("CourseViewModel", "Simulating loading more courses, page: $currentPage")
        
        viewModelScope.launch {
            try {
                // Simulate network delay
                delay(1000)
                
                if (isSearchMode) {
                    repository.searchCourses(currentQuery)
                        .onSuccess { simulateNewDataResponse(it) }
                        .onFailure { handlePaginationFailure(it) }
                } else {
                    repository.getCourses()
                        .onSuccess { simulateNewDataResponse(it) }
                        .onFailure { handlePaginationFailure(it) }
                }
            } catch (e: Exception) {
                handlePaginationFailure(e)
            }
        }
    }
    
    private fun simulateNewDataResponse(response: com.studyedge.phie.data.model.CourseResponse) {
        if (response.encrypted) {
            try {
                val decryptedData = CryptoUtil.decrypt(response.data)
                val courseType = object : TypeToken<List<Course>>() {}.type
                val newCourses = gson.fromJson<List<Course>>(decryptedData, courseType)
                
                android.util.Log.d("CourseViewModel", "Received ${newCourses.size} new courses for simulated page $currentPage")
                
                if (newCourses.isEmpty()) {
                    _isLastPage.value = true
                    android.util.Log.d("CourseViewModel", "No courses in response for simulated page $currentPage, setting isLastPage to true")
                } else {
                    // Simulate pagination by taking a subset of courses or shuffling them
                    val simulatedNewCourses = if (currentPage <= 3) {
                        // For the first few "pages", use the original courses but modify them slightly
                        newCourses.map { it.copy(id = it.id + (currentPage * 1000)) }
                    } else {
                        // After a few pages, set isLastPage to true to stop pagination
                        _isLastPage.value = true
                        emptyList()
                    }
                    
                    android.util.Log.d("CourseViewModel", "Simulating ${simulatedNewCourses.size} new courses for page $currentPage")
                    
                    if (simulatedNewCourses.isEmpty()) {
                        _isLastPage.value = true
                        android.util.Log.d("CourseViewModel", "No more simulated courses, setting isLastPage to true")
                    } else {
                        allCourses.addAll(simulatedNewCourses)
                        // Create a new list to trigger LiveData update
                        _courses.value = ArrayList(allCourses)
                        android.util.Log.d("CourseViewModel", "Total courses now: ${allCourses.size}, updated from simulated page $currentPage")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to decrypt data: ${e.message}"
                android.util.Log.e("CourseViewModel", "Decryption error for simulated page $currentPage: ${e.message}", e)
            }
        } else {
            _error.value = "Invalid response format"
            android.util.Log.e("CourseViewModel", "Invalid response format for simulated page $currentPage, not encrypted")
        }
        _isLoading.value = false
    }
    
    private fun handlePaginationFailure(throwable: Throwable) {
        _error.value = throwable.message ?: "Failed to load more courses"
        _isLoading.value = false
    }

    fun searchCourses(query: String) {
        isSearchMode = true
        currentQuery = query
        currentPage = 1
        _isLastPage.value = false
        allCourses.clear()
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                repository.searchCourses(query)
                    .onSuccess { response ->
                        if (response.encrypted) {
                            val decryptedData = CryptoUtil.decrypt(response.data)
                            val courseType = object : TypeToken<List<Course>>() {}.type
                            val courseList = gson.fromJson<List<Course>>(decryptedData, courseType)
                            
                            if (courseList.isEmpty()) {
                                _isLastPage.value = true
                                android.util.Log.d("CourseViewModel", "Search: No courses in response, setting isLastPage to true")
                            } else {
                                allCourses.addAll(courseList)
                                _courses.value = ArrayList(allCourses)
                                android.util.Log.d("CourseViewModel", "Search: Found ${courseList.size} courses for query '$query'")
                            }
                        } else {
                            _error.value = "Invalid response format"
                            android.util.Log.e("CourseViewModel", "Search: Invalid response format")
                        }
                        _isLoading.value = false
                    }
                    .onFailure {
                        _error.value = it.message ?: "Failed to search courses"
                        _isLoading.value = false
                        android.util.Log.e("CourseViewModel", "Search failed: ${it.message}")
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
                android.util.Log.e("CourseViewModel", "Search exception: ${e.message}")
            }
        }
    }

    fun getCurrentPage(): Int {
        return currentPage
    }
} 