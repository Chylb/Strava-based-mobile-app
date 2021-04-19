package com.example.stravabasedapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class GetName : AppCompatActivity() {
    lateinit var nameOfActivity: EditText
    lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_name)
        nameOfActivity = findViewById(R.id.nameOfActivity)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {
            if (nameOfActivity.text.isNotEmpty()) {
                val intent = Intent(this, TrackActivity::class.java).apply {
                    putExtra("nameAct", nameOfActivity.text.toString())
                }
                this.startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, "Activity name is empty",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}