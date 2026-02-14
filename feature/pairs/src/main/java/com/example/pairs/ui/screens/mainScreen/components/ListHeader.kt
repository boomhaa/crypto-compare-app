package com.example.pairs.ui.screens.mainScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.pairs.ui.screens.mainScreen.components.PairRow
import com.example.ui.theme.Dimensions
import com.example.ui.theme.textSecondary

@Composable
fun ListHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.listItemHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Gap.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Название",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.textSecondary,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Gap.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Макс",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.textSecondary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
            )
            Text(
                text = "Мин",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.textSecondary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
            )
        }
    }
}
