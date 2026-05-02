package com.example.oskolki.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var currentLocation: Location? = null
    private var locationListener: LocationListener? = null
    private var networkListener: LocationListener? = null

    fun startLocationUpdates(callback: (Location) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationHelper", "Location permission not granted")
            return
        }

        // GPS listener
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
                Log.d("LocationHelper", "GPS location: ${location.latitude}, ${location.longitude}")
                callback(location)
            }

            override fun onProviderEnabled(provider: String) {
                Log.d("LocationHelper", "Provider enabled: $provider")
            }
            override fun onProviderDisabled(provider: String) {
                Log.d("LocationHelper", "Provider disabled: $provider")
            }
        }

        // Network listener (for faster first fix)
        networkListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
                Log.d("LocationHelper", "Network location: ${location.latitude}, ${location.longitude}")
                callback(location)
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            // Request updates from GPS
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,  // 1 second
                1f,    // 1 meter
                locationListener!!
            )
            Log.d("LocationHelper", "GPS updates requested")
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "GPS permission denied", e)
        }

        try {
            // Request updates from Network
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    1f,
                    networkListener!!
                )
                Log.d("LocationHelper", "Network updates requested")
            }
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Network permission denied", e)
        }

        // Try to get last known location immediately
        try {
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            val bestLast = when {
                lastGps != null && lastNetwork != null -> 
                    if (lastGps.time > lastNetwork.time) lastGps else lastNetwork
                lastGps != null -> lastGps
                lastNetwork != null -> lastNetwork
                else -> null
            }
            
            if (bestLast != null) {
                currentLocation = bestLast
                Log.d("LocationHelper", "Last known location: ${bestLast.latitude}, ${bestLast.longitude}")
                callback(bestLast)
            }
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Error getting last known location", e)
        }
    }

    fun stopLocationUpdates() {
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
        networkListener?.let {
            locationManager.removeUpdates(it)
        }
        Log.d("LocationHelper", "Location updates stopped")
    }

    fun getCurrentLocation(): Location? {
        return currentLocation
    }
}
