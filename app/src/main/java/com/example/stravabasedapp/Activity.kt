package com.example.stravabasedapp


class Activity(
    var actName: String, var type: String, var dateVal: String,
    var distanceVal: Double, var elevationGain: Int,
    var movingTime: Int, var elapsedTime: Int,
    val avgSpeed: Double, var maxSpeed: Double,
    val mapPolyline: String
) {
    val name = actName
    val distance = distanceVal / 1000

    val time = movingTime

    val date = dateVal.slice(0..9)
    val speed = avgSpeed * 3.6
}