package com.xwl.webrtcdemo

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import org.webrtc.PeerConnectionFactory

/**
 * @author  lxw
 * @date 2024/5/23
 * descripe
 */
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions())

        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)      //（可选）是否显示线程信息。 默认值为true
            .tag("lxw")                  //（可选）每个日志的全局标记。 默认PRETTY_LOGGER（如上图）
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
    }
}