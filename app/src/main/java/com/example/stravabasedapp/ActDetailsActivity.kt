package com.example.stravabasedapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
        movingTimeView.text = secondsToString(activity.movingTime)
        averageSpeedView.text = activity.avgSpeed.toString()
    }
}