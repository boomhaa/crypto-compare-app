package com.cryptocompare.helpers.util

object Constants {
    const val SPLASH_DURATION_MS = 2000L

    const val MAX_TICKERS_ON_SCREEN = 10

    object CryptoCompareRepositoryConstants {
        const val CATALOG_CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L
        const val SYMBOLS_IN_ROW = 25
    }

    object WebSocketConstants {
        const val NORMAL_CLOSURE_STATUS = 1000
        const val BASE_RECONNECT_DELAY_MS = 1_000L
        const val MAX_RECONNECT_DELAY_MS = 30_000L
        const val RECONNECT_JITTER_MS = 500L
        const val MAX_EXPONENT = 5
        const val UNKNOWN_ERROR_CODE = -1
    }

    object UseCaseConstants {
        val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }

    object WorkerConstants {
        const val UNIQUE_WORK_NAME = "refresh_catalog_once_per_day"
    }

    object FirestoreConstants {
        const val USERS_COLLECTION = "users"
        const val FAVORITES_COLLECTION = "favorites"
        const val TICKER_FIELD = "ticker"
        const val UPDATED_AT_FIELD = "updatedAt"
    }
}
