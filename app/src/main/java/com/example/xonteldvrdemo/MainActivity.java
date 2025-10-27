package com.example.xonteldvrdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.Player.Core.PlayerClient;
import com.Player.Core.PlayerCore;
import com.Player.Source.AudioDecodeListener;
import com.Player.Source.TAlarmFrame;
import com.Player.web.response.ResponseCommon;
import com.Player.web.websocket.ClientCore;
import com.example.xonteldvrdemo.umeyesdk.api.WebSdkApi;
import com.example.xonteldvrdemo.umeyesdk.utils.MyAudioDecodeThread;
import com.example.xonteldvrdemo.umeyesdk.utils.MyRecoredThread;
import com.example.xonteldvrdemo.umeyesdk.utils.MyVideoDecodeThread;
import com.video.h264.DecodeDisplay;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static String DAX_USER= "admin";// 设备密码 //zd1234
    public static String DAX_PASSWORD= "admin";// 设备密码 //zd1234
    public static String DAX_ADDRESS = "192.168.1.250";
    public static int DAX_PORT = 5800;
    public static int DAX_VENDOR_ID = 1009;
    public static int DAX_CHANNEL_NUMBER = 2;

    public static final byte SHOW_STATE = 0;

    public static final byte ALARM_STATE = 1;

    PlayerClient playClient;
    PlayerCore playerCore;
    ClientCore clientCore;
    ImageView img;
    Button btnPlay;
    AppMain appMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        appMain = (AppMain) this.getApplicationContext();
        playClient = appMain.getPlayerclient();
        clientCore = ClientCore.getInstance();
        initPlayCore();
        customDecode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new StateThread().start();
    }

    class StateThread extends Thread {

        @Override
        public void run() {

            try {
                while (true) {

                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = SHOW_STATE;
                    msg.arg1 = playerCore.PlayCoreGetCameraPlayerState();
                    if (playerCore.GetIsSnapVideo()) {
                        msg.arg2 = 1;
                    }
                    Log.w("state", "state: " + msg.arg1 + ",pc.GetIsPPT():"
                            + playerCore.GetIsPPT());
                    handler.sendMessage(msg);

                    TAlarmFrame tAlarmFrame = playerCore.CameraGetAlarmInfo();
                    if (tAlarmFrame != null) {
                        handler.sendMessage(Message.obtain(handler,
                                ALARM_STATE, tAlarmFrame));
                    }

                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                return;
            }
            Log.e("PlayActivity2", "state:" + msg.toString());
            Log.e("PlayActivity2", "state:" + msg.what);
            if (msg.what == SHOW_STATE) {
                Log.e("PlayActivity2", "state:" + msg.arg1);
            }
            super.handleMessage(msg);
        }

    };
    private void initViews() {
        img = findViewById(R.id.imgLive);
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(v -> play());
    }

    void login(){
        clientCore.setLocalList(true);
        ClientCore.isSuportLocalAlarmPush = true;// 设置支持免登陆报警

        WebSdkApi.loginServerAtUserId(clientCore, "", "", "",
                new Handler() {

                    @Override
                    public void handleMessage(Message msg) {
                        // TODO Auto-generated method stub

//                        clientCore.setUserPush(1,
//                                1,
//                                AlarmUtils.GETUI_CID, 1, 0, 0, new Handler() {
//
//                                    @Override
//                                    public void handleMessage(Message msg) {
//                                        ResponseCommon responseCommon = (ResponseCommon) msg.obj;
//                                        if (responseCommon != null
//                                                && responseCommon.h != null
//                                                && responseCommon.h.e == Errors.UM_WEB_API_SUCCESS) {
//                                            Log.i("setUserPush", "设置用户推送成功");
//                                        } else {
//                                            Log.i("setUserPush", "设置用户推送失败");
//                                        }
//                                    }
//                                });

                        play();
                        super.handleMessage(msg);
                    }
                });
    }
    public static boolean isZh(Context con) {
        Locale locale = con.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }


    private void play() {
        playerCore.StopAsync();
        //    playerCore.PlayP2P(Constants.UMID, Constants.user, Constants.password, Constants.iChNo, 1);
        playerCore.PlayAddress(DAX_VENDOR_ID, DAX_ADDRESS, DAX_PORT, DAX_USER,DAX_PASSWORD, DAX_CHANNEL_NUMBER, 1);
    }

    public void initPlayCore() {
        playerCore = new PlayerCore(this);
        playerCore.InitParam("", -1, img);
        playerCore.SetPPtMode(false);
        playerCore.isQueryDevInfo = true;
        playerCore.openWebRtcNs = true;
    }

    public void customDecode() {
        playerCore.setAudioDecodeListener(new AudioDecodeListener() {

            @Override
            public void StartTalk(PlayerCore playercore) { // TODO
                // 对讲、录音线程
                MyRecoredThread myRecoredThread = new MyRecoredThread(playercore);
                myRecoredThread.start();
            }

            @Override
            public void StartAudioDecode(PlayerCore playercore, DecodeDisplay decodeDisplay) { // TODO Auto-generated method stub //音频解码播放线程
                MyAudioDecodeThread AudioThreadDecode = new MyAudioDecodeThread(playercore, decodeDisplay);
                AudioThreadDecode.start();
            }

            @Override
            public void startVideoDecode(DecodeDisplay arg0) { // 视频解码线程
                MyVideoDecodeThread defualtVideoDecodeThread = new MyVideoDecodeThread(arg0);
                defualtVideoDecodeThread.start();
            }
        });
    }
}