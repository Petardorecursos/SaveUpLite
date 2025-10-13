package com.example.saveuplite.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
                Log.d("LocationViewModel", "lastLocation was null")
            }
        }.addOnFailureListener { e ->
            Log.e("LocationViewModel", "Location failed", e)
        }
    }
}
