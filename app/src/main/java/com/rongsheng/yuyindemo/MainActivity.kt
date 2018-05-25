package com.rongsheng.yuyindemo

import android.Manifest
import android.content.Context
import android.content.Intent
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
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionNo
import com.yanzhenjie.permission.PermissionYes

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var mTvShowtext: TextView? = null
    private var mBtOpen: Button? = null
    private var isRecognizing = false
    private var mNlsClient: NlsClient? = null
    private var mNlsRequest: NlsRequest? = null
    private var context: Context? = null
    private var statrt_hecheng: Button? = null
    private var mKuaisu: Button? = null
    private var wx: Button? = null
    private val mRecognizeListener = object : NlsListener() {
        override fun onRecognizingResult(status: Int, result: NlsListener.RecognizedResult?) {
            when (status) {
                NlsClient.ErrorCode.SUCCESS -> {
                    val gson = Gson()
                    val stu = gson.fromJson(result!!.asr_out, Stu::class.java)
                    mTvShowtext!!.text = stu.result
                }
                NlsClient.ErrorCode.RECOGNIZE_ERROR -> Toast.makeText(this@MainActivity, "recognizer error", Toast.LENGTH_LONG).show()
                NlsClient.ErrorCode.RECORDING_ERROR -> Toast.makeText(this@MainActivity, "recording error", Toast.LENGTH_LONG).show()
                NlsClient.ErrorCode.NOTHING -> Toast.makeText(this@MainActivity, "nothing", Toast.LENGTH_LONG).show()
            }
            isRecognizing = false
        }
    }
    private val mStageListener = object : StageListener() {
        override fun onVoiceVolume(volume: Int) = super.onVoiceVolume(volume)

        override fun onStartRecognizing(recognizer: NlsClient?) =
                super.onStartRecognizing(recognizer)

        override fun onStopRecognizing(recognizer: NlsClient?) =
                super.onStopRecognizing(recognizer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        initPermission()
        initNls()

    }

    private fun initNlsRequest(): NlsRequest {
        val proto = NlsRequestProto(context)
        proto.app_user_id = "CLL" //设置在应用中的用户名，可选
        return NlsRequest(proto)
    }

    private fun initView() {
        mTvShowtext = findViewById(R.id.tv_showtext) as TextView
        mBtOpen = findViewById(R.id.bt_open) as Button
        mBtOpen!!.setOnTouchListener(myOnTouchListener())
        statrt_hecheng = findViewById(R.id.statrt_hecheng) as Button
        statrt_hecheng!!.setOnClickListener(this)
        mKuaisu = findViewById(R.id.kuaisu) as Button
        wx = findViewById(R.id.wx) as Button
        mKuaisu!!.setOnClickListener(this)
        wx!!.setOnClickListener(this)


    }

    private fun initPermission() = AndPermission.with(this)
            .requestCode(300)
            .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
            .rationale { requestCode, rationale ->
                AndPermission.rationaleDialog(this@MainActivity, rationale)
                        .show()
            }
            .callback(this)
            .start()

    private fun initNls() {
        mNlsClient = NlsClient.newInstance(this, mRecognizeListener, mStageListener, mNlsRequest)                          //实例化NlsClient
        mNlsClient!!.setMaxRecordTime(60000)  //设置最长语音
        mNlsClient!!.setMaxStallTime(1000)    //设置最短语音
        mNlsClient!!.setMinRecordTime(500)    //设置最大录音中断时间
        mNlsClient!!.setRecordAutoStop(false)  //设置VAD
        mNlsClient!!.setMinVoiceValueInterval(200) //设置音量回调时长
    }

    private fun initStartRecognizing() {
        isRecognizing = true
        mTvShowtext!!.text = "正在录音，请稍候！"
        mNlsRequest!!.authorize(AKID, AKS) //请替换为用户申请到的Access Key ID和Access Key Secret
        mNlsClient!!.start()
        mBtOpen!!.text = "录音中。。。"
    }

    private fun initStopRecognizing() {
        isRecognizing = false
        mTvShowtext!!.text = ""
        mNlsClient!!.stop()
        mBtOpen!!.text = "开始 录音"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.statrt_hecheng -> {
                val intent = Intent(this@MainActivity, Main2Activity::class.java)
                startActivity(intent)
            }
            R.id.kuaisu -> {
                val intent2 = Intent(this@MainActivity, Main3Activity::class.java)
                startActivity(intent2)
            }
            R.id.wx -> {
                val intent3 = Intent(this@MainActivity, Main4Activity::class.java)
                startActivity(intent3)
            }
        }
    }

    @PermissionYes(300)
    private fun getPermissionYes(grantedPermissions: List<String>) = Unit

    @PermissionNo(300)
    private fun getPermissionNo(deniedPermissions: List<String>) =
            AndPermission.defaultSettingDialog(this@MainActivity, 300).show()

    private inner class myOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN ->
                    //按下
                    initStartRecognizing()
                MotionEvent.ACTION_UP ->
                    //松开
                    initStopRecognizing()
            }
            return false
        }
    }
}
