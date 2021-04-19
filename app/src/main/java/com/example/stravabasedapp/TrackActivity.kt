package com.example.stravabasedapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class TrackActivity : AppCompatActivity() {
    lateinit var time: TextView
    lateinit var distance: TextView
    lateinit var startButton: Button
    lateinit var mapView: MapView
    private var isTracking: Boolean = false
    internal var locations = mutableListOf<Location>()
    internal lateinit var nameOfActivity: String

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        time = findViewById(R.id.timePassed)
        distance = findViewById(R.id.distance)
        distance.text = "0 m"
        startButton = findViewById(R.id.startButton)
        mapView = findViewById(R.id.mapView)

        nameOfActivity = intent.getStringExtra("nameAct").toString()
        Log.d("CREATION2", nameOfActivity)


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
        var locationListener: LocationCallback = object : LocationCallback() {
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
                            results
                        )
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
                if (!projDir.exists()) {
                    projDir.mkdirs()
                    Log.d("CREATION", "NOT CREATED")
                }

                val f = File(projDir, "file.gpx")
                if (f.exists()) {
                    Log.wtf(
                        "myTag", "Temp File created: "
                                + f.absolutePath
                    );
                } else {
                    Log.wtf(
                        "myTag", "Temp File cannot be created: "
                                + f.absolutePath
                    );

                    if (!f.getParentFile().exists())
                        f.getParentFile().mkdirs();
                    Log.wtf("myTag", "parent ${f.parentFile.exists()}")
                    f.createNewFile()
                }

                GPX.writePath(file = f, n = nameOfActivity!!, points = locations)
                val inputStream: InputStream = f.inputStream()

                val inputString = inputStream.bufferedReader().use { it.readText() }
                println(inputString)

                postActivity(f)

                handler.removeCallbacksAndMessages(null)
                LocationServices.getFusedLocationProviderClient(this)
                    .removeLocationUpdates(locationListener)
                finish()
            } else {
                dist = 0f
                startCoordinate = null
                finishCoordinate = null
                var startTime = System.currentTimeMillis()

                handler.post(object : Runnable {
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
                LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(request, locationListener, Looper.myLooper())
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

    private fun postActivity(f: File) {
        val uri =
            Uri.Builder().scheme("https")
                .authority("www.strava.com")
                .path("/api/v3/uploads")
                .appendQueryParameter("name", nameOfActivity)
                .appendQueryParameter("data_type", "gpx")
                .appendQueryParameter("activity_type", "run")
                .appendQueryParameter(
                    "access_token",
                    getSharedPreferences("user", Context.MODE_PRIVATE).getString(
                        "access_token",
                        "x"
                    )
                )
                .build().toString()
        Log.d("CREAT", uri)

        val volleyMultipartRequest: VolleyMultipartRequest =
            object : VolleyMultipartRequest(
                Method.POST, uri,
                Response.Listener { response ->
                    try {
                        val obj = JSONObject(String(response.data))
                        Toast.makeText(
                            applicationContext,
                            "Activity uploaded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(
                        applicationContext,
                        "Activity upload failed",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.wtf("myTag", "e1 " + error.message)
                    Log.wtf("myTag", "e2 " + error.localizedMessage)
                    Log.wtf("myTag", "e3 " + error.toString())
                    Log.wtf("myTag", "e4 " + error.networkResponse.allHeaders.toString())
                    Log.wtf("myTag", "e5 " + error.networkResponse.toString())
                    Log.wtf("myTag", "e6 " + error.networkResponse.data.toString())
                }) {
                override fun getByteData(): Map<String, DataPart> {
                    val params: MutableMap<String, DataPart> = mutableMapOf()
                    params.put(
                        "file",
                        DataPart(f.absolutePath, f.readBytes())
                    )
                    return params
                }
            }

        Volley.newRequestQueue(this).add(volleyMultipartRequest)
    }

}