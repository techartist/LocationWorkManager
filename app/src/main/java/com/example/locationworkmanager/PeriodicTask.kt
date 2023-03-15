package com.example.locationworkmanager

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.coroutineScope

class PeriodicWorker(val context: Context, val params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val WORK_TAG = "PeriodicWorker"
    }

    override suspend fun doWork(): Result = coroutineScope {
        // This method will be called every hour.
        try {
            toastResults(context, this)
            Result.success()
        }

        catch (exception: Exception) {
            // Handle any exceptions that might occur
            Log.e(WORK_TAG, exception.localizedMessage ?: "Error tosting results")
            Result.failure()
        }
    }
}

