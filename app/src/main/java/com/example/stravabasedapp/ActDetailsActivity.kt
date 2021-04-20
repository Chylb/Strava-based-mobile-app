package com.example.stravabasedapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.stravabasedapp.utils.secondsToString
import org.json.JSONObject

class ActDetailsActivity : AppCompatActivity() {
    private var actId: Long = 0
    private lateinit var activity: Activity

    private lateinit var nameView: TextView
    private lateinit var distanceView: TextView
    private lateinit var movingTimeView: TextView
    private lateinit var elapsedTimeView: TextView
    private lateinit var averageSpeedView: TextView
    private lateinit var maxSpeedView: TextView
    private lateinit var elevationGainView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_act_details)

        actId = intent.getLongExtra("id", 0L)
        activity = ActStorage.activities.find { it.id == actId }!!

        nameView = findViewById(R.id.activityName)
        distanceView = findViewById(R.id.distanceView)
        movingTimeView = findViewById(R.id.movingTimeView)
        averageSpeedView = findViewById(R.id.avgSpeedView)

        nameView.text = activity.actName
        distanceView.text = activity.distance.toString()
        movingTimeView.text =
            secondsToString(activity.movingTime)
        averageSpeedView.text = activity.avgSpeed.toString()

        //Log.wtf("myTag", "activity type ${activity.type}")
        if(activity.type == "Run") {
            val db = DataBaseHandler(this)
            val detailsJson = db.get(actId)

            if(detailsJson.isEmpty()) {
                downloadActivityDetails(actId)
            }
            else {
                showBestEfforts()
            }
        }
    }

    private fun showBestEfforts() {
        val db = DataBaseHandler(this)
        val json = db.get(actId)

        val actJsonObj = JSONObject(json)
        val bestEfforts = actJsonObj.getJSONArray("best_efforts")

        for (i in 0 until bestEfforts.length()) {
            val bestEffort = bestEfforts.getJSONObject(i)

            val name = bestEffort.getString("name")
            val pr_rank = bestEffort.get("pr_rank") // int or null
            val pr_rank_string = pr_rank.toString()
            val time = bestEffort.getInt("elapsed_time")
            Log.wtf("myTag", "$name $pr_rank_string $time")
        }
    }

    //downloads json of specific activity and stores it in database. Used because this is the only way to get activity best efforts
    private fun downloadActivityDetails(id: Long) {
        val uri =
            Uri.Builder().scheme("https")
                .authority("www.strava.com")
                .path("/api/v3/activities/$id")
                .appendQueryParameter(
                    "access_token",
                    getSharedPreferences("user", Context.MODE_PRIVATE).getString(
                        "access_token",
                        "x"
                    )
                )
                .build().toString()

        val request = JsonObjectRequest(
            Request.Method.GET, uri, null,
            Response.Listener<JSONObject> { response ->

                val db = DataBaseHandler(this)

                Log.wtf("myTag", response.toString())
                db.insert(id, response.toString())

                showBestEfforts()
            },
            Response.ErrorListener { err ->
                Log.wtf("myTag", err.localizedMessage)
                Toast.makeText(this, "Could not get activity details", Toast.LENGTH_SHORT).show()
            })
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(request)
    }
}