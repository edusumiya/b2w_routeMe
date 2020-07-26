package com.sumiya.routeme.scenes.map

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.sumiya.routeme.R
import java.security.AccessControlContext

interface MapPresenterProtocol {
    var fusedLocationProviderClient: FusedLocationProviderClient
    var locationPermissionGranted: Boolean
    var userLastLocation: Location?
    fun updateLocationUI()
    fun getDeviceLocation()
}

class MapPresenter(private val view: MapActivityProtocol, private val applicationContext: Context) : MapPresenterProtocol {
    //region Properties and Variables
    override var locationPermissionGranted = false
    override lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override var userLastLocation: Location? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    //endregion

    //region MapPresenterProtocol
    override fun updateLocationUI() {
        if (view.mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                view.mMap?.isMyLocationEnabled = true
                view.mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                view.mMap?.isMyLocationEnabled = false
                view.mMap?.uiSettings?.isMyLocationButtonEnabled = false
                userLastLocation = null
                view.getLocationPermission()
            }
        } catch (e: SecurityException) {
            Toast.makeText(applicationContext,e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userLastLocation = task.result
                        if (userLastLocation != null) {
                            val position = LatLng(
                                userLastLocation!!.latitude,
                                userLastLocation!!.longitude
                            )
                            view.mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    position, DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        view.mMap?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        view.mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(applicationContext,e.message, Toast.LENGTH_LONG).show()
        }
    }
    //endregion

    //region Constants
    companion object {
        private const val DEFAULT_ZOOM = 15
    }
    //endregion
}