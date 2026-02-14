package com.example.network.websocket

import com.example.helpers.util.Constants
import com.example.network.dto.webSocketDTO.SocketDtoMessage
import com.example.network.dto.webSocketDTO.SocketInboundRawMessage
import com.example.network.dto.webSocketDTO.SocketOutboundMessage
import com.example.network.dto.webSocketDTO.dataTypes.ErrorData
import com.example.network.dto.webSocketDTO.dataTypes.SymbolPriceChangeData
import com.example.network.dto.webSocketDTO.dataTypes.TickerData
import com.example.network.dto.webSocketDTO.dataTypes.WelcomeData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.min
import kotlin.random.Random

@Singleton
class WebSocketClient
    @Inject
    constructor(
        private val gson: Gson,
        private val okHttpClient: OkHttpClient,
        @Named("ioDispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + dispatcher)

        private val subscribedTickers = mutableSetOf<String>()
        private val lock = Any()

        private var reconnectJob: Job? = null
        private var isManuallyDisconnect = false
        private var reconnectAttempts = 0
        private val maxReconnectingAttempts = 10
        private var webSocket: WebSocket? = null
        private var currentUrl: String? = null

        private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
        val connectionState = _connectionState.asStateFlow()

        private val _messages =
            MutableSharedFlow<SocketDtoMessage>(
                extraBufferCapacity = 256,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        val messages = _messages.asSharedFlow()

        fun connect(url: String) {
            require(url.startsWith("ws://") || url.startsWith("wss://")) {
                "WebSocket URL must start with ws:// or wss://"
            }

            currentUrl = url
            if (_connectionState.value == ConnectionState.Connecting ||
                _connectionState.value == ConnectionState.Connected
            ) {
                return
            }

            _connectionState.value = ConnectionState.Connecting
            isManuallyDisconnect = false

            val request = Request.Builder().url(url).build()
            webSocket = okHttpClient.newWebSocket(request, webSocketListener())
        }

        fun disconnect() {
            isManuallyDisconnect = true
            reconnectJob?.cancel()
            reconnectAttempts = 0
            _connectionState.value = ConnectionState.Disconnected
            webSocket?.close(Constants.WebSocketConstants.NORMAL_CLOSURE_STATUS, "Client disconnected")
            webSocket = null
        }

        fun reconnect() {
            if (isManuallyDisconnect || reconnectJob?.isActive == true) {
                return
            }

            val url = currentUrl ?: return

            if (reconnectAttempts >= maxReconnectingAttempts) {
                _connectionState.value = ConnectionState.Error("Max attempts are reached")
                return
            }

            _connectionState.value =
                ConnectionState.Reconnecting(
                    attempts = reconnectAttempts,
                    timeDelay = reconnectDelayMs(reconnectAttempts),
                )

            val delayMS = reconnectDelayMs(reconnectAttempts++)
            reconnectJob =
                scope.launch {
                    delay(delayMS)
                    connect(url)
                }
        }

        fun subscribe(ticker: String): Boolean {
            val tickerLower = ticker.lowercase()

            val wasAdded =
                synchronized(lock) {
                    subscribedTickers.add(tickerLower)
                }

            return if (wasAdded) sendMessage(MessageType.SUBSCRIBE, tickerLower) else false
        }

        fun unsubscribe(ticker: String): Boolean {
            val tickerLower = ticker.lowercase()

            val wasRemoved =
                synchronized(lock) {
                    subscribedTickers.remove(tickerLower)
                }

            return if (wasRemoved) sendMessage(MessageType.UNSUBSCRIBE, tickerLower) else false
        }

        private fun webSocketListener(): WebSocketListener =
            object : WebSocketListener() {
                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response,
                ) {
                    _connectionState.value = ConnectionState.Connected
                    reconnectAttempts = 0
                    reconnectJob?.cancel()
                    restoreSubscriptions()
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    bytes: ByteString,
                ) {
                    parseAndEmitRaw(bytes.utf8())
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    text: String,
                ) {
                    parseAndEmitRaw(text)
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?,
                ) {
                    _connectionState.value = ConnectionState.Error(t.message ?: "Error in websocket")
                    reconnect()
                }

                override fun onClosing(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String,
                ) {
                    webSocket.close(code, reason)
                }

                override fun onClosed(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String,
                ) {
                    _connectionState.value = ConnectionState.Disconnected
                    this@WebSocketClient.webSocket = null
                    if (!isManuallyDisconnect) {
                        reconnect()
                    }
                }
            }

        private fun sendMessage(
            type: MessageType,
            ticker: String,
        ): Boolean {
            val payload =
                SocketOutboundMessage(
                    type = type.type,
                    data = TickerData(ticker = ticker),
                )
            return webSocket?.send(gson.toJson(payload)) ?: false
        }

        private fun restoreSubscriptions() {
            val tickers = synchronized(lock) { subscribedTickers.toSet() }
            tickers.forEach { ticker ->
                sendMessage(MessageType.SUBSCRIBE, ticker)
            }
        }

        private fun parseAndEmitRaw(rawMessage: String) {
            val parsedMessage =
                runCatching { gson.fromJson(rawMessage, SocketInboundRawMessage::class.java) }.getOrNull() ?: return
            val type = MessageType.fromType(parsedMessage.type)

            val message: SocketDtoMessage =
                when (type) {
                    MessageType.WELCOME -> {
                        val data =
                            runCatching { gson.fromJson(parsedMessage.data, WelcomeData::class.java) }.getOrNull()
                                ?: WelcomeData("")
                        SocketDtoMessage.Welcome(parsedMessage.id, data = data)
                    }

                    MessageType.SUBSCRIBE -> {
                        val data =
                            runCatching { gson.fromJson(parsedMessage.data, TickerData::class.java) }.getOrNull()
                                ?: TickerData("")
                        SocketDtoMessage.Subscribe(parsedMessage.id, data = data)
                    }

                    MessageType.UNSUBSCRIBE -> {
                        val data =
                            runCatching { gson.fromJson(parsedMessage.data, TickerData::class.java) }.getOrNull()
                                ?: TickerData("")
                        SocketDtoMessage.Unsubscribe(parsedMessage.id, data = data)
                    }

                    MessageType.PRICE_CHANGE -> {
                        val data =
                            runCatching {
                                gson.fromJson(
                                    parsedMessage.data,
                                    SymbolPriceChangeData::class.java,
                                )
                            }.getOrNull()
                                ?: return
                        SocketDtoMessage.SymbolPriceChange(parsedMessage.id, data = data)
                    }

                    MessageType.ERROR -> {
                        val data =
                            runCatching { gson.fromJson(parsedMessage.data, ErrorData::class.java) }.getOrNull()
                                ?: ErrorData(error = "", errorCode = Constants.WebSocketConstants.UNKNOWN_ERROR_CODE)
                        SocketDtoMessage.Error(parsedMessage.id, data = data)
                    }
                }
            scope.launch { _messages.emit(message) }
        }

        private fun reconnectDelayMs(attempt: Int): Long {
            val exponent = min(attempt, Constants.WebSocketConstants.MAX_EXPONENT)
            val expMultiplier = 1L shl exponent
            val baseDelay =
                min(
                    Constants.WebSocketConstants.MAX_RECONNECT_DELAY_MS,
                    Constants.WebSocketConstants.BASE_RECONNECT_DELAY_MS * expMultiplier,
                )
            val jitter = Random.nextLong(Constants.WebSocketConstants.RECONNECT_JITTER_MS + 1)
            return baseDelay + jitter
        }

        fun close() {
            disconnect()
            scope.cancel()
        }
    }
