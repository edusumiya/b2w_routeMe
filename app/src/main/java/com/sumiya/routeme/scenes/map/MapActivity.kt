package com.sumiya.routeme.scenes.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.sumiya.routeme.R

interface MapActivityProtocol {
    /**
     * Map Property
     */
    var mMap: GoogleMap?

    /**
     * Sets a Marker on Map
     *
     * @param location
     */
    fun setMapMarker(location: LatLng)

    /**
     * Gets the user location permission
     *
     */
    fun getLocationPermission()
}

class MapActivity : AppCompatActivity(), MapActivityProtocol, OnMapReadyCallback {
    //region Properties and Variables
    override var mMap: GoogleMap? = null

    private lateinit var presenter: MapPresenterProtocol
    //endregion

    //region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = MapPresenter(this, applicationContext)

        configureData()
        configureUI()
    }
    //endregion

    //region Private Methods
    private fun configureData() {
        presenter.fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    private fun configureUI() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.searchView)
                    as AutocompleteSupportFragment

        autocompleteFragment.setHint(getString(R.string.localHint))

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Toast.makeText(applicationContext, place.name, Toast.LENGTH_LONG).show()

                setMapMarker(place.latLng!!)
            }

            override fun onError(status: Status) {
                Toast.makeText(applicationContext,getString(R.string.placeError), Toast.LENGTH_LONG).show()
            }
        })

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun setMarkersToZoom(destinyMarker: MarkerOptions) {
        try {
            val userMarkerOptions = MarkerOptions()

            userMarkerOptions.position(LatLng(presenter.userLastLocation!!.latitude,presenter.userLastLocation!!.longitude))
            userMarkerOptions.title("User Position")

            val markers = mutableListOf<MarkerOptions>()
            markers.add(userMarkerOptions)
            markers.add(destinyMarker)
            zoomToFitLocations(markers)
        } catch (exception: Exception) {
            Toast.makeText(applicationContext,getString(R.string.genericError), Toast.LENGTH_LONG).show()
        }
    }

    private fun zoomToFitLocations(markers: List<MarkerOptions>) {
        val builder = LatLngBounds.Builder()

        for (marker in markers) {
            builder.include(marker.position)
        }

        val bounds = builder.build()
        val padding = 300
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        mMap?.animateCamera(cameraUpdate)
    }
    //endregion

    //region OnMapReadyCallback
    override fun onMapReady(map: GoogleMap) {
        this.mMap = map

        presenter.updateLocationUI()
        presenter.getDeviceLocation()
    }
    //endregion

    //region MapActivityProtocol
    override fun setMapMarker(location: LatLng) {
        val markerOptions = MarkerOptions()

        markerOptions.position(location)
        markerOptions.title("Destiny Position")
        markerOptions.icon(
            BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_ROSE
            )
        )

        mMap?.clear()
        mMap?.addMarker(markerOptions)

        setMarkersToZoom(markerOptions)
    }

    override fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.locationPermissionGranted = true
            presenter.updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
    //endregion

    //region Permission Methods
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        presenter.locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    presenter.locationPermissionGranted = true
                }
            }
        }

        presenter.updateLocationUI()
    }
    //endregion

    //region Constants
    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
    //endregion
}
