package com.bfy.schedule_app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bfy.schedule_app.ui.AppNavigation
import com.bfy.schedule_app.ui.theme.AppTheme
import com.bfy.schedule_app.utils.SettingsManager

@Composable
@Preview
fun App() {
    AppTheme(darkTheme = SettingsManager.isDarkTheme) {
        AppNavigation()
    }
}
