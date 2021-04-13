package com.example.stravabasedapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class ShowActActivity : AppCompatActivity() {
    internal lateinit var activitiesRecyclerView: RecyclerView
    internal lateinit var adapter: ActivitiesAdapter
    internal var activitiesList = mutableListOf<Activity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_act)

        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView)
        activitiesRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ActivitiesAdapter(activitiesList, this)
        activitiesRecyclerView.adapter = adapter

        val act = intent.getStringExtra("activities")
        val activityList = JSONArray(act)
        extractActivities(activityList)
    }


    private fun extractActivities(activitiesList: JSONArray) {
        for (i in 0 until activitiesList.length()) {
            val activity = activitiesList.getJSONObject(i)
            val name = activity.getString("name")
            val distance = activity.getDouble("distance")
            val time = activity.getDouble("elapsed_time")
            val date = activity.getString("start_date")

            val actObject = Activity(name, distanceVal = distance, timeVal = time, dateVal = date)
            adapter.dataSet.add(actObject)
        }
    }
}