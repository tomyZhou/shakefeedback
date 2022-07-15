package com.example.shakefeedback

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FeedbackManager(this).feedback()

        findViewById<TextView>(R.id.tv_next).setOnClickListener {
            val intent:Intent = Intent(this,SecondActivity::class.java)
            startActivity(intent)
        }

    }

}