package com.bfy.schedule_app.ui.screens.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.components.*
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.bfy.schedule_app.utils.Localization
import com.bfy.schedule_app.utils.SettingsManager


@Composable
fun SignInScreen(
    onSignIn: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf(if (SettingsManager.rememberMeEnabled) SettingsManager.rememberedEmail else "") }
    var password by remember { mutableStateOf(if (SettingsManager.rememberMeEnabled) SettingsManager.rememberedPassword else "") }
    var rememberMe by remember { mutableStateOf(SettingsManager.rememberMeEnabled) }
    
    val viewModel: AuthViewModel = viewModel { AuthViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onSignIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SegmentedControl(
                items = listOf(Localization.get("sign_in"), Localization.get("sign_up")),
                selectedIndex = 0,
                onItemSelection = { if (it == 1) onSignUpClick() }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            AuthTextField(
                label = Localization.get("email_address"),
                value = email,
                onValueChange = { email = it },
                placeholder = "alex@example.com"
            )
            
            AuthTextField(
                label = Localization.get("password"),
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                isPassword = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material.Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = androidx.compose.material.CheckboxDefaults.colors(
                            checkedColor = PrimaryColor,
                            uncheckedColor = BorderColor,
                            checkmarkColor = TextDark
                        )
                    )
                    Text("Remember me", color = TextSecondary, fontSize = 12.sp)
                }
                Text(
                    text = Localization.get("forgot_password") ?: "Forgot Password?",
                    color = PrimaryColor,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (uiState.error != null) {
                Text(uiState.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            PrimaryButton(
                text = if (uiState.isLoading) Localization.get("signing_in") else Localization.get("sign_in"),
                onClick = { 
                    SettingsManager.rememberMeEnabled = rememberMe
                    if (rememberMe) {
                        SettingsManager.rememberedEmail = email
                        SettingsManager.rememberedPassword = password
                    } else {
                        SettingsManager.rememberedEmail = ""
                        SettingsManager.rememberedPassword = ""
                    }
                    viewModel.login(email, password) 
                }
            )
            
            DividerWithText(Localization.get("or"))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GoogleSignInButton(
                onClick = {
                    // Mock Google Login for demo
                    viewModel.googleLogin("google_123", "google_user@gmail.com", "Google User")
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextSecondary, fontSize = 12.sp)) {
                    append(Localization.get("dont_have_account"))
                }
                withStyle(style = SpanStyle(color = PrimaryColor, fontSize = 12.sp)) {
                    append(Localization.get("sign_up"))
                }
            }
            Text(
                text = annotatedString,
                modifier = Modifier.clickable { onSignUpClick() }
            )
        }
    }
}
