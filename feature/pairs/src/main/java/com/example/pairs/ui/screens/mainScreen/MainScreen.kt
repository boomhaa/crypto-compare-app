package com.example.pairs.ui.screens.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pairs.ui.screens.mainScreen.components.ListHeader
import com.example.pairs.ui.screens.mainScreen.components.PairRow
import com.example.pairs.viewmodel.mainViewModel.MainViewModel
import com.example.ui.theme.Dimensions
import com.example.ui.theme.bgPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val focusManages = LocalFocusManager.current
    val filteredPairs =
        uiState.pairs.filter { pair ->
            pair.ticker.contains(uiState.searchQuery.trim(), ignoreCase = true)
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Symbols",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.bgPrimary)
                    .padding(paddingValues)
                    .padding(horizontal = Dimensions.Padding.screenHorizontal)
                    .padding(vertical = Dimensions.Padding.screenVertical)
                    .pointerInput(Unit) {
                        detectTapGestures { focusManages.clearFocus() }
                    },
            verticalArrangement = Arrangement.spacedBy(Dimensions.Gap.md),
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search:") },
            )

            when {
                uiState.loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                else -> {
                    ListHeader()

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Gap.sm),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(filteredPairs, key = { it.ticker }) { pair ->
                            PairRow(pair)
                        }
                    }
                }
            }
        }
    }
}
