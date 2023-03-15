package com.example.locationworkmanager


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

@SuppressLint("MissingPermission")
suspend fun collectLocationData(context: Context): List<Location> = withContext(IO) {

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Define 3 random locations in the Phoenix, AZ area
    val locations = listOf(
        generateRandomLocationInPhoenix(),
        generateRandomLocationInPhoenix(),
        generateRandomLocationInPhoenix()
    )

    // Collect the location data for each location using coroutines
    val deferredLocations = locations.map { location ->

        async {
            fusedLocationClient.lastLocation.apply {
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
    return@withContext deferredLocations.awaitAll()

}

//as the name implies, toast the results now that you have them
fun toastResults(context: Context, coroutineScope: CoroutineScope) {

    coroutineScope.launch(IO) {
        val list = collectLocationData(context)

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

private fun generateRandomLocationInPhoenix(): Location {
    val location = Location("")
    location.latitude = 33.4484 + (Math.random() * (33.4484 - 33.3472))
    location.longitude = -112.0740 - (Math.random() * (112.0740 - 111.9631))
    return location
}
