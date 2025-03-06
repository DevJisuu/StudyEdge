package com.studyedge.phie.data.model

data class ApiResponse(
    val status: String,
    val settings: AppSettings
)

data class AppSettings(
    val app_name: String,
    val app_logo: String,
    val version: String,
    val description: String,
    val developer: String,
    val support: Support,
    val maintenance_mode: Boolean,
    val force_update: ForceUpdate,
    val last_updated: String,
    val versions: List<Version>
)

data class Support(
    val email: String,
    val website: String,
    val telegram: String
)

data class ForceUpdate(
    val enabled: Boolean,
    val min_version: String,
    val message: String,
    val update_url: String
)

data class Version(
    val version: String,
    val release_date: String,
    val changelog: List<String>
) 