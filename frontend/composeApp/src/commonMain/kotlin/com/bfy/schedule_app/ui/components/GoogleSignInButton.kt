package com.bfy.schedule_app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun GoogleSignInButton(
    text: String,
    modifier: Modifier = Modifier,
    onTokenReceived: (String?) -> Unit
)
