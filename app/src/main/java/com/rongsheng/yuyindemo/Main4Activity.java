package com.rongsheng.yuyindemo;

import android.content.Context;
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

import static com.rongsheng.yuyindemo.Config.AKID;
import static com.rongsheng.yuyindemo.Config.AKS;
/**
 * @desc 手指滑动取消录音
 * @author  lei
 * @date  2018/3/28 0028 -- 上午 10:04.
 * 个人博客站: http://www.bestlei.top
 */
public class Main4Activity extends AppCompatActivity{

    private TextView mTvShowtext;
    private boolean isRecognizing = false;
    private NlsClient mNlsClient;
    private NlsRequest mNlsRequest;
    private Context context;
    Button yuyin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
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
        yuyin = (Button) findViewById(R.id.yuyin);
        yuyin.setOnTouchListener(new myOnTouchListener());
    }
    private class myOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event){
            int action = event.getAction();
            int start_x=0,start_y=0,end_x,end_y,mov_x,mov_y;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //按下
                    start_x=(int)event.getX();
                    start_y=(int)event.getY();
                    initStartRecognizing();
                    break;
                case MotionEvent.ACTION_UP:
                    //松开
                    end_x=(int) event.getX();
                    end_y=(int)event.getY();
                    mov_x=Math.abs(start_x-end_x);
                    mov_y=Math.abs(start_y-end_y);
                    if (mov_x > 200 || mov_y > 200) {
                        mNlsClient.cancel();
                        mTvShowtext.setText("");
                    } else {
                        initStopRecognizing();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    end_x=(int) event.getX();
                    end_y=(int)event.getY();
                    mov_x=Math.abs(start_x-end_x);
                    mov_y=Math.abs(start_y-end_y);
                    if (mov_x>150||mov_y>150) {
                        mNlsClient.cancel();
                        mTvShowtext.setText("");

                    }
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
    }

    private void initStopRecognizing() {
        isRecognizing = false;
        mTvShowtext.setText("");
        mNlsClient.stop();
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
                    Toast.makeText(Main4Activity.this, "录音错误", Toast.LENGTH_LONG).show();
                    break;
                case NlsClient.ErrorCode.RECORDING_ERROR:
                    Toast.makeText(Main4Activity.this, "录音识别错误", Toast.LENGTH_LONG).show();
                    break;
                case NlsClient.ErrorCode.NOTHING:
                    Toast.makeText(Main4Activity.this, "什么都没说", Toast.LENGTH_LONG).show();
                    break;
                case NlsClient.ErrorCode.USER_CANCEL:
                    Toast.makeText(Main4Activity.this, "用户取消录音", Toast.LENGTH_LONG).show();
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
}
