package com.example.obsmini.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>
}

@SuppressLint("MissingPermission")
class LocationClientImpl @Inject constructor(context: Context): LocationClient {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
                    .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}