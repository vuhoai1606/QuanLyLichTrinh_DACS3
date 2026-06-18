package com.bfy.schedule_app.platform

object FocusSessionSharedState {
    var isGiveUpTriggered: Boolean = false
    var isCompletedTriggered: Boolean = false
    var timeLeftSeconds: Int = -1
    var isUserLeaving: Boolean = false
}
