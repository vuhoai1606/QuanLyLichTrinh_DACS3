package com.bfy.schedule_app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform