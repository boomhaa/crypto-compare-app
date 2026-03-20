package com.cryptocompare.domain.usecase.pairs

import com.cryptocompare.domain.mapper.toPairItems
import com.cryptocompare.domain.repository.CryptoCompareRepository
import com.cryptocompare.domain.repository.TickerStreamRepository
import com.cryptocompare.model.PairUiItem
import com.cryptocompare.model.Symbol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.set

class LoadPairsUseCase
    @Inject
    constructor(
        private val cryptoCompareRepository: CryptoCompareRepository,
        private val tickerStreamRepository: TickerStreamRepository,
    ) {
        suspend operator fun invoke(symbolsById: MutableMap<Long, Symbol>): Flow<List<PairUiItem>> {
            tickerStreamRepository.connect()

            return cryptoCompareRepository.getSymbols().map { page ->
                page.forEach { symbol ->
                    symbolsById[symbol.id] = symbol
                }
                symbolsById.values.toPairItems()
            }
        }
    }
