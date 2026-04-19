package com.example.playlist_maker_main.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlist_maker_main.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onShareClick: () -> Unit,
    onSupportClick: () -> Unit,
    onAgreementClick: () -> Unit
) {
    val state by viewModel.state.observeAsState()
    val isDarkTheme = state?.isDarkThemeOn == true

    val ypBlack = Color(0xFF1A1B22)
    val ypWhite = Color(0xFFFFFFFF)
    val ypBlue = Color(0xFF3772E7)

    val backgroundColor = if (isDarkTheme) ypBlack else ypWhite
    val textColor = if (isDarkTheme) ypWhite else ypBlack
    val iconColor = if (isDarkTheme) ypWhite else ypBlack

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.dark_theme),
                    fontSize = 16.sp,
                    color = textColor
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isChecked ->
                        viewModel.onThemeToggled(isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ypBlue,
                        checkedTrackColor = ypBlue.copy(alpha = 0.3f),
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = Color(0xFFAEAFB4),
                        uncheckedTrackColor = Color(0xFFE6E8EB),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            SettingsItem(
                label = stringResource(id = R.string.share),
                iconResId = R.drawable.share_img,
                textColor = textColor,
                iconColor = iconColor,
                onClick = onShareClick
            )

            SettingsItem(
                label = stringResource(id = R.string.support),
                iconResId = R.drawable.support_img,
                textColor = textColor,
                iconColor = iconColor,
                onClick = onSupportClick
            )

            SettingsItem(
                label = stringResource(id = R.string.user_agreement),
                iconResId = R.drawable.arrow_forvard_img,
                textColor = textColor,
                iconColor = iconColor,
                onClick = onAgreementClick
            )
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    iconResId: Int,
    textColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = textColor
        )
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}