package com.example.xonteldvrdemo;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.Player.Core.PlayerClient;
import com.Player.Core.PlayerCore;
import com.Player.Core.Utils.CommenUtil;
import com.Player.web.websocket.ClientCore;
import com.example.xonteldvrdemo.umeyesdk.entity.PlayNode;

import java.util.ArrayList;
import java.util.List;

public class AppMain extends Application {
	private PlayerClient playerclient;
	public boolean isRun = false;

	@Override
	public void onCreate() {
		ClientCore.getInstance().init(this);
		PlayerCore.isNewRecordMode = true;
//		PlayerCore.isFFMPEG_2_8_15 = false;
		CommenUtil.isAcceptTargetQFileStorage = true;//是否使用androidQ兼容方案，如果不使用，在targetsdk=29时候须设置android:requestLegacyExternalStorage="true"

//		ClientCore.ver_api = "1.0.1";
		// 设置免登陆支持报警，如果服务器不支持，必须 设置ClientCore.isSuportLocalAlarmPush=false；
		ClientCore.isSuportLocalAlarmPush = false; // 默认是不支持
		playerclient = new PlayerClient();
		getLogData();

		super.onCreate();
	}

	/** this function gets log data  **/
	private void getLogData() {
		if (!isRun) {
			isRun = true;
			new Thread() {
				@Override
				public void run() {
					while (isRun) {
						String log = playerclient.CLTGetLogData(100);
						if (!TextUtils.isEmpty(log)) {
							Log.d("WriteLogThread ", log);
						}
					}
				}
			}.start();
		}
	}

	public synchronized PlayerClient getPlayerclient() {
		return playerclient;
	}


	public boolean isRun() {
		return isRun;
	}

	public void setRun(boolean isRun) {
		this.isRun = isRun;
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

}
