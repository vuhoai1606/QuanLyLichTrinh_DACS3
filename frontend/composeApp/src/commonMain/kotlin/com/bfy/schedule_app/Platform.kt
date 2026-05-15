package com.bfy.schedule_app

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun BackHandlerWrapper(enabled: Boolean = true, onBack: () -> Unit)