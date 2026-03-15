package com.cryptocompare.pairs.ui.screens.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptocompare.helpers.util.Constants
import com.cryptocompare.pairs.ui.screens.mainScreen.components.ListHeader
import com.cryptocompare.pairs.ui.screens.mainScreen.components.PairRow
import com.cryptocompare.pairs.viewmodel.mainViewModel.MainViewModel
import com.cryptocompare.ui.theme.Dimensions
import com.cryptocompare.ui.theme.bgPrimary
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val filteredPairs =
        uiState.value.pairs.filter { pair ->
            pair.ticker.contains(uiState.value.searchQuery.trim(), ignoreCase = true)
        }

    val lazyList = rememberLazyListState()

    LaunchedEffect(lazyList, viewModel, filteredPairs) {
        snapshotFlow {
            val firstVisibleIndex = lazyList.firstVisibleItemIndex
            filteredPairs
                .drop(firstVisibleIndex)
                .take(Constants.MAX_TICKERS_ON_SCREEN)
                .map { it.ticker }
        }.distinctUntilChanged()
            .collect(viewModel::onVisibleTickersChange)
    }

    LaunchedEffect(uiState.value.error, uiState.value.loading) {
        if (uiState.value.loading || uiState.value.error != null) {
            viewModel.onVisibleTickersChange(emptyList())
        }
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
                        detectTapGestures { focusManager.clearFocus() }
                    },
            verticalArrangement = Arrangement.spacedBy(Dimensions.Gap.md),
        ) {
            OutlinedTextField(
                value = uiState.value.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search:") },
            )

            when {
                uiState.value.loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.value.error != null -> {
                    Text(
                        text = "Error: ${uiState.value.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                else -> {
                    ListHeader()

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val rowSpacing = Dimensions.Gap.sm
                        val totalSpacing = rowSpacing * (Constants.MAX_TICKERS_ON_SCREEN - 1)
                        val calculatedRowHeight =
                            ((maxHeight - totalSpacing) / Constants.MAX_TICKERS_ON_SCREEN).coerceAtLeast(40.dp)

                        LazyColumn(
                            state = lazyList,
                            verticalArrangement = Arrangement.spacedBy(rowSpacing),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(filteredPairs, key = { it.ticker }) { pair ->
                                PairRow(
                                    pair = pair,
                                    rowHeight = calculatedRowHeight,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
