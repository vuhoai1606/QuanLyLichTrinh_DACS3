package com.bfy.schedule_app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import com.bfy.schedule_app.ui.screens.SplashScreen
import com.bfy.schedule_app.ui.theme.BFYTheme

@Composable
@Preview
fun App() {
    BFYTheme {
        Navigator(SplashScreen())
    }
}