package com.studyedge.phie

import android.app.Application

class StudyEdgeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: StudyEdgeApp
            private set
    }
} 