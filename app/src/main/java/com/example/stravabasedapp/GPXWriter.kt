package com.example.stravabasedapp

import android.location.Location
import android.util.Config
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object GPX {
    private val TAG = GPX::class.java.name
    fun writePath(
        file: File?,
        n: String,
        points: List<Location>
    ) {
        val header =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/com.example.stravabasedapp.GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/com.example.stravabasedapp.GPX/1/1 http://www.topografix.com/com.example.stravabasedapp.GPX/1/1/gpx.xsd\"><trk>\n"
        val name = "<name>$n</name><trkseg>\n"
        var segments = ""
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        for (l in points) {
            segments += "<trkpt lat=\"" + l.latitude + "\" lon=\"" + l.longitude + "\"><time>" + df.format(
                Date(l.time)
            ) + "</time></trkpt>\n"
        }
        val footer = "</trkseg></trk></gpx>"
        try {
            val writer = FileWriter(file, false)
            writer.append(header)
            writer.append(name)
            writer.append(segments)
            writer.append(footer)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error Writting Path", e)
        }
    }
}