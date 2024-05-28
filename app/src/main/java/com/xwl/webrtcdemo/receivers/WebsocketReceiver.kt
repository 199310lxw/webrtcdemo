package com.xwl.webrtcdemo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author  lxw
 * @date 2024/5/16
 * descripe
 */
class WebsocketReceiver : BroadcastReceiver() {
    private var mListener: ((String) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("message")
        mListener?.let { it ->
            message?.let { it1 ->
                it.invoke(it1)
            }
        }
    }

    fun setMessageListener(listener: ((String) -> Unit)) {
        this.mListener = listener
    }
}