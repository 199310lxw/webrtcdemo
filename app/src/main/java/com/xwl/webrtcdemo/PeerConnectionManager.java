package com.xwl.webrtcdemo;

import android.content.Context;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxw
 * @date 2024/5/23
 * descripe
 */
public class PeerConnectionManager {
    private Context context;
    private EglBase mEglBase;
    private PeerConnectionFactory mPeerConnectionFactory;
    private PeerConnection peerConnection;
    private List<PeerConnection.IceServer> iceServers;

    /**
     * 创建PeerConnectionFactory
     * @return
     */
    public PeerConnectionFactory createPeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264Simulcast/Enabled/") //该特性效果为，在推流给多个设备时，编码端会编码多个不同码率、质量的视频码流，根据对方设备的性能、网络来发送对应质量的画面。即：编码端编码多种不同画面质量的h264视频流，WebRtc会根据接收端的性能、网络状况来发送对应画面质量的码流。以此来提高视频实时性。减少设备性能差距导致的视频帧解析耗时不一致问题
                .createInitializationOptions();

        PeerConnectionFactory.initialize(initializationOptions);

        mEglBase = EglBase.create();

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = true;
        options.disableNetworkMonitor = true;

        PeerConnectionFactory peerConnectionFactory =PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(mEglBase.getEglBaseContext(),true,true))//视频编码器
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext()))
                .setOptions(options)
                .createPeerConnectionFactory();
        return peerConnectionFactory;
    }
    /**
     * 创建Peerconnection
     */
    public PeerConnection createPeerconnection() {
        if(mPeerConnectionFactory == null) {
            mPeerConnectionFactory = createPeerConnectionFactory();
        }
        iceServers = new ArrayList<>();
        PeerConnection.IceServer iceServer = PeerConnection.IceServer.builder(Constants.STUN).createIceServer();
        iceServers.add(iceServer);

        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(iceServers);

        PeerConnection peerConnection = mPeerConnectionFactory.createPeerConnection(configuration, (PeerConnection.Observer) context);
        return peerConnection;
    }
}
