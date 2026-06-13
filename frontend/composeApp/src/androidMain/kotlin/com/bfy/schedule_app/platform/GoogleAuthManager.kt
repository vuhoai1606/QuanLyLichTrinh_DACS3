package com.bfy.schedule_app.platform

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

class GoogleAuthManager(private val context: Context) {

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(
            Scope("https://www.googleapis.com/auth/tasks"),
            Scope("https://www.googleapis.com/auth/calendar")
        )
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun getSignedInAccountFromIntent(data: Intent?): GoogleSignInAccount? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.await()
        } catch (e: ApiException) {
            e.printStackTrace()
            null
        }
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    fun signOut() {
        googleSignInClient.signOut()
    }
}
