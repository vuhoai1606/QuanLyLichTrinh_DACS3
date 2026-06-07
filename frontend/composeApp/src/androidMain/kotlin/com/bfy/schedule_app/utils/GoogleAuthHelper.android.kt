package com.bfy.schedule_app.utils

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.bfy.schedule_app.MainActivity

actual fun signOutFromGoogle() {
    val context = MainActivity.context ?: return
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("731620527212-10kqcai1ib22t3be0rimj085poa4h7ra.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    googleSignInClient.signOut()
}
