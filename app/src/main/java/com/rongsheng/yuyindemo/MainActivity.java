package com.rongsheng.yuyindemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.idst.nls.NlsClient;
import com.alibaba.idst.nls.NlsListener;
import com.alibaba.idst.nls.StageListener;
import com.alibaba.idst.nls.internal.protocol.NlsRequest;
import com.alibaba.idst.nls.internal.protocol.NlsRequestProto;
import com.google.gson.Gson;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

import static com.rongsheng.yuyindemo.Config.AKID;
import static com.rongsheng.yuyindemo.Config.AKS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvShowtext;
    private Button mBtOpen;
    private boolean isRecognizing = false;
    private NlsClient mNlsClient;
    private NlsRequest mNlsRequest;
    private Context context;
    private Button statrt_hecheng;
    private Button mKuaisu,wx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        mNlsRequest = initNlsRequest();
        String appkey = "nls-service"; //请设置简介页面的Appkey
        mNlsRequest.setApp_key(appkey);    //appkey列表中获取
        mNlsRequest.setAsr_sc("opu");      //设置语音格式
        // 热词参数
        mNlsRequest.setAsrVocabularyId("vocabid");
        NlsClient.openLog(true);
        NlsClient.configure(getApplicationContext()); //全局配置
        initView();
        initPermission();
        initNls();

    }

    private void initNls() {
        mNlsClient = NlsClient.newInstance(this, mRecognizeListener, mStageListener, mNlsRequest);                          //实例化NlsClient
        mNlsClient.setMaxRecordTime(60000);  //设置最长语音
        mNlsClient.setMaxStallTime(1000);    //设置最短语音
        mNlsClient.setMinRecordTime(500);    //设置最大录音中断时间
        mNlsClient.setRecordAutoStop(false);  //设置VAD
        mNlsClient.setMinVoiceValueInterval(200); //设置音量回调时长
    }

    private void initView() {
        mTvShowtext = (TextView) findViewById(R.id.tv_showtext);
        mBtOpen = (Button) findViewById(R.id.bt_open);
         mBtOpen.setOnTouchListener(new myOnTouchListener());
        statrt_hecheng = (Button) findViewById(R.id.statrt_hecheng);
        statrt_hecheng.setOnClickListener(this);
        mKuaisu = (Button) findViewById(R.id.kuaisu);
        wx = (Button) findViewById(R.id.wx);
        mKuaisu.setOnClickListener(this);
        wx.setOnClickListener(this);


    }
    private class myOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event){
            int action = event.getAction();
           switch (action) {
               case MotionEvent.ACTION_DOWN:
                   //按下
                   initStartRecognizing();
                   break;
               case MotionEvent.ACTION_UP:
                   //松开
                   initStopRecognizing();
                   break;
            }
            return false;
        }
    }
    private NlsRequest initNlsRequest() {
        NlsRequestProto proto = new NlsRequestProto(context);
        proto.setApp_user_id("CLL"); //设置在应用中的用户名，可选
        return new NlsRequest(proto);
    }

    private void initStartRecognizing() {
                isRecognizing = true;
                mTvShowtext.setText("正在录音，请稍候！");
                mNlsRequest.authorize(AKID, AKS); //请替换为用户申请到的Access Key ID和Access Key Secret
                mNlsClient.start();
                mBtOpen.setText("录音中。。。");
    }

    private void initStopRecognizing() {
                isRecognizing = false;
                mTvShowtext.setText("");
                mNlsClient.stop();
                mBtOpen.setText("开始 录音");
    }


    private NlsListener mRecognizeListener = new NlsListener() {
        @Override
        public void onRecognizingResult(int status, RecognizedResult result) {
            switch (status) {
                case NlsClient.ErrorCode.SUCCESS:
                    Gson gson = new Gson();
                    Stu stu = gson.fromJson(result.asr_out, Stu.class);
                    mTvShowtext.setText(stu.getResult());
                    break;
                case NlsClient.ErrorCode.RECOGNIZE_ERROR:
                    Toast.makeText(MainActivity.this, "recognizer error", Toast.LENGTH_LONG).show();
                    break;
                case NlsClient.ErrorCode.RECORDING_ERROR:
                    Toast.makeText(MainActivity.this, "recording error", Toast.LENGTH_LONG).show();
                    break;
                case NlsClient.ErrorCode.NOTHING:
                    Toast.makeText(MainActivity.this, "nothing", Toast.LENGTH_LONG).show();
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
        }
        @Override
        public void onVoiceVolume(int volume) {
            super.onVoiceVolume(volume);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.statrt_hecheng:
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
                break;
            case R.id.kuaisu:
                Intent intent2=new Intent(MainActivity.this,Main3Activity.class);
                startActivity(intent2);
                break;
            case R.id.wx:
                Intent intent3=new Intent(MainActivity.this,Main4Activity.class);
                startActivity(intent3);
                break;
        }
    }
    private void initPermission() {
        AndPermission.with(this)
                .requestCode(300)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(MainActivity.this, rationale)
                        .show();
                    }
                })
                .callback(this)
                .start();
    }

    @PermissionYes(300)
    private void getPermissionYes(List<String> grantedPermissions) {
    }

    @PermissionNo(300)
    private void getPermissionNo(List<String> deniedPermissions) {
        AndPermission.defaultSettingDialog(MainActivity.this, 300).show();
    }
}
