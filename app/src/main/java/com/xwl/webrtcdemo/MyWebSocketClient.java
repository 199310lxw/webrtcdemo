package com.xwl.webrtcdemo;

import android.content.Intent;

import com.orhanobut.logger.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * @author lxw
 * @date 2024/5/28
 * descripe
 */
public class MyWebSocketClient extends WebSocketClient {

    private MyWebSocketClient() {
        super(URI.create("ws://192.168.10.75:8080"));
    }
    private static MyWebSocketClient mInstance = null;

    private OnMessageListener mListener;
    public static MyWebSocketClient getInstance() {
        if(mInstance == null) {
            synchronized (MyWebSocketClient.class) {
                if(mInstance == null) {
                    mInstance = new MyWebSocketClient();
                }
            }
        }
        return mInstance;
    }
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Logger.e("Websocket onOpen");
    }

    public void  setOnMsgListener(OnMessageListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onMessage(String message) {
        Logger.e("Websocket onMessage:" + message);
        if(mListener != null) {
            mListener.onMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Logger.e("Websocket onClose: " + code + "---->" + reason + "---->" + remote);
    }

    @Override
    public void onError(Exception ex) {
        Logger.e("Websocket onError:" + ex.getMessage());
    }
    interface OnMessageListener{
        void onMessage(String message);
    }
}
