package com.bfy.schedule_app.ui.screens.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bfy.schedule_app.ui.components.AuthTextField
import com.bfy.schedule_app.ui.components.PrimaryButton
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.AuthViewModel
import com.bfy.schedule_app.utils.Localization
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRequester

enum class ForgotPasswordStep {
    EMAIL, OTP, RESET, SUCCESS
}

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel { AuthViewModel() }
) {
    var step by remember { mutableStateOf(ForgotPasswordStep.EMAIL) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(24.dp)
    ) {
        if (step != ForgotPasswordStep.SUCCESS) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.clickable { 
                    if (step == ForgotPasswordStep.EMAIL) onBackToLogin()
                    else step = ForgotPasswordStep.values()[step.ordinal - 1]
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (step) {
            ForgotPasswordStep.EMAIL -> {
                Text(
                    Localization.get("forgot_password_title") ?: "Forgot Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    Localization.get("forgot_password_desc") ?: "Enter your email to receive a 6-digit OTP code.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                AuthTextField(
                    label = Localization.get("email_address"),
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "alex@example.com"
                )

                if (uiState.error != null) {
                    Text(uiState.error!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = if (uiState.isLoading) "Sending..." else "Send OTP",
                    onClick = {
                        viewModel.forgotPassword(email) { step = ForgotPasswordStep.OTP }
                    }
                )
            }

            ForgotPasswordStep.OTP -> {
                Text(
                    Localization.get("verify_otp_title") ?: "Verify OTP",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "We've sent a code to $email",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
                val otpLength = 6

                LaunchedEffect(Unit) {
                    try {
                        focusRequester.requestFocus()
                    } catch (e: Exception) {}
                }

                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = otp,
                        onValueChange = { newValue ->
                            if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
                                otp = newValue
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(focusRequester)
                            .alpha(0f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until otpLength) {
                            val digit = otp.getOrNull(i)?.toString() ?: ""
                            val isCurrent = i == otp.length
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceColor)
                                    .border(
                                        width = 2.dp,
                                        color = if (isCurrent) PrimaryColor else BorderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        try {
                                            focusRequester.requestFocus()
                                        } catch (e: Exception) {}
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    Text(uiState.error!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = if (uiState.isLoading) "Verifying..." else "Verify",
                    onClick = {
                        viewModel.verifyOtp(email, otp) { step = ForgotPasswordStep.RESET }
                    }
                )
            }

            ForgotPasswordStep.RESET -> {
                Text(
                    Localization.get("reset_password_title") ?: "Reset Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Set your new password below.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                AuthTextField(
                    label = "New Password",
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "••••••••"
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    label = "Confirm Password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "••••••••"
                )

                if (uiState.error != null) {
                    Text(uiState.error!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = if (uiState.isLoading) "Resetting..." else "Reset Password",
                    onClick = {
                        if (newPassword == confirmPassword) {
                            viewModel.resetPassword(email, otp, newPassword) { step = ForgotPasswordStep.SUCCESS }
                        }
                    }
                )
            }

            ForgotPasswordStep.SUCCESS -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Success!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Your password has been reset successfully.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        PrimaryButton(
                            text = "Back to Login",
                            onClick = onBackToLogin
                        )
                    }
                }
            }
        }
    }
}
