package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bfy.schedule_app.ui.components.BfyButton
import com.bfy.schedule_app.ui.components.BfyButtonStyle
import com.bfy.schedule_app.ui.components.BfyTextField
import com.bfy.schedule_app.ui.model.AuthMode
import com.bfy.schedule_app.ui.state.AuthUiState
import com.bfy.schedule_app.ui.theme.BfyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.auth_subtitle
import schedule_app.composeapp.generated.resources.auth_title
import schedule_app.composeapp.generated.resources.confirm_password
import schedule_app.composeapp.generated.resources.create_account
import schedule_app.composeapp.generated.resources.email
import schedule_app.composeapp.generated.resources.field_required
import schedule_app.composeapp.generated.resources.full_name
import schedule_app.composeapp.generated.resources.invalid_email
import schedule_app.composeapp.generated.resources.login
import schedule_app.composeapp.generated.resources.password
import schedule_app.composeapp.generated.resources.password_not_match
import schedule_app.composeapp.generated.resources.password_rule
import schedule_app.composeapp.generated.resources.sign_in
import schedule_app.composeapp.generated.resources.sign_up

class AuthScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var uiState by remember { mutableStateOf(AuthUiState()) }
        val scope = rememberCoroutineScope()
        val requiredMessage = stringResource(Res.string.field_required)
        val invalidEmailMessage = stringResource(Res.string.invalid_email)
        val passwordRuleMessage = stringResource(Res.string.password_rule)
        val mismatchMessage = stringResource(Res.string.password_not_match)

        fun submit() {
            val errors = linkedMapOf<String, String>()

            if (uiState.mode == AuthMode.SIGN_UP && uiState.fullName.isBlank()) {
                errors["fullName"] = requiredMessage
            }
            if (uiState.email.isBlank()) {
                errors["email"] = requiredMessage
            } else if (!uiState.email.contains("@") || !uiState.email.contains(".")) {
                errors["email"] = invalidEmailMessage
            }
            if (uiState.password.isBlank()) {
                errors["password"] = requiredMessage
            } else {
                val hasLetter = uiState.password.any { it.isLetter() }
                val hasDigit = uiState.password.any { it.isDigit() }
                if (uiState.password.length < 8 || !hasLetter || !hasDigit) {
                    errors["password"] = passwordRuleMessage
                }
            }
            if (uiState.mode == AuthMode.SIGN_UP) {
                if (uiState.confirmPassword.isBlank()) {
                    errors["confirmPassword"] = requiredMessage
                } else if (uiState.password != uiState.confirmPassword) {
                    errors["confirmPassword"] = mismatchMessage
                }
            }

            if (errors.isNotEmpty()) {
                uiState = uiState.copy(fieldErrors = errors)
                return
            }

            uiState = uiState.copy(isLoading = true, fieldErrors = emptyMap(), errorMessage = null)
            scope.launch {
                delay(900)
                uiState = uiState.copy(isLoading = false)
                navigator.replaceAll(MainShellScreen())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(BfyTheme.dimens.spacing16),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = BfyTheme.dimens.spacing4),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(BfyTheme.dimens.spacing20),
                    verticalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing12)
                ) {
                    Text(
                        text = stringResource(Res.string.auth_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.auth_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(BfyTheme.dimens.spacing8)) {
                        BfyButton(
                            text = stringResource(Res.string.sign_in),
                            style = if (uiState.mode == AuthMode.SIGN_IN) BfyButtonStyle.PRIMARY else BfyButtonStyle.SECONDARY,
                            onClick = { uiState = uiState.copy(mode = AuthMode.SIGN_IN, fieldErrors = emptyMap()) },
                            modifier = Modifier.weight(1f)
                        )
                        BfyButton(
                            text = stringResource(Res.string.sign_up),
                            style = if (uiState.mode == AuthMode.SIGN_UP) BfyButtonStyle.PRIMARY else BfyButtonStyle.SECONDARY,
                            onClick = { uiState = uiState.copy(mode = AuthMode.SIGN_UP, fieldErrors = emptyMap()) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (uiState.mode == AuthMode.SIGN_UP) {
                        BfyTextField(
                            value = uiState.fullName,
                            onValueChange = { uiState = uiState.copy(fullName = it) },
                            label = stringResource(Res.string.full_name),
                            errorText = uiState.fieldErrors["fullName"]
                        )
                    }

                    BfyTextField(
                        value = uiState.email,
                        onValueChange = { uiState = uiState.copy(email = it) },
                        label = stringResource(Res.string.email),
                        errorText = uiState.fieldErrors["email"]
                    )
                    BfyTextField(
                        value = uiState.password,
                        onValueChange = { uiState = uiState.copy(password = it) },
                        label = stringResource(Res.string.password),
                        errorText = uiState.fieldErrors["password"],
                        isPassword = true
                    )
                    if (uiState.mode == AuthMode.SIGN_UP) {
                        BfyTextField(
                            value = uiState.confirmPassword,
                            onValueChange = { uiState = uiState.copy(confirmPassword = it) },
                            label = stringResource(Res.string.confirm_password),
                            errorText = uiState.fieldErrors["confirmPassword"],
                            isPassword = true
                        )
                    }

                    BfyButton(
                        text = if (uiState.mode == AuthMode.SIGN_IN) stringResource(Res.string.login) else stringResource(Res.string.create_account),
                        onClick = { if (!uiState.isLoading) submit() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
