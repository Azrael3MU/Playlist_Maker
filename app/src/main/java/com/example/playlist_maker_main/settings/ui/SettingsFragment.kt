package com.example.playlist_maker_main.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.playlist_maker_main.App
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        initListeners()
        observeViewModel()
    }

    private fun initListeners() = with(binding) {
        shareApp.setOnClickListener {
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

        agreement.setOnClickListener {
            val agreementIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.agreement_link))
            )
            startActivity(agreementIntent)
        }

        themeSwithcer.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeToggled(isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: SettingsScreenState) = with(binding) {
        val app = requireContext().applicationContext as App

        if (themeSwithcer.isChecked != state.isDarkThemeOn) {
            themeSwithcer.isChecked = state.isDarkThemeOn
        }

        app.switchTheme(state.isDarkThemeOn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
