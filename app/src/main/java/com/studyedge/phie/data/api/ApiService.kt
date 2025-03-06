package com.studyedge.phie.data.api

import com.studyedge.phie.data.model.ApiResponse
import com.studyedge.phie.data.model.CourseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("app/settings.php")
    suspend fun getAppSettings(): Response<ApiResponse>

    @GET("app/new/courses.php")
    suspend fun getCourses(
        @Query("filterType") filterType: String = "all_batches",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<CourseResponse>

    @GET("app/new/courses.php")
    suspend fun searchCourses(
        @Query("filterType") filterType: String = "search",
        @Query("searchQuery") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<CourseResponse>
} 