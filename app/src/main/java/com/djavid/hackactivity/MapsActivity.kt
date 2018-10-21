package com.djavid.hackactivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.location.Location
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.djavid.hackactivity.data.Place
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet.*
import org.joda.time.DateTime
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.android.gms.maps.model.BitmapDescriptor

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLastKnownLocation: Location? = null
    private val mDefaultLocation = LatLng(0.0, 0.0)
    private var places: List<Place>? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var disposable: Disposable? = null
    private var chosenPlace: Place? = null

    companion object {
        private const val DEFAULT_ZOOM = 12f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    chosenPlace = null
                }
            }
        })

        joinBtn.setOnClickListener {
            chosenPlace?.let { place ->
                if (place.upcomingEvent != null)
                    joinEvent(place.upcomingEvent.id)
            }
        }

        bottom_sheet.setOnClickListener {
            val id = chosenPlace?.id
            val name = chosenPlace?.name

            if (id != null && name != null) {
                val intent = Intent(this, PlaceActivity::class.java).apply {
                    putExtra("placeId", id)
                    putExtra("placeName", name)
                }
                startActivity(intent)
            }
        }
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
        mMap.setOnMarkerClickListener(this::onMarkerClick)
        setMyLocationButton()
        getDeviceLocation()
        loadAllMarkers()
    }

    private fun onMarkerClick(marker: Marker): Boolean {
        val place: Place? = marker.tag as Place

        place?.let {
            placeTitle.text = it.name
            chosenPlace = it

            if (it.upcomingEvent != null) {
                activityTitle.text = it.upcomingEvent.type
                membersTitle.text = "${it.upcomingEvent.membersCount} / ${it.upcomingEvent.maxAllowed}"
                val start = DateTime(it.upcomingEvent.dateTime * 1000)
                val end = start.plusMinutes(it.upcomingEvent.duration)
                setUpcomingEventState(start.isBeforeNow && end.isAfterNow, it.upcomingEvent.joined != 0)
                eventState.visible(true)
                upcomingEvent.visible(true)
                bottomSheetBehavior.peekHeight = toPx(200.0)
            } else {
                eventState.visible(false)
                upcomingEvent.visible(false)
                bottomSheetBehavior.peekHeight = toPx(50.0)
            }

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return false
    }

    private fun setUpcomingEventState(live: Boolean, joined: Boolean) {
        eventState.text = if (live) getString(R.string.title_live) else getString(R.string.title_upcoming)
        eventState.setBackgroundColor(
            if (live) ContextCompat.getColor(this, R.color.red)
            else ContextCompat.getColor(this, R.color.orange)
        )
        setEventJoined(joined)
    }

    private fun setEventJoined(joined: Boolean) {
        joinBtn.text = if (joined) getString(R.string.joined) else getString(R.string.join_event)
        joinBtn.setTextColor(
            if (joined) ContextCompat.getColor(this, R.color.green)
            else ContextCompat.getColor(this, R.color.blue)
        )
        joinBtn.isEnabled = !joined
    }

    private fun showJoinBtnProgress(show: Boolean) {
        joinBtn.visible(!show)
        progressBar.visible(show)
    }

    @SuppressLint("CheckResult")
    private fun joinEvent(eventId: Int) {
        disposable?.dispose()
        disposable = App.getApi().joinEvent(App.getPreferences().getInt("token", 2), eventId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showJoinBtnProgress(true) }
            .doOnEvent { _, _ -> showJoinBtnProgress(false) }
            .subscribe({
                if (it == 1) {
                    setEventJoined(true)
                }
            }, {
                it.printStackTrace()
            })
    }

    @SuppressLint("CheckResult")
    private fun loadAllMarkers() {
        disposable?.dispose()
        disposable = App.getApi().getAllPlaces(App.getPreferences().getInt("token", 2))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ places ->
                this.places = places
                places.forEach { place ->
                    val latlng = LatLng(place.latitude.toDouble(), place.longitude.toDouble())
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(latlng)
                            .title(place.name)
                            .apply {
                                if (place.types.size == 1) {
                                    val activityIcon = getIconForActivityType(place.types[0])
                                    if (activityIcon != null) icon(activityIcon)
                                } else {

                                }
                            }
                    )
                    marker.tag = place
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

    private fun getIconForActivityType(type: String): BitmapDescriptor? {
        return when (type) {
            "volleyball" -> bitmapDescriptorFromVector(R.drawable.ic_volleyball)
            "table tennis" -> bitmapDescriptorFromVector(R.drawable.ic_ping_pong)
            "tennis" -> bitmapDescriptorFromVector(R.drawable.ic_tennis_ball)
            "football" -> bitmapDescriptorFromVector(R.drawable.ic_football)
            "basketball" -> bitmapDescriptorFromVector(R.drawable.ic_basketball)
            "frisbee" -> bitmapDescriptorFromVector(R.drawable.ic_frisbee)
            else -> null
        }
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}
