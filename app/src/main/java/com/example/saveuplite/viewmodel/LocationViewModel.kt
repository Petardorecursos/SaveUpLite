package com.example.saveuplite.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel : ViewModel() {

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    @SuppressLint("MissingPermission")
    fun getLocation(context: Context) {
        Log.d("LocationViewModel", "Attempting to get location")
        val fusedClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                _location.value = loc
                Log.d("LocationViewModel", "Location success: $loc")
            } else {
                Log.d("LocationViewModel", "lastLocation was null, requesting current location")
                val cancellationTokenSource = CancellationTokenSource()
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                    .addOnSuccessListener { currentLocation: Location? ->
                        if (currentLocation != null) {
                            _location.value = currentLocation
                            Log.d("LocationViewModel", "Current location success: $currentLocation")
                        } else {
                            Log.d("LocationViewModel", "getCurrentLocation returned null")
                        }
                    }.addOnFailureListener { e ->
                        Log.e("LocationViewModel", "Failed to get current location", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("LocationViewModel", "Location failed", e)
        }
    }
}
