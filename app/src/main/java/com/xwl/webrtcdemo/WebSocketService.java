package com.xwl.webrtcdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * @author lxw
 * @date 2024/5/27
 * descripe
 */
public class WebSocketService extends Service {
    private static final long HEART_BEAT_RATE = 5 * 1000; //心跳检测时间间隔;
    private MyWebSocketClient client;
    private WebSocketClientBinder webSocketClientBinder = new WebSocketClientBinder();
    private static final long CLOSE_RECON_TIME = 100;//连接断开或者连接错误立即重连
    private Handler mHandler = new Handler();
    private Intent mIntent =new Intent("com.xwl.webrtcdemo.receivers");
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return webSocketClientBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectWs();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测
        return START_STICKY;
    }

    public MyWebSocketClient getClient() {
        return client;
    }

    /**
     * 连接WebSocket
     */
    private void connectWs() {
        client = MyWebSocketClient.getInstance();
        new Thread() {
            @Override
            public void run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    if(client.getReadyState().equals(ReadyState.NOT_YET_CONNECTED)) {
                        client.connectBlocking();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        client.setOnMsgListener(message -> {
            mIntent.putExtra("message", message);
            sendBroadcast(mIntent);
        });
    }

    /**
     * 开启重连
     */
    private void reconnectWs() {
        if (client == null) return;
        new Thread() {
            @Override
            public void run() {
                Logger.e("开启重连");
                Logger.e(client.getReadyState().toString());
                if (client.getReadyState().equals(ReadyState.CLOSING) || client.getReadyState().equals(ReadyState.CLOSED)) {
                    try {
                        client.reconnectBlocking();
                    } catch (InterruptedException e) {
                        Logger.e(e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Logger.e("无法重连");
                }
            }
        }.start();
    }

    /**
     * 发送消息
     */
    public void sendMsg(String msg) {
        if (client != null && client.isOpen()) {
            try {
                client.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 断开连接
     */
    public void closeConnect() {
        mHandler.removeCallbacks(heartBeatRunnable);
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }


    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (client != null) {
                if (client.isClosed()) {
                    Logger.e("心跳包检测WebSocket连接状态：已关闭" + client.getReadyState() + "-------->重新连接");
                    reconnectWs();
                } else if (client.isOpen()) {
                    Logger.e("心跳包检测WebSocket连接状态：已连接-------->发送心跳包");
                    sendMsg("我是心跳包");
                } else {
                    Logger.e("心跳包检测WebSocket连接状态：" + client.getReadyState());
                }
            } else {
                //如果client已为空，重新初始化连接
                Logger.e("心跳包检测WebSocket连接状态：client已为空-------->重新初始化连接");
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };


    public class WebSocketClientBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.e("Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        closeConnect();
        super.onDestroy();
    }

}
