package com.rongsheng.yuyindemo

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.alibaba.idst.nls.NlsClient
import com.alibaba.idst.nls.NlsListener
import com.alibaba.idst.nls.StageListener
import com.alibaba.idst.nls.internal.protocol.NlsRequest
import com.alibaba.idst.nls.internal.protocol.NlsRequestASR
import com.google.gson.Gson
import com.rongsheng.yuyindemo.Config.AKID
import com.rongsheng.yuyindemo.Config.AKS

class Main3Activity : AppCompatActivity() {
    private var mTvShowtext: TextView? = null
    private var mBtOpen: Button? = null
    private var mBtStop: Button? = null
    private var isRecognizing = false
    private var mNlsClient: NlsClient? = null
    private var mNlsRequest: NlsRequest? = null
    private var context: Context? = null
    private var id: String? = null
    private var secret: String? = null
    private var appKey: String? = null
    private var mStu: Stu? = null
    private val mRecognizeListener = object : NlsListener() {
        override fun onRecognizingResult(status: Int, result: NlsListener.RecognizedResult?) {
            when (status) {
                NlsClient.ErrorCode.SUCCESS -> if (result != null) {
                    val gson = Gson()
                    mStu = gson.fromJson(result.asr_out, Stu::class.java)
                    mTvShowtext!!.text = mStu!!.result
                    Log.e(TAG, "onRecognizingResult: " + mStu!!.result)

                } else {
                    Log.i("asr", "[demo] callback onRecognizResult finish!")
                    mTvShowtext!!.text = "Recognize finish!"
                    mBtOpen!!.text = "Recognize finish!"
                }
                NlsClient.ErrorCode.RECOGNIZE_ERROR -> Toast.makeText(this@Main3Activity, "recognizer error", Toast.LENGTH_LONG).show()
                NlsClient.ErrorCode.RECORDING_ERROR -> Toast.makeText(this@Main3Activity, "recording error", Toast.LENGTH_LONG).show()
                NlsClient.ErrorCode.NOTHING -> Toast.makeText(this@Main3Activity, "nothing", Toast.LENGTH_LONG).show()
            }
            isRecognizing = false
        }
    }
    private val mStageListener = object : StageListener() {
        override fun onStartRecognizing(recognizer: NlsClient?) =
                super.onStartRecognizing(recognizer)    //To change body of overridden methods use File | Settings | File Templates.

        override fun onStopRecognizing(recognizer: NlsClient?) {
            super.onStopRecognizing(recognizer)    //To change body of overridden methods use File | Settings | File Templates.
            mTvShowtext!!.text = mStu!!.result
            mNlsClient!!.stop()
            mBtOpen!!.text = "开始录音"
        }

        override fun onVoiceVolume(volume: Int) = super.onVoiceVolume(volume)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        context = applicationContext
        initView()
        mNlsRequest = NlsRequest()
        appKey = "nls-service" //参考文档
        id = AKID
        secret = AKS//请替换为用户申请到的数加认证Access Key和Access Srcret，见上方文档
        mNlsRequest!!.app_key = appKey    //appkey请从 "快速开始" 帮助页面的appkey列表中获取
        mNlsRequest!!.setAsrResposeMode(NlsRequestASR.mode.STREAMING)//流式为streaming,非流式为normal
        //设置热词相关属性
        //mNlsRequest.setVocabularyId("vocab_id");//详情参考热词相关接口
        NlsClient.openLog(true)
        NlsClient.configure(applicationContext) //全局配置
        mNlsClient = NlsClient.newInstance(this, mRecognizeListener, mStageListener, mNlsRequest)//实例化NlsClient
        initStartRecognizing()
        initStopRecognizing()

    }

    private fun initView() {
        mTvShowtext = findViewById(R.id.tv_showtext) as TextView
        mBtOpen = findViewById(R.id.bt_open) as Button
        mBtStop = findViewById(R.id.bt_stop) as Button
    }

    private fun initStartRecognizing() = mBtOpen!!.setOnClickListener {
        isRecognizing = true
        mTvShowtext!!.text = "正在录音，请稍候！"
        mNlsRequest!!.authorize(id, secret) //请替换为用户申请到的数加认证Access Key和Access Srcret，见上方文档
        mNlsClient!!.start()
        mBtOpen!!.text = "录音中。。。"
    }

    private fun initStopRecognizing() = mBtStop!!.setOnClickListener {
        isRecognizing = false
        mTvShowtext!!.text = ""
        mNlsClient!!.stop()
        mBtOpen!!.text = "开始录音"
    }

    companion object {

        private val TAG = "AAA"
    }
}
