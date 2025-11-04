package com.example.xonteldvrdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.Player.Core.PlayerClient;
import com.Player.Core.PlayerCore;
import com.Player.Source.AudioDecodeListener;
import com.Player.Source.TAlarmFrame;
import com.Player.web.response.ResponseServer;
import com.Player.web.websocket.ClientCore;
import com.example.xonteldvrdemo.umeyesdk.utils.MyAudioDecodeThread;
import com.example.xonteldvrdemo.umeyesdk.utils.MyRecoredThread;
import com.example.xonteldvrdemo.umeyesdk.utils.MyVideoDecodeThread;
import com.video.h264.DecodeDisplay;

public class MainActivity extends AppCompatActivity {
    public static final byte SHOW_STATE = 0;
    public static final byte ALARM_STATE = 1;
    public static String DAX_USER = "admin";// 设备密码 //zd1234
    public static String DAX_PASSWORD = "admin";// 设备密码 //zd1234
    public static String DAX_ADDRESS = "192.168.1.250";
    public static int DAX_PORT = 5800;
    public static int DAX_VENDOR_ID = 1009;
    public static int DAX_CHANNEL_NUMBER = 2;
    public static int DAX_LANGUAGE = 1; //2 is for chinese

    PlayerCore playerCore;
    ClientCore clientCore;
    ImageView img;
    Button btnPlay;
    AppMain appMain;
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                return;
            }
            Log.e("MainActivity", "state:" + msg.toString());
            Log.e("MainActivity", "state:" + msg.what);
            if (msg.what == SHOW_STATE) {
                Log.e("MainActivity", "state:" + msg.arg1);
            }
            super.handleMessage(msg);
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        appMain = (AppMain) this.getApplicationContext();
        clientCore = ClientCore.getInstance();
        initPlayCore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new StateThread().start();
    }

    private void initViews() {
        img = findViewById(R.id.imgLive);
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(v -> play());
    }

    @SuppressLint("HandlerLeak")
    private void play() {
        ClientCore.isAPLanMode = true;
        ClientCore.setHttps(null);

        // if isAPLanMode true, will only use local lan to find device and no need to handle message in handler because it will always be null
        clientCore.getCurrentBestServer(new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) { // TODO
                ResponseServer responseServer = (ResponseServer) msg.obj;
                if (responseServer == null) {
                    // we asume that we are in local network
                    playerCore.StopAsync();
                    playerCore.PlayAddress(DAX_VENDOR_ID, DAX_ADDRESS, DAX_PORT, DAX_USER, DAX_PASSWORD, DAX_CHANNEL_NUMBER, 1);
                }
                super.handleMessage(msg);
            }
        });

    }

    public void initPlayCore() {
        playerCore = new PlayerCore(this);
        playerCore.InitParam("", -1, img);
        playerCore.SetPPtMode(false);
        playerCore.isQueryDevInfo = true;
        playerCore.openWebRtcNs = true;
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
                    Log.w("state", "state: " + msg.arg1 + ",pc.GetIsPPT():" + playerCore.GetIsPPT());
                    handler.sendMessage(msg);

                    TAlarmFrame tAlarmFrame = playerCore.CameraGetAlarmInfo();
                    if (tAlarmFrame != null) {
                        handler.sendMessage(Message.obtain(handler, ALARM_STATE, tAlarmFrame));
                    }

                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

    }
}