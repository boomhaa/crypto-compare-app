package com.example.helpers.util

object Constants {
    const val SPLASH_DURATION_MS = 2000L

    const val MAX_TICKERS_ON_SCREEN = 10
    const val SYMBOLS_IN_ROW = 25

    object WebSocketConstants {
        const val NORMAL_CLOSURE_STATUS = 1000
        const val BASE_RECONNECT_DELAY_MS = 1_000L
        const val MAX_RECONNECT_DELAY_MS = 30_000L
        const val RECONNECT_JITTER_MS = 500L
        const val MAX_EXPONENT = 5
        const val UNKNOWN_ERROR_CODE = -1
    }
}
