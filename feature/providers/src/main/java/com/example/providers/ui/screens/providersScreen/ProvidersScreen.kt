package com.example.providers.ui.screens.providersScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.providers.viewmodel.providersviewmodel.ProvidersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(viewModel: ProvidersViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Список провайдеров",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
        )
    }) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            Button(onClick = viewModel::loadProviders) {
                Text("Загрузить провайдеров")
            }

            Spacer(Modifier.height(12.dp))

            if (uiState.value.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            uiState.value.error?.let { error ->
                Text("Ошибка: $error")
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(uiState.value.providers, key = { it.id }) { provider ->
                    Text(
                        text = "${provider.id}: ${provider.name} (${provider.status})",
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}
