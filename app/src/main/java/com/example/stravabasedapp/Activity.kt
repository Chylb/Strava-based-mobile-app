package com.example.stravabasedapp


import kotlin.math.floor

class Activity(var actName: String, var distanceVal: Double, var timeVal:Double,
                var dateVal: String, val speedVal : Double){
    val name = actName
    val distance = distanceVal / 1000

    val time = timeVal
    val timeHours = floor(time / 3600).toInt()
    val timeMinutes = (time - timeHours*3600).toInt()

    val date = dateVal.slice(0..9)
    val speed = speedVal * 3.6
}