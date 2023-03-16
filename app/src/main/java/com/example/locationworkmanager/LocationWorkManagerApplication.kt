package com.example.locationworkmanager

import android.app.Application
import androidx.startup.AppInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LocationWorkManagerApplication:  Application() {
    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this).initializeComponent(CustomWorkManagerInitializer::class.java)
    }
}

