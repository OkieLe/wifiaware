package io.github.boopited.wifiaware

import android.content.Context
import android.net.wifi.aware.*
import io.github.boopited.wifiaware.common.BaseManager

class WifiAwarePub(
    context: Context, serviceName: String,
    private val callback: Callback
): BaseManager(context) {

    interface Callback {
        fun onServicePublished()
        fun onMessageSendResult(id: Int, success: Boolean)
        fun onMessageReceived(message: ByteArray)
    }

    private var discoverySession: DiscoverySession? = null
    private var subscriberHandle: PeerHandle? = null

    private val config: PublishConfig = PublishConfig.Builder()
        .setServiceName(serviceName)
        .build()

    private val discoverySessionCallback = object : DiscoverySessionCallback() {

        override fun onPublishStarted(session: PublishDiscoverySession) {
            super.onPublishStarted(session)
            discoverySession = session
            callback.onServicePublished()
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
            subscriberHandle = peerHandle
            callback.onMessageReceived(message ?: ByteArray(0))
        }
    }

    override fun onSessionAttached() {
        super.onSessionAttached()
        publish()
    }

    private fun publish() {
        activeSession?.publish(config, discoverySessionCallback, null)
    }

    fun sendMessage(id: Int, data: ByteArray) {
        subscriberHandle?.let {
            discoverySession?.sendMessage(it, id, data)
        }
    }

    override fun stop() {
        discoverySession?.close()
        super.stop()
    }
}