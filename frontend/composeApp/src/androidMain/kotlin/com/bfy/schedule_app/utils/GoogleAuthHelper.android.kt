package com.bfy.schedule_app.utils

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.bfy.schedule_app.MainActivity

actual fun signOutFromGoogle() {
    val context = MainActivity.context ?: return
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("430543234158-or99cp6a8okc9p1uj9tnen1amfm1ss91.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    googleSignInClient.signOut()
}
