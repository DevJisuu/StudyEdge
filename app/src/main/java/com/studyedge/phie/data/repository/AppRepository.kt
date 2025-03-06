package com.studyedge.phie.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyedge.phie.data.api.ApiClient
import com.studyedge.phie.data.model.ApiResponse
import com.studyedge.phie.data.model.CourseResponse
import com.studyedge.phie.util.CryptoUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository {
    private val apiService = ApiClient.apiService
    private val gson = Gson()

    suspend fun getAppSettings(): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAppSettings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch app settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCourses(page: Int = 1): Result<CourseResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCourses(page = 1)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch courses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCourses(query: String, page: Int = 1): Result<CourseResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchCourses(query = query, page = 1)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search courses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 