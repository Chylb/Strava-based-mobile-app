package com.example.stravabasedapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var connectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val clientId = BuildConfig.CLIENT_ID
        val redirectUri = "strava-based-app://callback"

        connectButton = findViewById(R.id.connectButton)
        connectButton.setOnClickListener {
            intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.strava.com/oauth/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&approval_prompt=force&scope=activity:read_all")
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val uri = intent.data
        val code = uri?.getQueryParameter("code")

        Log.wtf("myTag", uri.toString())
        Log.wtf("myTag", code.toString())

        if (uri != null && code != null)
            login(code)
    }

    private fun login(code: String) {
        //Toast.makeText(this, "Trying to login", Toast.LENGTH_SHORT).show()
        val clientId = BuildConfig.CLIENT_ID
        val clientSecret = BuildConfig.CLIENT_SECRET
        val url =
            "https://www.strava.com/oauth/token?client_id=$clientId&client_secret=$clientSecret&code=$code&grant_type=authorization_code"

        val request = JsonObjectRequest(
            Request.Method.POST, url, null,
            Response.Listener<JSONObject> { response ->
                val accessToken = response.getString("access_token")
                val refreshToken = response.getString("refresh_token")
                val expiresAt = response.getLong("expires_at")
                val athlete = response.getString("athlete")

                val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
                with(preferences.edit()) {
                    putString("access_token", accessToken)
                    putString("refresh_token", refreshToken)
                    putLong("expires_at", expiresAt)
                    putString("athlete", athlete.toString())
                    commit()
                }

                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
                finish()
            },
            Response.ErrorListener { err ->
                Log.wtf("myTag", "Login error")
                Log.wtf("myTag", err.localizedMessage)
                Toast.makeText(this, "Error. Could not sign in", Toast.LENGTH_SHORT).show()
            })

        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(request)
    }
}