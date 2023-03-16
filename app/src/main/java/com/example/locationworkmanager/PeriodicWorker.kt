package com.example.locationworkmanager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*

/**
 * The instance of the Work Manager we will use.  Made it a Coroutine worker so we could get Coroutine Scope
 */
////// Task 2 & Task 3
//I selected WorkManager because, it is the Google recommended method and also for the requirements:  must
//run even when closed or app restart.
@HiltWorker
class PeriodicWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val params: WorkerParameters,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : CoroutineWorker(context, params) {

    /**
     * This method will be called every time period.
     */
    override suspend fun doWork(): Result = coroutineScope {
        try {
            toastResults(this, context, fusedLocationProviderClient)
            Result.success()
        } catch (exception: Exception) {
            // Handle any exceptions that might occur
            Log.e(WORK_TAG, exception.localizedMessage ?: "Error tosting results")
            Result.failure()
        }
    }

    companion object {
        const val WORK_TAG = "PeriodicWorker"
        /**
         * as the name implies, toast the results now that you have them
         */
        fun toastResults(coroutineScope: CoroutineScope, context: Context, fusedLocationProviderClient: FusedLocationProviderClient) {

            coroutineScope.launch(Dispatchers.IO) {
                val list = collectLocationData(fusedLocationProviderClient)

                var toastText = ""
                list.forEach {
                    toastText += "lat: ${it.latitude}, long: ${it.longitude}, "
                }
                toastText.dropLast(2)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
                }
            }
        }

        /**
         * This method takes three random locations and gets the location data for each
         * done using coroutines
         */
        //////////Task 3
        //The FusedLocationProvider is the best way to get last location.
        @SuppressLint("MissingPermission")
        suspend fun collectLocationData(fusedLocationProviderClient: FusedLocationProviderClient): List<Location> = withContext(Dispatchers.IO) {

            // Define 3 random locations in the Phoenix, AZ area
            val locations = listOf(
                generateRandomLocationInPhoenix(),
                generateRandomLocationInPhoenix(),
                generateRandomLocationInPhoenix()
            )

            // Collect the location data for each location using coroutines
            val deferredLocations = locations.map { location ->

                async {
                    fusedLocationProviderClient.lastLocation.apply {
                        this.addOnSuccessListener {
                            it?.let {
                                location.latitude = it.latitude
                                location.longitude = it.longitude
                            }
                        }
                        this.addOnFailureListener {
                            Log.e("Failure", it.localizedMessage ?: "")
                        }
                    }
                    location
                }
            }
            return@withContext deferredLocations.awaitAll() //get everthing at once
            ////////////Task 4
            //I would store the locations in a Room Database, or sharedpreferences
            //Shared Prefernces is not a good solution as there will be so many of them
            //Room is preferable, you can store and retrieve the data quickly and use the
            //return type of Flow to use with coroutines
        }

        /**
         * This method generates a random location in Phoenix, AZ
         */
        private fun generateRandomLocationInPhoenix(): Location {
            val location = Location("")
            location.latitude = 33.4484 + (Math.random() * (33.4484 - 33.3472))
            location.longitude = -112.0740 - (Math.random() * (112.0740 - 111.9631))
            return location
        }
    }
}

