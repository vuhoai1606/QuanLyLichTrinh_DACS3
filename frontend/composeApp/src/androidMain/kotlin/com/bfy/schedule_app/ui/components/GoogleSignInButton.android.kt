package com.bfy.schedule_app.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
actual fun GoogleSignInButton(
    text: String,
    modifier: Modifier,
    onTokenReceived: (String?) -> Unit
) {
    val context = LocalContext.current
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("731620527212-10kqcai1ib22t3be0rimj085poa4h7ra.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account?.idToken == null) {
                    android.widget.Toast.makeText(context, "idToken is null. Check Web Client ID.", android.widget.Toast.LENGTH_LONG).show()
                }
                onTokenReceived(account?.idToken)
            } catch (e: ApiException) {
                e.printStackTrace()
                val statusCode = com.google.android.gms.common.api.CommonStatusCodes.getStatusCodeString(e.statusCode)
                android.widget.Toast.makeText(context, "Google Sign In Failed: " + statusCode + " (" + e.statusCode + ")", android.widget.Toast.LENGTH_LONG).show()
                onTokenReceived(null)
            }
        } else {
            android.widget.Toast.makeText(context, "Google Sign In Canceled or Failed. Result Code: " + result.resultCode, android.widget.Toast.LENGTH_LONG).show()
            onTokenReceived(null)
        }
    }

    Button(
        onClick = {
            launcher.launch(googleSignInClient.signInIntent)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF1E1E1E),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}
