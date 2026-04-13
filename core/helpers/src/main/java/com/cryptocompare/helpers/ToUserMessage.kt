package com.cryptocompare.helpers

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserMessage(): String =
    when (this) {
        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException,
        -> "No internet connection"
        is IOException -> "No internet connection"
        else -> message ?: "Unknown error"
    }
