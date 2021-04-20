package com.example.stravabasedapp.utils

fun secondsToString(seconds: Int): String {
    val h = seconds / 3600;
    val m = (seconds - 3600 * h) / 60;
    val s = (seconds - 3600 * h - 60 * m);

    var res = s.toString() + "s";

    if(seconds >= 60)
        res = m.toString() + "m " + res;

    if (h > 0)
        res = h.toString() + "h " + res;

    return res;
}