package com.rongsheng.yuyindemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.idst.nls.NlsClient;
import com.alibaba.idst.nls.NlsListener;
import com.alibaba.idst.nls.StageListener;
import com.alibaba.idst.nls.internal.protocol.NlsRequest;
import com.alibaba.idst.nls.internal.protocol.NlsRequestASR;
import com.google.gson.Gson;

public class Main3Activity extends AppCompatActivity{

    private static final String TAG = "AAA";
    private TextView mTvShowtext;
    private Button mBtOpen;
    private Button mBtStop;
    private boolean isRecognizing = false;
    private NlsClient mNlsClient;
    private NlsRequest mNlsRequest;
    private Context context;
    private String id ;
    private String secret;
    private String appKey;
    private Stu mStu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        context = getApplicationContext();
        initView();
        mNlsRequest = new NlsRequest();
        appKey = "nls-service"; //参考文档
        id = "LTAIZ5mIISpcZKfR";
        secret = "K7xUsmUoHroqQbq3XlAsNqGenQJOdW";//请替换为用户申请到的数加认证Access Key和Access Srcret，见上方文档
        mNlsRequest.setApp_key(appKey);    //appkey请从 "快速开始" 帮助页面的appkey列表中获取
        mNlsRequest.setAsrResposeMode(NlsRequestASR.mode.STREAMING);//流式为streaming,非流式为normal
        //设置热词相关属性
        //mNlsRequest.setVocabularyId("vocab_id");//详情参考热词相关接口
        NlsClient.openLog(true);
        NlsClient.configure(getApplicationContext()); //全局配置
        mNlsClient = NlsClient.newInstance(this, mRecognizeListener, mStageListener,mNlsRequest);//实例化NlsClient
        initStartRecognizing();
        initStopRecognizing();

    }

    private void initView() {
        mTvShowtext = (TextView) findViewById(R.id.tv_showtext);
        mBtOpen = (Button) findViewById(R.id.bt_open);
        mBtStop = (Button) findViewById(R.id.bt_stop);
    }
    private void initStartRecognizing(){
        mBtOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecognizing = true;
                mTvShowtext.setText("正在录音，请稍候！");
                mNlsRequest.authorize(id,secret); //请替换为用户申请到的数加认证Access Key和Access Srcret，见上方文档
                mNlsClient.start();
                mBtOpen.setText("录音中。。。");
            }
        });
    }
    private void initStopRecognizing(){
        mBtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecognizing = false;
                mTvShowtext.setText("");
                mNlsClient.stop();
                mBtOpen.setText("开始录音");
            }
        });
    }
    private NlsListener mRecognizeListener = new NlsListener() {
        @Override
        public void onRecognizingResult(int status, RecognizedResult result) {
            switch (status) {
                case NlsClient.ErrorCode.SUCCESS:
                    if (result!=null){
                        Gson gson = new Gson();
                        mStu = gson.fromJson(result.asr_out, Stu.class);
                        mTvShowtext.setText(mStu.getResult());
                        Log.e(TAG, "onRecognizingResult: "+mStu.getResult() );

                    }else {
                        Log.i("asr", "[demo] callback onRecognizResult finish!" );
                        mTvShowtext.setText("Recognize finish!");
                        mBtOpen.setText("Recognize finish!");
                    }
                    break;
                case NlsClient.ErrorCode.RECOGNIZE_ERROR:
                    Toast.makeText(Main3Activity.this, "recognizer error", Toast.LENGTH_LONG).show();
                    break;
                case NlsClient.ErrorCode.RECORDING_ERROR:
                    Toast.makeText(Main3Activity.this,"recording error", Toast.LENGTH_LONG).show();
                    break;
                case NlsClient.ErrorCode.NOTHING:
                    Toast.makeText(Main3Activity.this,"nothing", Toast.LENGTH_LONG).show();
                    break;
            }
            isRecognizing = false;
        }
    };
    private StageListener mStageListener = new StageListener() {
        @Override
        public void onStartRecognizing(NlsClient recognizer) {
            super.onStartRecognizing(recognizer);    //To change body of overridden methods use File | Settings | File Templates.
        }
        @Override
        public void onStopRecognizing(NlsClient recognizer) {
            super.onStopRecognizing(recognizer);    //To change body of overridden methods use File | Settings | File Templates.
            mTvShowtext.setText(mStu.getResult());
            mNlsClient.stop();
            mBtOpen.setText("开始录音");
        }
        @Override
        public void onStartRecording(NlsClient recognizer) {
            super.onStartRecording(recognizer);    //To change body of overridden methods use File | Settings | File Templates.
        }
        @Override
        public void onStopRecording(NlsClient recognizer) {
            super.onStopRecording(recognizer);    //To change body of overridden methods use File | Settings | File Templates.
        }
        @Override
        public void onVoiceVolume(int volume) {
            super.onVoiceVolume(volume);
        }
    };
}
