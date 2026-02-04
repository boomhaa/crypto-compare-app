package com.example.auth.ui.screens.loginscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.ui.components.AuthBackground
import com.example.auth.ui.components.AuthDivider
import com.example.auth.ui.components.AuthErrorMessage
import com.example.auth.ui.components.AuthFooterLink
import com.example.auth.ui.components.AuthGoogleButton
import com.example.auth.ui.components.AuthLogo
import com.example.auth.ui.components.AuthPrimaryButton
import com.example.auth.ui.components.AuthTextField
import com.example.auth.ui.components.rememberGoogleSignInHandler
import com.example.auth.viewmodel.loginviewmodel.LoginViewModel
import com.example.ui.theme.Dimensions

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val scrollState = rememberScrollState()
    val googleSignInHandler =
        rememberGoogleSignInHandler(
            onToken = viewModel::signInWithGoogle,
            onError = viewModel::onGoogleError,
        )

    AuthBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = Dimensions.Padding.screenHorizontal,
                        vertical = Dimensions.Padding.screenVertical,
                    ),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(Dimensions.Spacing.lg))

            AuthLogo()

            Spacer(modifier = Modifier.height(Dimensions.Spacing.md))

            Text(
                text = "Crypto Compare",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Track crypto prices in real-time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))

            AuthTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Email",
                leadingIcon = "📧",
                keyboardType = KeyboardType.Email,
                isError = uiState.errorMessage != null && uiState.email.isBlank(),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.sm))

            AuthTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Password",
                leadingIcon = "🔒",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = uiState.errorMessage != null && uiState.password.isBlank(),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.xs))

            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = Dimensions.Spacing.xs),
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.md))

            AuthPrimaryButton(
                text = if (uiState.isLoading) "Signing in..." else "Sign In",
                onClick = viewModel::signInWithEmail,
                enabled = !uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.md))

            AuthDivider()

            Spacer(modifier = Modifier.height(Dimensions.Spacing.md))

            AuthGoogleButton(
                text = "Continue with Google",
                onClick = googleSignInHandler,
            )

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(Dimensions.Spacing.md))
                AuthErrorMessage(text = message)
            }

            Spacer(modifier = Modifier.height(Dimensions.Spacing.lg))

            AuthFooterLink(
                prompt = "Don't have an account? ",
                actionText = "Sign Up",
                onClick = onRegisterClick,
            )

            Spacer(modifier = Modifier.height(Dimensions.Spacing.xl))
        }
    }
}
