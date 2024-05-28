package com.xwl.webrtcdemo

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.logger.Logger
import com.xwl.webrtcdemo.databinding.ActivityMainBinding
import com.xwl.webrtcdemo.receivers.WebsocketReceiver

class MainActivity : AppCompatActivity() {
    private  lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        checkPermission(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
        initView()
        initReceiver()
    }
    private fun checkPermission(vararg permission:String) {
        requestPermission(*permission, allGranted = {
             Logger.e("所有权限已获取")
        },denied = {
            Logger.e("未获得权限长度:${it.size}")
        }, explained = {
            Logger.e("关闭提示权限长度:${it.size}")
        })
    }
    private var mReceiver:WebsocketReceiver? = null
    private fun initReceiver() {
        val  mReceiver = WebsocketReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.xwl.webrtcdemo.receivers")
        registerReceiver(mReceiver, intentFilter)
        mReceiver.setMessageListener {
            Toast.makeText(this@MainActivity,it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        mBinding.btnConnect.setOnClickListener {
            connectWs()
        }
        mBinding.btnDisConnect.setOnClickListener {
            disConnectWs()
        }
    }

    private fun connectWs() {
        startWebSocketService()
    }

    private fun disConnectWs() {
        stopWebSocketService()
    }

    private var mWebSocketService:  WebSocketService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            //服务与活动成功绑定
            mWebSocketService = (iBinder as WebSocketService.WebSocketClientBinder).service
            Logger.e("WebSocket服务与MainActivity成功绑定")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            //服务与活动断开
            mWebSocketService = null
            Logger.e("WebSocket服务与MainActivity成功断开")
        }
    }
    private var bindIntent: Intent? = null
    /**
     * 开启并绑定WebSocket服务
     */
    private fun startWebSocketService() {
        bindIntent = Intent(this, WebSocketService::class.java)
        startService(bindIntent)
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun stopWebSocketService() {
        serviceConnection?.let { unbindService(it) }
        bindIntent?.let { stopService(it) }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        bindIntent?.let { stopService(it) }
        mReceiver?.let { unregisterReceiver(it) }
        mReceiver = null
        super.onDestroy()
    }
}