package io.github.boopited.wifiaware

import android.content.Context
import android.net.wifi.aware.*
import io.github.boopited.wifiaware.common.BaseManager

class WifiAwareSub(
    context: Context, serviceName: String,
    private val callback: Callback
): BaseManager(context) {

    interface Callback {
        fun onServiceSubscribed()
        fun onServiceFound(serviceInfo: ByteArray, matchFilter: List<ByteArray>)
        fun onMessageSendResult(id: Int, success: Boolean)
        fun onMessageReceived(message: ByteArray)
    }

    private var discoverySession: SubscribeDiscoverySession? = null
    private var publisherHandle: PeerHandle? = null

    private val config: SubscribeConfig = SubscribeConfig.Builder()
        .setServiceName(serviceName)
        .build()

    private val discoverySessionCallback = object : DiscoverySessionCallback() {

        override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
            super.onSubscribeStarted(session)
            discoverySession = session
            callback.onServiceSubscribed()
        }

        override fun onServiceDiscovered(
            peerHandle: PeerHandle?,
            serviceSpecificInfo: ByteArray?,
            matchFilter: MutableList<ByteArray>?
        ) {
            super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
            publisherHandle = peerHandle
            callback.onServiceFound(
                serviceSpecificInfo ?: ByteArray(0),
                matchFilter ?: emptyList()
            )
        }

        override fun onMessageSendSucceeded(messageId: Int) {
            super.onMessageSendSucceeded(messageId)
            callback.onMessageSendResult(messageId, true)
        }

        override fun onMessageSendFailed(messageId: Int) {
            super.onMessageSendFailed(messageId)
            callback.onMessageSendResult(messageId, false)
        }

        override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray?) {
            super.onMessageReceived(peerHandle, message)
            callback.onMessageReceived(message ?: ByteArray(0))
        }
    }

    override fun onSessionAttached() {
        super.onSessionAttached()
        subscribe()
    }

    private fun subscribe() {
        activeSession?.subscribe(config, discoverySessionCallback,null)
    }

    fun refreshSession() {
        discoverySession?.updateSubscribe(config)
    }

    fun sendMessage(id: Int, data: ByteArray) {
        publisherHandle?.let {
            discoverySession?.sendMessage(it, id, data)
        }
    }

    override fun stop() {
        discoverySession?.close()
        super.stop()
    }
}