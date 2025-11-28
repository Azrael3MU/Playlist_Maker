package com.example.playlist_maker_main.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.playlist_maker_main.App
import com.example.playlist_maker_main.R
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var share: LinearLayout
    private lateinit var supportBtn: LinearLayout
    private lateinit var agreementBtn: LinearLayout
    private lateinit var settingsRoot: LinearLayout
    private lateinit var themeSwitcher: SwitchMaterial

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        share = view.findViewById(R.id.share_app)
        supportBtn = view.findViewById(R.id.support_btn)
        agreementBtn = view.findViewById(R.id.agreement)
        settingsRoot = view.findViewById(R.id.settingsRoot)
        themeSwitcher = view.findViewById(R.id.themeSwithcer)
    }

    private fun initListeners() {
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
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: SettingsScreenState) {
        val app = requireContext().applicationContext as App

        if (themeSwitcher.isChecked != state.isDarkThemeOn) {
            themeSwitcher.isChecked = state.isDarkThemeOn
        }

        app.switchTheme(state.isDarkThemeOn)
    }
}
