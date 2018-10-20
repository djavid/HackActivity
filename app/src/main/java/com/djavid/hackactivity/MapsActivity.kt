package com.djavid.hackactivity

import android.Manifest
import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLastKnownLocation: Location? = null
    private val mDefaultLocation = LatLng(0.0, 0.0)

    companion object {
        private const val DEFAULT_ZOOM = 12f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMyLocationButton()
        getDeviceLocation()
        loadAllPlaces()
    }

    @SuppressLint("CheckResult")
    private fun loadAllPlaces() {
        App.getApi().getAllPlaces()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ places ->
                places.forEach { place ->
                    val latlng = LatLng(place.latitude.toDouble(), place.longitude.toDouble())
                    mMap.addMarker(MarkerOptions().position(latlng).title(place.name))
                }
            }, {
                it.printStackTrace()
            })
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun setMyLocationButton() {
        if (checkLocationPermission()) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        } else {
            requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
                mMap.isMyLocationEnabled = it
                mMap.uiSettings.isMyLocationButtonEnabled = it
            }
        }
    }

    private fun getDeviceLocation() {
        try {
            if (checkLocationPermission()) {
                val locationResult = mFusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result

                        task.result?.let {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), DEFAULT_ZOOM)
                            )
                        }

                    } else {
                        Log.d("MapsActivity", "Current location is null. Using defaults.")
                        Log.e("MapsActivity", "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

}
