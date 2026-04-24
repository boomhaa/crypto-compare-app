package com.cryptocompare.domain.usecase.pairs

import com.cryptocompare.domain.repository.CryptoCompareRepository
import com.cryptocompare.model.ProviderDetail
import com.cryptocompare.model.TickerDetail
import javax.inject.Inject

class GetTickerDetailUseCase
    @Inject
    constructor(
        private val cryptoCompareRepository: CryptoCompareRepository,
    ) {
        suspend operator fun invoke(ticker: String): Result<TickerDetail> =
            runCatching {
                val symbols = cryptoCompareRepository.getSymbolsByTicker(ticker).getOrThrow()
                val providers = cryptoCompareRepository.getProviders().getOrThrow().associateBy { it.id }

                val providersBySell =
                    symbols
                        .filter { it.providerSellId > 0 }
                        .groupBy { it.providerSellId }
                        .mapValues { (_, syms) -> syms.maxByOrNull { it.priceSell }?.priceSell }

                val providersByBuy =
                    symbols
                        .filter { it.providerBuyId > 0 }
                        .groupBy { it.providerBuyId }
                        .mapValues { (_, syms) -> syms.maxByOrNull { it.priceSell }?.priceSell }

                val providersAllId = (providersBySell.keys + providersByBuy.keys).distinct()

                val exchanges =
                    providersAllId.mapNotNull { providerId ->
                        val provider = providers[providerId] ?: return@mapNotNull null
                        ProviderDetail(
                            provider = provider,
                            priceSell = providersBySell[providerId],
                            priceBuy = providersByBuy[providerId],
                        )
                    }

                TickerDetail(ticker = ticker, exchanges = exchanges)
            }
    }
