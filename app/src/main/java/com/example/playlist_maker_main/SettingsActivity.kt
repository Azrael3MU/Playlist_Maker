package com.example.playlist_maker_main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        val backBtn = findViewById<ImageView>(R.id.back_button)
        val share = findViewById<LinearLayout>(R.id.share_app)
        val shareMessaqe = getString(R.string.share_link)
        val supportBtn = findViewById<LinearLayout>(R.id.support_btn)
        val agreementBtn = findViewById<LinearLayout>(R.id.agreement)


        backBtn.setOnClickListener{
            finish()
        }
        share.setOnClickListener{
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessaqe)
            startActivity(shareIntent)
        }
        supportBtn.setOnClickListener{
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            supportIntent.data = Uri.parse("mailto:")
            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_adress)))
            supportIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_title))
            supportIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.support_text))
            startActivity(supportIntent)
        }
        agreementBtn.setOnClickListener{
            val agreementIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.agreement_link)))
            startActivity(agreementIntent)
        }
    }
}