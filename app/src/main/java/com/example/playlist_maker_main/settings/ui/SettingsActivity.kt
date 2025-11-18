package com.example.playlist_maker_main.settings.ui

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
import androidx.lifecycle.ViewModelProvider
import com.example.playlist_maker_main.App
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.creator.Creator
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var backBtn: ImageView
    private lateinit var share: LinearLayout
    private lateinit var supportBtn: LinearLayout
    private lateinit var agreementBtn: LinearLayout
    private lateinit var settingsRoot: LinearLayout
    private lateinit var themeSwitcher: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        initViews()
        initInsets()

        val themeInteractor = Creator.provideThemeInteractor(this)
        val factory = SettingsViewModelFactory(themeInteractor)
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        initListeners()
        observeViewModel()
    }

    private fun initViews() {
        backBtn = findViewById(R.id.back_button)
        share = findViewById(R.id.share_app)
        supportBtn = findViewById(R.id.support_btn)
        agreementBtn = findViewById(R.id.agreement)
        settingsRoot = findViewById(R.id.settingsRoot)
        themeSwitcher = findViewById(R.id.themeSwithcer)
    }

    private fun initInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(settingsRoot) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

    private fun initListeners() {
        backBtn.setOnClickListener { finish() }

        share.setOnClickListener {
            val shareMessage = getString(R.string.share_link)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(shareIntent)
        }

        supportBtn.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_adress)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_title))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_text))
            }
            startActivity(supportIntent)
        }

        agreementBtn.setOnClickListener {
            val agreementIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.agreement_link))
            )
            startActivity(agreementIntent)
        }

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeToggled(isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            render(state)
        }
    }

    private fun render(state: SettingsScreenState) {
        val app = applicationContext as App

        if (themeSwitcher.isChecked != state.isDarkThemeOn) {
            themeSwitcher.isChecked = state.isDarkThemeOn
        }

        app.switchTheme(state.isDarkThemeOn)
    }
}
