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


@Composable
fun AuthenticationScreen(
    onSignUp: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val viewModel: AuthViewModel = viewModel { AuthViewModel() }
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(horizontal = 24.dp, vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SegmentedControl(
                items = listOf("Sign In", "Sign Up"),
                selectedIndex = 1,
                onItemSelection = { if (it == 0) onSignInClick() }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            AuthTextField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Alex Rivers"
            )


            AuthTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "alex@example.com"
            )
            
            AuthTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••"
            )
            
            if (uiState.error != null) {
                Text(uiState.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            PrimaryButton(
                text = if (uiState.isLoading) "Signing Up..." else "Sign Up",
                onClick = { viewModel.register(fullName, email, password) }
            )

            
            DividerWithText("OR")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GoogleSignInButton(
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextSecondary, fontSize = 12.sp)) {
                    append("Already have an account? ")
                }
                withStyle(style = SpanStyle(color = PrimaryColor, fontSize = 12.sp)) {
                    append("Sign In")
                }
            }
            Text(
                text = annotatedString,
                modifier = Modifier.clickable { onSignInClick() }
            )
        }
    }
}
