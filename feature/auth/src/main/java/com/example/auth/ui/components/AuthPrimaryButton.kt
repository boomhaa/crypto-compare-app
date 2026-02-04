package com.example.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.CryptoGradients
import com.example.ui.theme.Dimensions
import com.example.ui.theme.inputBorder

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val colors =
        if (enabled) {
            CryptoGradients.primary
        } else {
            listOf(MaterialTheme.colorScheme.inputBorder, MaterialTheme.colorScheme.inputBorder)
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Dimensions.Height.button)
                .background(
                    brush = Brush.horizontalGradient(colors),
                    shape = MaterialTheme.shapes.large,
                ).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
