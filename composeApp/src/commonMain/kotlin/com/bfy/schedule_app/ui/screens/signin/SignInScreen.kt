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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.components.*
import com.bfy.schedule_app.ui.theme.*

@Composable
fun SignInScreen(
    onSignIn: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("alex@example.com") }
    var password by remember { mutableStateOf("12345678") }

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
                selectedIndex = 0,
                onItemSelection = { if (it == 1) onSignUpClick() }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
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
            
            PrimaryButton(
                text = "Sign In",
                onClick = onSignIn
            )
            
            DividerWithText("OR")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GoogleSignInButton(
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextSecondary, fontSize = 12.sp)) {
                    append("Don't have an account? ")
                }
                withStyle(style = SpanStyle(color = PrimaryColor, fontSize = 12.sp)) {
                    append("Sign Up")
                }
            }
            Text(
                text = annotatedString,
                modifier = Modifier.clickable { onSignUpClick() }
            )
        }
    }
}
