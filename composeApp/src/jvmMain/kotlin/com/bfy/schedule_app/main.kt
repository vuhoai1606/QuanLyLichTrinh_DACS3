package com.bfy.schedule_app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "schedule_app",
    ) {
        App()
    }
}