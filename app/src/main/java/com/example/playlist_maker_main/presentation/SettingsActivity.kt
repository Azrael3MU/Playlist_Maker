package com.example.playlist_maker_main.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlist_maker_main.App
import com.example.playlist_maker_main.R
import com.google.android.material.switchmaterial.SwitchMaterial

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
        val settingsRoot = findViewById<LinearLayout>(R.id.settingsRoot)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwithcer)

        val app = applicationContext as App
        themeSwitcher.isChecked = app.darkTheme

        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
            recreate()
        }

        ViewCompat.setOnApplyWindowInsetsListener(settingsRoot) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left + v.paddingLeft,
                top = bars.top + v.paddingTop,
                right = bars.right + v.paddingRight,
                bottom = bars.bottom + v.paddingBottom
            )
            insets
        }


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