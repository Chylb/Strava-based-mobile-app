package com.example.stravabasedapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class TrackActivity : AppCompatActivity() {
    lateinit var time: TextView
    lateinit var distance: TextView
    lateinit var startButton : Button
    lateinit var mapView : MapView
    private var isTracking: Boolean = false
    internal var locations = mutableListOf<Location>()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        time = findViewById(R.id.timePassed)
        distance = findViewById(R.id.distance)
        distance.text = "0 m"
        startButton = findViewById(R.id.startButton)
        mapView = findViewById(R.id.mapView)


        var duration: Long = 0
        var dist: Float = 0f;
        var startTime: Long = 0
        var startCoordinate: LatLng? = null
        var finishCoordinate: LatLng? = null


        var map: GoogleMap? = null

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(googleMap: GoogleMap?) {
                map = googleMap;
                if (map != null) {
                    map!!.setMinZoomPreference(16f)
                    map!!.isMyLocationEnabled = true
                    map!!.uiSettings.isZoomControlsEnabled = false
                    map!!.uiSettings.isScrollGesturesEnabled = false
                    map!!.uiSettings.isMyLocationButtonEnabled = false
                }
            }

        })
        var handler: Handler = Handler()
        var locationListener: LocationCallback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult?) {
                if (result != null) {
                    var previousCoordinate: LatLng? = finishCoordinate

                    var lastLoc = result.lastLocation
                    finishCoordinate = LatLng(lastLoc.latitude, lastLoc.longitude)

                    if (startCoordinate == null) {
                        startCoordinate = finishCoordinate;
                    }

                    if (map != null) {
                        map!!.moveCamera(CameraUpdateFactory.newLatLng(finishCoordinate))
                    }

                    if (previousCoordinate != null) {
                        var results: FloatArray = floatArrayOf(0.0f)
                        Location.distanceBetween(
                            previousCoordinate.latitude,
                            previousCoordinate.longitude,
                            finishCoordinate!!.latitude,
                            finishCoordinate!!.longitude,
                            results)
                        val temp = Location(LocationManager.GPS_PROVIDER)
                        temp.latitude = finishCoordinate!!.latitude
                        temp.longitude = finishCoordinate!!.longitude
                        temp.time = lastLoc.time
                        locations.add(temp)

                        dist += results[0]
                        distance.text = String.format("%.2f m", dist)
                    }
                }
            }
        }

        startButton.setOnClickListener {
            if (isTracking) {
                val dirPath = filesDir.absolutePath + File.separator + "newfoldername"
                val projDir = File(dirPath)
                if (!projDir.exists())
                    Log.d("CREATION", "NOT CREATED")

                val f = File(projDir,"file")
                if (f.exists())
                    Log.d("CREATION","Temp File created: "
                            + f.absolutePath);
                else
                    Log.d("CREATION","Temp File cannot be created: "
                            + f.absolutePath);
                GPX.writePath(file= f, n="activity", points=locations)
                val inputStream: InputStream = f.inputStream()

                val inputString = inputStream.bufferedReader().use { it.readText() }
                println(inputString)

                handler.removeCallbacksAndMessages(null)
                LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationListener)
            } else {
                dist = 0f
                startCoordinate = null
                finishCoordinate = null
                var startTime = System.currentTimeMillis()

                handler.post(object: Runnable {
                    override fun run() {
                        duration = System.currentTimeMillis() - startTime
                        val locale = Locale.ENGLISH
                        time.text = SimpleDateFormat("mm:ss", locale).format(Date(duration))

                        handler.postDelayed(this, 20)
                    }
                })

                var request: LocationRequest = LocationRequest().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    fastestInterval = 1000
                    interval = 10000
                }
                var builder = LocationSettingsRequest.Builder()
                builder.addLocationRequest(request)
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(request, locationListener, Looper.myLooper())
            }
            isTracking = !isTracking
        }

    }
    override fun onStart() {
        super.onStart()
        findViewById<MapView>(R.id.mapView).onStart()
    }

    override fun onResume() {
        super.onResume()
        findViewById<MapView>(R.id.mapView).onResume()
    }

    override fun onPause() {
        findViewById<MapView>(R.id.mapView).onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        findViewById<MapView>(R.id.mapView).onStop()
    }

    override fun onDestroy() {
        findViewById<MapView>(R.id.mapView).onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        findViewById<MapView>(R.id.mapView).onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        findViewById<MapView>(R.id.mapView).onLowMemory()
    }

}