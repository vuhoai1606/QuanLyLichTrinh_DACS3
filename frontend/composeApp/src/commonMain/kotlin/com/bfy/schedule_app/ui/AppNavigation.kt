package com.bfy.schedule_app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bfy.schedule_app.ui.screens.authentication.AuthenticationScreen
import com.bfy.schedule_app.ui.screens.homedashboard.HomeDashboardScreen
import com.bfy.schedule_app.ui.screens.signin.SignInScreen
import com.bfy.schedule_app.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
    val uiState by authViewModel.uiState.collectAsState()
    
    var showSignIn by remember { mutableStateOf(true) }
    var showForgotPassword by remember { mutableStateOf(false) }

    if (uiState.isAuthenticated) {
        HomeDashboardScreen(
            userId = uiState.userId ?: "",
            onLogout = { 
                com.bfy.schedule_app.utils.signOutFromGoogle()
                authViewModel.logout() 
            }
        )
    } else if (showForgotPassword) {
        com.bfy.schedule_app.ui.screens.forgotpassword.ForgotPasswordScreen(
            onBackToLogin = { showForgotPassword = false },
            viewModel = authViewModel
        )
    } else if (showSignIn) {
        SignInScreen(
            onSignIn = { /* authViewModel handles it */ },
            onSignUpClick = { showSignIn = false },
            onForgotPasswordClick = { showForgotPassword = true }
        )
    } else {
        AuthenticationScreen(
            onSignUp = { /* authViewModel handles it */ },
            onSignInClick = { showSignIn = true }
        )
    }
}
