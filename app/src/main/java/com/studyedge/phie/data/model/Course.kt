package com.studyedge.phie.data.model

import com.google.gson.annotations.SerializedName

data class CourseResponse(
    val encrypted: Boolean,
    val data: String,
    val method: String = "AES-256-CBC"
)

data class Course(
    val id: Int,
    @SerializedName("batch_id")
    val batchId: String,
    val language: String,
    val exam: String,
    val name: String,
    @SerializedName("byName")
    val description: String,
    val photo: String,
    @SerializedName("class")
    val classLevel: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val examyear: String
) 