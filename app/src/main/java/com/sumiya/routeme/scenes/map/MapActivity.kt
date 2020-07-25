package com.sumiya.routeme.scenes.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sumiya.routeme.R
import com.sumiya.routeme.enums.MarkerType
import kotlinx.android.synthetic.main.activity_main.*

interface MapActivityProtocol {
    var mMap: GoogleMap?
    fun setMapMarker(location: LatLng, markerType: MarkerType)
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

        presenter = MapPresenter(this)

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
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        locationButton.setOnClickListener {
            presenter.getDeviceLocation()
        }
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
    override fun setMapMarker(location: LatLng, markerType: MarkerType) {
        val markerOptions = MarkerOptions()

        markerOptions.position(location)

        when(markerType) {
            MarkerType.USER -> {
                markerOptions.title("User Position")
                markerOptions.icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }

            MarkerType.DESTINY -> {
                markerOptions.title("Destiny Position")
                markerOptions.icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_ROSE
                    )
                )
            }
        }

        mMap?.addMarker(markerOptions)
    }

    override fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.locationPermissionGranted = true
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
