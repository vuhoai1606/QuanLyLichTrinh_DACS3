package com.bfy.schedule_app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bfy.schedule_app.ui.screens.authentication.AuthenticationScreen
import com.bfy.schedule_app.ui.screens.homedashboard.HomeDashboardScreen
import com.bfy.schedule_app.ui.screens.signin.SignInScreen

@Composable
fun AppNavigation() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var showSignIn by remember { mutableStateOf(true) }

    if (isAuthenticated) {
        HomeDashboardScreen()
    } else if (showSignIn) {
        SignInScreen(
            onSignIn = { isAuthenticated = true },
            onSignUpClick = { showSignIn = false }
        )
    } else {
        AuthenticationScreen(
            onSignUp = { isAuthenticated = true },
            onSignInClick = { showSignIn = true }
        )
    }
}
