package com.bfy.schedule_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bfy.schedule_app.ui.data.FakeBfyData
import com.bfy.schedule_app.ui.theme.BfyTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import schedule_app.composeapp.generated.resources.Res
import schedule_app.composeapp.generated.resources.app_name
import schedule_app.composeapp.generated.resources.splash_loading

class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            delay(1200)
            if (FakeBfyData.hasValidToken) {
                navigator.replaceAll(MainShellScreen())
            } else {
                navigator.replaceAll(AuthScreen())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(BfyTheme.dimens.spacing24),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displayMedium
            )
            CircularProgressIndicator(modifier = Modifier.padding(top = BfyTheme.dimens.spacing16))
            Text(
                text = stringResource(Res.string.splash_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = BfyTheme.dimens.spacing16)
            )
        }
    }
}
