package com.example.stravabasedapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class ActivitiesAdapter(var dataSet: MutableList<Activity>, val context: Context) :
    RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>()  {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val currencyName: TextView = view.findViewById(R.id.currencyName)
        val actName: TextView = view.findViewById(R.id.activityName)
        val distance: TextView = view.findViewById(R.id.distanceValue)
        val pace: TextView = view.findViewById(R.id.speedValue)
        val time: TextView = view.findViewById(R.id.timeValue)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.activity_row, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val df = DecimalFormat("0.##")

        val activity = dataSet[position]
        viewHolder.actName.text = context.getString(R.string.activityname,
            activity.name, activity.date)
        viewHolder.distance.text = context.getString(R.string.distance,
            df.format(activity.distance))
        viewHolder.pace.text = context.getString(R.string.speed,
            df.format(activity.speed))
        viewHolder.time.text = secondsToString(activity.elapsedTime)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
