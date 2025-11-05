package com.example.xonteldvrdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.Player.Core.PlayerCore;
import com.Player.Source.SDKError;
import com.Player.Source.SetRecodeVideoListener;
import com.Player.Source.TAlarmFrame;
import com.Player.Source.TDevChannelInfo;
import com.Player.web.response.ResponseServer;
import com.Player.web.websocket.ClientCore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private PlayerCore playerCore;
    private ClientCore clientCore;
    private ImageView img;
    private Button btnPlay, snapshot, record, listChannels;
    private final SetRecodeVideoListener recodeVideoListener = new SetRecodeVideoListener() {
        @Override
        public void record(boolean b, Bitmap bitmap) {
            Log.e("MainActivity", "recording: " + b);
        }

        @Override
        public void finish(boolean b, String s) {
            Log.e("MainActivity", "record finish: " + b + " path: " + s);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        clientCore = ClientCore.getInstance();
        initPlayCore();
    }

    private void initViews() {
        img = findViewById(R.id.imgLive);
        btnPlay = findViewById(R.id.btnPlay);
        snapshot = findViewById(R.id.snapshot);
        record = findViewById(R.id.record);
        listChannels = findViewById(R.id.listChannels);

        btnPlay.setOnClickListener(v -> play());
        snapshot.setOnClickListener(v -> takeSnapshot());
        record.setOnClickListener(v -> recordVideo());
        listChannels.setOnClickListener(v -> {
            new Thread(() -> {
                List<TDevChannelInfo> channels = getChannelList(playerCore, DAX_USER);
                for (TDevChannelInfo channel : channels) {
                    Log.e("MainActivity", "Channel: " + channel.toString());
                }
            }).start();
        });
    }

    private void recordVideo() {

        playerCore.setRecodeVideoListener(recodeVideoListener);

        if (playerCore.GetIsSnapVideo()) {
            playerCore.SetSnapVideo(false);
        } else {
            if (playerCore.GetPlayerState() == SDKError.Statue_PLAYING) {

                File albumPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                playerCore.SetVideoPath(albumPath.getAbsolutePath(), "Xontel DVR Demo " + System.currentTimeMillis() + ".mp4");
                playerCore.SetSnapVideo(true);
            }
        }
    }

    private void takeSnapshot() {
        File albumPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        playerCore.SetAlbumPath(albumPath.getAbsolutePath(), "Xontel DVR Demo " + System.currentTimeMillis() + ".jpg");
        playerCore.setSnapListener((b, s, bitmap) -> Log.e("MainActivity", "b" + b + " snapshot path: " + s));
        playerCore.SetSnapPicture(true);
    }

    @SuppressLint("HandlerLeak")
    private void play() {
        ClientCore.isAPLanMode = true;
        playerCore.iCustom = true;
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


    @WorkerThread
    private List<TDevChannelInfo> getChannelList(PlayerCore playerCore,String userName) {
        final List<TDevChannelInfo> channels = new ArrayList<>();
        if (userName == null || userName.isEmpty()) {
            Log.w("getChannelList", "Username is empty");
            return channels;
        }

        // Prepare channel iterator on the device
        int rc = playerCore.QueryChannleList(userName); // SDK: 0 == success
        if (rc != 0) {
            Log.d("getChannelList", "Failed to query channel list, rc=" + rc);
            return channels;
        }

        // Pull items until SDK returns null
        for (;;) {
            TDevChannelInfo ch = playerCore.GetNextChannel();
            if (ch == null) break;
            channels.add(TDevChannelInfo.copy(ch)); // copy: avoids buffer reuse issues
        }
        return channels;
    }
    class StateThread extends Thread {
        @SuppressLint("HandlerLeak")
        Handler handler = new Handler() {


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