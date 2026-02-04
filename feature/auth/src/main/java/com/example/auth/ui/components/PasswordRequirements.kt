package com.example.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.theme.Dimensions
import com.example.ui.theme.cryptoSuccess
import com.example.ui.theme.textTertiary

@Composable
fun PasswordRequirements(
    lengthMet: Boolean,
    letterMet: Boolean,
    numberMet: Boolean,
) {
    Column(
        modifier = Modifier.padding(top = Dimensions.Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Gap.xs),
    ) {
        RequirementItem(text = "At least 6 characters", met = lengthMet)
        RequirementItem(text = "Contains a letter", met = letterMet)
        RequirementItem(text = "Contains a number", met = numberMet)
    }
}

@Composable
private fun RequirementItem(
    text: String,
    met: Boolean,
) {
    val color =
        if (met) {
            MaterialTheme.colorScheme.cryptoSuccess
        } else {
            MaterialTheme.colorScheme.textTertiary
        }
    val icon = if (met) "✓" else "○"
    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Gap.sm)) {
        Text(text = icon, color = color, style = MaterialTheme.typography.bodySmall)
        Text(text = text, color = color, style = MaterialTheme.typography.bodySmall)
    }
}
