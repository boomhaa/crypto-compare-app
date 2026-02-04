package com.example.auth.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.auth.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun rememberGoogleSignInHandler(
    onToken: (String) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)

    val googleSignInOptions =
        remember(webClientId) {
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    onError(context.getString(R.string.google_sign_in_token_missing))
                } else {
                    onToken(idToken)
                }
            } catch (_: ApiException) {
                onError(context.getString(R.string.google_sign_in_failed))
            }
        }

    return signIn@{
        val activity = context as? Activity
        if (activity == null) {
            onError(context.getString(R.string.google_sign_in_failed))
            return@signIn
        }
        val client = GoogleSignIn.getClient(activity, googleSignInOptions)
        launcher.launch(client.signInIntent)
    }
}
