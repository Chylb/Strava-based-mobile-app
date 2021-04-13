package com.example.stravabasedapp


import kotlin.math.floor

class Activity(var actName: String, var distanceVal: Double,
               var paceVal: String = "", var timeVal:Double,
                var dateVal: String){
    val name = actName
    val distance = distanceVal / 1000
    val pace = paceVal

    val time = timeVal
    val timeHours = floor(time / 3600).toInt()
    val timeMinutes = (time - timeHours*3600).toInt()

    val date = dateVal.slice(0..9)

    //TemporalAccessor parsed = formatter.parse(dateVal)
}