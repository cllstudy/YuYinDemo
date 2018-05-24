package com.rongsheng.yuyindemo

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.alibaba.idst.nls.NlsClient
import com.alibaba.idst.nls.NlsListener
import com.alibaba.idst.nls.internal.protocol.NlsRequest
import com.alibaba.idst.nls.internal.protocol.NlsRequestProto
import com.rongsheng.yuyindemo.Config.AKID
import com.rongsheng.yuyindemo.Config.AKS
import com.rongsheng.yuyindemo.R.id.et_shibie

/**
 * @desc 语音合成
 * @author  lei
 * @date  2018/3/28 0028 -- 上午 10:25.
 * 个人博客站: http://www.bestlei.top
 */
class Main2Activity : AppCompatActivity() {
    private var mEtShibie: EditText? = null
    private var mBtStatrtHecheng: Button? = null
    private var mNlsClient: NlsClient? = null
    private var mNlsRequest: NlsRequest? = null
    private var context: Context? = null
    internal var iMinBufSize = AudioTrack.getMinBufferSize(16000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT)
    internal var audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 16000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            iMinBufSize, AudioTrack.MODE_STREAM) //使用audioTrack播放返回的pcm数据
    private val mRecognizeListener = object : NlsListener() {
        override fun onTtsResult(status: Int, ttsResult: ByteArray?) {
            when (status) {
                NlsClient.ErrorCode.TTS_BEGIN -> {
                    audioTrack.play()
                    Log.e(TAG, "tts begin")
                    audioTrack.write(ttsResult!!, 0, ttsResult.size)
                }
                NlsClient.ErrorCode.TTS_TRANSFERRING -> {
                    Log.e(TAG, "tts transferring" + ttsResult!!.size)
                    audioTrack.write(ttsResult, 0, ttsResult.size)
                }
                NlsClient.ErrorCode.TTS_OVER -> {
                    audioTrack.stop()
                    Log.e(TAG, "tts over")
                }
                NlsClient.ErrorCode.CONNECT_ERROR -> Toast.makeText(this@Main2Activity, "CONNECT ERROR", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        initView()
        context = applicationContext
        mNlsRequest = initNlsRequest()
        val appkey = "nls-service"     //请设置简介页面的Appkey
        mNlsRequest!!.app_key = appkey    //appkey请从 简介页面的appkey列表中获取
        mNlsRequest!!.initTts()               //初始化tts请求
        NlsClient.openLog(true)
        NlsClient.configure(applicationContext) //全局配置
        mNlsClient = NlsClient.newInstance(this, mRecognizeListener, null, mNlsRequest)                          //实例化NlsClient
        initTtsContentButton()
    }

    private fun initView() {
        mEtShibie = findViewById(et_shibie) as EditText
        mBtStatrtHecheng = findViewById(R.id.bt_statrt_hecheng) as Button

    }

    private fun initNlsRequest(): NlsRequest {
        val proto = NlsRequestProto(context)
        proto.app_user_id = "陈" //设置用户名
        return NlsRequest(proto)
    }

    private fun initTtsContentButton() {
        mBtStatrtHecheng!!.setOnClickListener {
            val user_input = mEtShibie!!.text.toString()
            if (user_input == "") {
                Toast.makeText(this@Main2Activity, "输入不能为空！", Toast.LENGTH_LONG).show()
            } else {
                mNlsRequest!!.setTtsEncodeType("pcm") //返回语音数据格式，支持pcm,wav.alaw
                mNlsRequest!!.setTtsVolume(50)   //音量大小默认50，阈值0-100
                mNlsRequest!!.setTtsSpeechRate(0)//语速，阈值-500~500
                mNlsClient!!.PostTtsRequest(user_input) //用户输入文本
                mNlsRequest!!.authorize(AKID, AKS)       //请替换为用户申请到的数加认证key和密钥
                audioTrack.play()
            }
        }
    }

    override fun onDestroy() {
        audioTrack.release()
        super.onDestroy()
    }

    companion object {

        private val TAG = "CLL"
    }


}
