package com.example.playlist_maker_main.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.playlist_maker_main.App
import com.example.playlist_maker_main.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            val app = requireContext().applicationContext as App
            app.switchTheme(state.isDarkThemeOn)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                SettingsScreen(
                    viewModel = viewModel,
                    onShareClick = { shareApp() },
                    onSupportClick = { contactSupport() },
                    onAgreementClick = { openAgreement() }
                )
            }
        }
    }

    private fun shareApp() {
        val shareMessage = getString(R.string.share_link)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(shareIntent)
    }

    private fun contactSupport() {
        val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_adress)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_title))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_text))
        }
        startActivity(supportIntent)
    }

    private fun openAgreement() {
        val agreementIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.agreement_link))
        )
        startActivity(agreementIntent)
    }
}