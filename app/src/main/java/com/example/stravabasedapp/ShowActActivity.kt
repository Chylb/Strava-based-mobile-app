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

        for(act in ActStorage.activities.reversed()) {
            adapter.dataSet.add(act)
        }
    }
}