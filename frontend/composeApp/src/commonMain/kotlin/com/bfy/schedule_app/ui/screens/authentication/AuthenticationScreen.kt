package com.bfy.schedule_app.ui.screens.authentication

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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh


@Composable
fun AuthenticationScreen(
    onSignUp: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var dob by remember { mutableStateOf("") }
    
    var captchaNum1 by remember { mutableStateOf((2..9).random()) }
    var captchaNum2 by remember { mutableStateOf((2..9).random()) }
    var captchaAnswer by remember { mutableStateOf("") }

    val viewModel: AuthViewModel = viewModel { AuthViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onSignUp()
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
                .padding(horizontal = 24.dp, vertical = 20.dp), // reduced vertical padding to fit more fields
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SegmentedControl(
                items = listOf(Localization.get("sign_in"), Localization.get("sign_up")),
                selectedIndex = 1,
                onItemSelection = { if (it == 0) onSignInClick() }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            AuthTextField(
                label = Localization.get("full_name"),
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Alex Rivers"
            )

            AuthTextField(
                label = Localization.get("email_address"),
                value = email,
                onValueChange = { email = it },
                placeholder = "alex@example.com"
            )
            
            // Gender field
            Text("Gender", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Male", "Female", "Other").forEach { g ->
                    val isSelected = gender == g
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryColor else SurfaceColor)
                            .border(1.dp, if (isSelected) PrimaryColor else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { gender = g }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(g, color = if (isSelected) TextDark else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Date of birth
            AuthTextField(
                label = "Date of Birth (DD/MM/YYYY)",
                value = dob,
                onValueChange = { dob = it },
                placeholder = "15/08/2000"
            )

            AuthTextField(
                label = Localization.get("password"),
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                isPassword = true
            )

            AuthTextField(
                label = Localization.get("confirm_password") ?: "Confirm Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "••••••••",
                isPassword = true
            )
            
            // Captcha field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    text = "Security Captcha: What is $captchaNum1 + $captchaNum2?",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = captchaAnswer,
                        onValueChange = { captchaAnswer = it },
                        placeholder = { Text("Answer", color = BorderColor) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = SurfaceColor,
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = BorderColor,
                            textColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        captchaNum1 = (2..9).random()
                        captchaNum2 = (2..9).random()
                        captchaAnswer = ""
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = "Refresh Captcha", tint = PrimaryColor)
                    }
                }
            }
            
            if (uiState.error != null || localError != null) {
                Text(uiState.error ?: localError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            PrimaryButton(
                text = if (uiState.isLoading) Localization.get("signing_up") else Localization.get("sign_up"),
                onClick = { 
                    val captchaAnswerInt = captchaAnswer.toIntOrNull()
                    if (password != confirmPassword) {
                        localError = "Passwords do not match"
                    } else if (email.isBlank() || !email.contains("@")) {
                        localError = "Invalid email format"
                    } else if (captchaAnswerInt == null || captchaAnswerInt != (captchaNum1 + captchaNum2)) {
                        localError = "Incorrect Captcha answer"
                        // refresh captcha
                        captchaNum1 = (2..9).random()
                        captchaNum2 = (2..9).random()
                        captchaAnswer = ""
                    } else {
                        localError = null
                        viewModel.register(fullName, email, password, gender, dob) 
                    }
                }
            )
            
            DividerWithText(Localization.get("or"))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GoogleSignInButton(
                onClick = {
                    viewModel.googleLogin("google_123", "google_user@gmail.com", "Google User")
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextSecondary, fontSize = 12.sp)) {
                    append(Localization.get("already_have_account"))
                }
                withStyle(style = SpanStyle(color = PrimaryColor, fontSize = 12.sp)) {
                    append(Localization.get("sign_in"))
                }
            }
            Text(
                text = annotatedString,
                modifier = Modifier.clickable { onSignInClick() }
            )
        }
    }
}
