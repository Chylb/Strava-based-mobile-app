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
import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.*

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
        }

        printButton = findViewById(R.id.printButton)
        printButton.setOnClickListener {
            var activities =
                getSharedPreferences("user", Context.MODE_PRIVATE).getString("activities", "[]")
            val activityList = JSONArray(activities)
            Log.wtf("myTag", "number of activities " + activityList.length())
            //Log.wtf("myTag", activities)
            Log.wtf(
                "myTag",
                getSharedPreferences("user", Context.MODE_PRIVATE).getString("athlete", "x")
            )
            Log.wtf(
                "myTag",
                getSharedPreferences("user", Context.MODE_PRIVATE).getString("access_token", "x")
            )
            Toast.makeText(this, activities, Toast.LENGTH_SHORT).show()
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
        val activityList = mutableListOf<JSONObject>()

        if (lastSync != 0L) {
            val jsonArray =
                JSONArray(
                    getSharedPreferences("user", Context.MODE_PRIVATE).getString(
                        "activities",
                        "[]"
                    )
                )
            for (i in 0 until jsonArray.length()) {
                val activity = jsonArray.getJSONObject(i)
                activityList.add(activity)
            }
        }
        fun makeRequest(page: Int, after: Long, activityList : MutableList<JSONObject>) {
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
                        val activity = response.getJSONObject(i)
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