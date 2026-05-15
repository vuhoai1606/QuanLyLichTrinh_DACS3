package com.bfy.schedule_app

import androidx.compose.runtime.Composable

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@Composable
actual fun BackHandlerWrapper(enabled: Boolean, onBack: () -> Unit) {
    // No-op for Wasm
}