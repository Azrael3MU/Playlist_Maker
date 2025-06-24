package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlist_maker_main.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val search_btn = findViewById<Button>(R.id.search)
        val library_btn = findViewById<Button>(R.id.library)
        val settings_btn = findViewById<Button>(R.id.settings)

        val searchClickListener: View.OnClickListener = object : View.OnClickListener{
            override fun onClick(v: View?) {
                Toast.makeText(this@MainActivity, "Нажал на поиск!", Toast.LENGTH_SHORT).show()
            }
        }
        search_btn.setOnClickListener(searchClickListener)
        library_btn.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажал на медиатеку!", Toast.LENGTH_SHORT).show()
        }
        settings_btn.setOnClickListener{
            Toast.makeText(this@MainActivity, "Нажал на настройки!",Toast.LENGTH_SHORT).show()
        }
    }
}