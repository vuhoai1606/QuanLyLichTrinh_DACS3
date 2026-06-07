package com.bfy.schedule_app.ui.screens.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.bfy.schedule_app.ui.components.GoogleSignInButton
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
    var showServerDialog by remember { mutableStateOf(false) }
    var serverIpInput by remember { mutableStateOf(SettingsManager.customServerIp) }
    
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
        // Settings icon at top-right
        IconButton(
            onClick = {
                serverIpInput = SettingsManager.customServerIp
                showServerDialog = true
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Server Settings",
                tint = Color(0xFF6E6E73),
                modifier = Modifier.size(22.dp)
            )
        }

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
                text = "Continue with Google",
                onTokenReceived = { token ->
                    if (token != null) {
                        viewModel.googleLogin(token)
                    }
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

    // Server IP configuration dialog
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            backgroundColor = Color(0xFF2C2C2E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Server Configuration",
                    color = Color(0xFFE2E2E6),
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter server IP address (leave empty for default 10.0.2.2)",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverIpInput,
                        onValueChange = { serverIpInput = it },
                        placeholder = {
                            Text("e.g. 192.168.1.100", color = Color(0xFF6E6E73))
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color(0xFFE2E2E6),
                            cursorColor = Color(0xFF59DBC7),
                            focusedBorderColor = Color(0xFF59DBC7),
                            unfocusedBorderColor = Color(0xFF6E6E73)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    SettingsManager.customServerIp = serverIpInput.trim()
                    showServerDialog = false
                }) {
                    Text("Save", color = Color(0xFF59DBC7))
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }
}

