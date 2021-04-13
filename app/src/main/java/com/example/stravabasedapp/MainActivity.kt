package com.example.stravabasedapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var syncButton: Button
    lateinit var printButton: Button
    lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.wtf("myTag", "creating main activity")
        Log.wtf(
            "myTag",
            "is there token " + getSharedPreferences(
                "user",
                Context.MODE_PRIVATE
            ).contains("access_token").toString()
        )
        if (!getSharedPreferences("user", Context.MODE_PRIVATE).contains("access_token")) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        }
        Log.wtf("myTag", "inside main activity")
        syncButton = findViewById(R.id.syncButton)
        syncButton.setOnClickListener {
            synchronizeActivities()
            /*
            //PRZYKLADOWE AKTYWNOSCI
            val json = "[{\"resource_state\":2,\"athlete\":{\"id\":134815,\"resource_state\":1},\"name\":\"Happy Friday\",\"distance\":24931.4,\"moving_time\":4500,\"elapsed_time\":4500,\"total_elevation_gain\":0,\"type\":\"Ride\",\"workout_type\":null,\"id\":154504250376823,\"external_id\":\"garmin_push_12345678987654321\",\"upload_id\":9.876543212345679e+20,\"start_date\":\"2018-05-02T12:15:09Z\",\"start_date_local\":\"2018-05-02T05:15:09Z\",\"timezone\":\"(GMT-08:00) America\\/Los_Angeles\",\"utc_offset\":-25200,\"start_latlng\":null,\"end_latlng\":null,\"location_city\":null,\"location_state\":null,\"location_country\":\"United States\",\"achievement_count\":0,\"kudos_count\":3,\"comment_count\":1,\"athlete_count\":1,\"photo_count\":0,\"map\":{\"id\":\"a12345678987654321\",\"summary_polyline\":null,\"resource_state\":2},\"trainer\":true,\"commute\":false,\"manual\":false,\"private\":false,\"flagged\":false,\"gear_id\":\"b12345678987654321\",\"from_accepted_tag\":false,\"average_speed\":5.54,\"max_speed\":11,\"average_cadence\":67.1,\"average_watts\":175.3,\"weighted_average_watts\":210,\"kilojoules\":788.7,\"device_watts\":true,\"has_heartrate\":true,\"average_heartrate\":140.3,\"max_heartrate\":178,\"max_watts\":406,\"pr_count\":0,\"total_photo_count\":1,\"has_kudoed\":false,\"suffer_score\":82},{\"resource_state\":2,\"athlete\":{\"id\":167560,\"resource_state\":1},\"name\":\"Bondcliff\",\"distance\":23676.5,\"moving_time\":5400,\"elapsed_time\":5400,\"total_elevation_gain\":0,\"type\":\"Ride\",\"workout_type\":null,\"id\":1234567809,\"external_id\":\"garmin_push_12345678987654321\",\"upload_id\":1234567819,\"start_date\":\"2018-04-30T12:35:51Z\",\"start_date_local\":\"2018-04-30T05:35:51Z\",\"timezone\":\"(GMT-08:00) America\\/Los_Angeles\",\"utc_offset\":-25200,\"start_latlng\":null,\"end_latlng\":null,\"location_city\":null,\"location_state\":null,\"location_country\":\"United States\",\"achievement_count\":0,\"kudos_count\":4,\"comment_count\":0,\"athlete_count\":1,\"photo_count\":0,\"map\":{\"id\":\"a12345689\",\"summary_polyline\":null,\"resource_state\":2},\"trainer\":true,\"commute\":false,\"manual\":false,\"private\":false,\"flagged\":false,\"gear_id\":\"b12345678912343\",\"from_accepted_tag\":false,\"average_speed\":4.385,\"max_speed\":8.8,\"average_cadence\":69.8,\"average_watts\":200,\"weighted_average_watts\":214,\"kilojoules\":1080,\"device_watts\":true,\"has_heartrate\":true,\"average_heartrate\":152.4,\"max_heartrate\":183,\"max_watts\":403,\"pr_count\":0,\"total_photo_count\":1,\"has_kudoed\":false,\"suffer_score\":162}]"
            val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
            with(preferences.edit()) {
                putString("activities", json)
                putLong("lastSync", System.currentTimeMillis() / 1000)
                commit()
            }*/
        }

        printButton = findViewById(R.id.printButton)
        printButton.setOnClickListener {
            var activities =
                getSharedPreferences("user", Context.MODE_PRIVATE).getString("activities", "[]")

            //temporary activity count reduction
            val activityArr = Gson().fromJson(activities, Array<JsonObject>::class.java)
            var activityList = activityArr.toMutableList()
            while (activityList.size > 10)
                activityList.removeAt(0)
            activities = Gson().toJson(activityList)

            val intent = Intent(this, ShowActActivity::class.java).apply {
                putExtra("activities", activities)
            }
            this.startActivity(intent)
        }

        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
            with(preferences.edit()) {
                remove("access_token")
                remove("refresh_token")
                remove("expires_at")
                remove("athlete")
                remove("activities")
                remove("lastSync")
                commit()
            }
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        }
    }

    private fun synchronizeActivities() {
        val lastSync = getSharedPreferences("user", Context.MODE_PRIVATE).getLong("lastSync", 0)
        val activityList = mutableListOf<JsonObject>()

        if (lastSync != 0L) {
            val jsonArray =
                JSONArray(
                    getSharedPreferences("user", Context.MODE_PRIVATE).getString(
                        "activities",
                        "[]"
                    )
                )
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i).toString()
                val activity = JsonParser.parseString(json).asJsonObject;
                activityList.add(activity)
            }
        }
        fun makeRequest(page: Int, after: Long, activityList: MutableList<JsonObject>) {
            val uri =
                Uri.Builder().scheme("https")
                    .authority("www.strava.com")
                    .path("/api/v3/athlete/activities")
                    .appendQueryParameter("after", lastSync.toString())
                    .appendQueryParameter("page", page.toString())
                    .appendQueryParameter("per_page", "200")
                    .appendQueryParameter(
                        "access_token",
                        getSharedPreferences("user", Context.MODE_PRIVATE).getString(
                            "access_token",
                            "x"
                        )
                    )
                    .build().toString()

            val request = JsonArrayRequest(
                Request.Method.GET, uri, null,
                Response.Listener<JSONArray> { response ->
                    val responseLength = response.length()
                    for (i in 0 until responseLength) {
                        val json = response.getJSONObject(i).toString()
                        val activity = JsonParser.parseString(json).asJsonObject;
                        activityList.add(activity)
                    }

                    if (responseLength != 200) {
                        val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
                        with(preferences.edit()) {
                            putString("activities", Gson().toJson(activityList))
                            putLong("lastSync", System.currentTimeMillis() / 1000)
                            commit()
                        }
                        Toast.makeText(this, "Synced activities", Toast.LENGTH_SHORT).show()
                    } else
                        makeRequest(page + 1, after, activityList)
                },
                Response.ErrorListener { err ->
                    Log.wtf("myTag", err.localizedMessage)
                    Toast.makeText(this, "Could not sync activities", Toast.LENGTH_SHORT).show()
                })
            val queue = newRequestQueue(applicationContext)
            queue.add(request)
        }

        makeRequest(1, lastSync, activityList)
    }
}