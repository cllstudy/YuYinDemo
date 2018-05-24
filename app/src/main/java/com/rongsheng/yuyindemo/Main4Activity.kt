package com.rongsheng.yuyindemo

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.alibaba.idst.nls.NlsClient
import com.alibaba.idst.nls.NlsListener
import com.alibaba.idst.nls.StageListener
import com.alibaba.idst.nls.internal.protocol.NlsRequest
import com.alibaba.idst.nls.internal.protocol.NlsRequestProto
import com.google.gson.Gson

import com.rongsheng.yuyindemo.Config.AKID
import com.rongsheng.yuyindemo.Config.AKS

/**
 * @desc 手指滑动取消录音
 * @author  lei
 * @date  2018/3/28 0028 -- 上午 10:04.
 * 个人博客站: http://www.bestlei.top
 */
class Main4Activity : AppCompatActivity() {


    private var mTvShowtext: TextView? = null
    private var isRecognizing = false
    private var mNlsClient: NlsClient? = null
    private var mNlsRequest: NlsRequest? = null
    private var context: Context? = null
    internal var yuyin: Button? = null


    private val mRecognizeListener = object : NlsListener() {
        override fun onRecognizingResult(status: Int, result: NlsListener.RecognizedResult?) {
            when (status) {
                NlsClient.ErrorCode.SUCCESS -> {
                    val gson = Gson()
                    val stu = gson.fromJson<Stu>(result!!.asr_out, Stu::class.java!!)
                    mTvShowtext!!.text = stu.result
                }
                NlsClient.ErrorCode.RECOGNIZE_ERROR -> Toast.makeText(this@Main4Activity, "录音错误", Toast.LENGTH_LONG).show()
                NlsClient.ErrorCode.RECORDING_ERROR -> Toast.makeText(this@Main4Activity, "录音识别错误", Toast.LENGTH_LONG).show()
                NlsClient.ErrorCode.NOTHING -> Toast.makeText(this@Main4Activity, "什么都没说", Toast.LENGTH_LONG).show()
                NlsClient.ErrorCode.USER_CANCEL -> Toast.makeText(this@Main4Activity, "用户取消录音", Toast.LENGTH_LONG).show()
            }
            isRecognizing = false
        }
    }
    private val mStageListener = object : StageListener() {
        override fun onStartRecognizing(recognizer: NlsClient?) =
                super.onStartRecognizing(recognizer)

        override fun onStopRecognizing(recognizer: NlsClient?) = super.onStopRecognizing(recognizer)

        override fun onVoiceVolume(volume: Int) = super.onVoiceVolume(volume)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        context = applicationContext

        mNlsRequest = initNlsRequest()
        val appkey = "nls-service" //请设置简介页面的Appkey
        mNlsRequest!!.app_key = appkey    //appkey列表中获取
        mNlsRequest!!.setAsr_sc("opu")      //设置语音格式
        // 热词参数
        mNlsRequest!!.setAsrVocabularyId("vocabid")
        NlsClient.openLog(true)
        NlsClient.configure(applicationContext) //全局配置
        initView()
        initNls()

    }

    private fun initNls() {
        mNlsClient = NlsClient.newInstance(this, mRecognizeListener, mStageListener, mNlsRequest)                          //实例化NlsClient
        mNlsClient!!.setMaxRecordTime(60000)  //设置最长语音
        mNlsClient!!.setMaxStallTime(1000)    //设置最短语音
        mNlsClient!!.setMinRecordTime(500)    //设置最大录音中断时间
        mNlsClient!!.setRecordAutoStop(false)  //设置VAD
        mNlsClient!!.setMinVoiceValueInterval(200) //设置音量回调时长
    }

    private fun initView() {
        mTvShowtext = findViewById(R.id.tv_showtext) as TextView
        yuyin = findViewById(R.id.yuyin) as Button
        yuyin!!.setOnTouchListener(myOnTouchListener())
    }

    private inner class myOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val action = event.action
            var start_x = 0
            var start_y = 0
            val end_x: Int
            val end_y: Int
            val mov_x: Int
            val mov_y: Int
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    //按下
                    start_x = event.x.toInt()
                    start_y = event.y.toInt()
                    initStartRecognizing()
                }
                MotionEvent.ACTION_UP -> {
                    //松开
                    end_x = event.x.toInt()
                    end_y = event.y.toInt()
                    mov_x = Math.abs(start_x - end_x)
                    mov_y = Math.abs(start_y - end_y)
                    if (mov_x > 200 || mov_y > 200) {
                        mNlsClient!!.cancel()
                        mTvShowtext!!.text = ""
                    } else {
                        initStopRecognizing()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    end_x = event.x.toInt()
                    end_y = event.y.toInt()
                    mov_x = Math.abs(start_x - end_x)
                    mov_y = Math.abs(start_y - end_y)
                    if (mov_x > 150 || mov_y > 150) {
                        mNlsClient!!.cancel()
                        mTvShowtext!!.text = ""

                    }
                }
            }
            return false
        }
    }

    private fun initNlsRequest(): NlsRequest {
        val proto = NlsRequestProto(context)
        proto.app_user_id = "CLL" //设置在应用中的用户名，可选
        return NlsRequest(proto)
    }

    private fun initStartRecognizing() {
        isRecognizing = true
        mTvShowtext!!.text = "正在录音，请稍候！"
        mNlsRequest!!.authorize(AKID, AKS) //请替换为用户申请到的Access Key ID和Access Key Secret
        mNlsClient!!.start()
    }

    private fun initStopRecognizing() {
        isRecognizing = false
        mTvShowtext!!.text = ""
        mNlsClient!!.stop()
    }
}
