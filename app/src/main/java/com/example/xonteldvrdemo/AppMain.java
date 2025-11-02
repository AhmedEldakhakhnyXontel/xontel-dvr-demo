package com.example.xonteldvrdemo;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;

import com.Player.Core.PlayerClient;
import com.Player.Core.PlayerCore;
import com.Player.Core.Utils.CommenUtil;
import com.Player.web.websocket.ClientCore;
import com.example.xonteldvrdemo.umeyesdk.entity.PlayNode;

import java.util.ArrayList;
import java.util.List;

public class AppMain extends Application {
	private PlayerClient playerclient;
	private List<PlayNode> nodeList;
	public boolean isRun = false;
	private int currentNodeId = 0; // 当前列表父节点的ID；
	private Handler handler = new Handler();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		nodeList = new ArrayList<PlayNode>();
		ClientCore.getInstance().init(this);
		PlayerCore.isNewRecordMode = true;
//		PlayerCore.isFFMPEG_2_8_15 = false;
		CommenUtil.isAcceptTargetQFileStorage = true;//是否使用androidQ兼容方案，如果不使用，在targetsdk=29时候须设置android:requestLegacyExternalStorage="true"

//		ClientCore.ver_api = "1.0.1";
		// 设置免登陆支持报警，如果服务器不支持，必须 设置ClientCore.isSuportLocalAlarmPush=false；
		ClientCore.isSuportLocalAlarmPush = false; // 默认是不支持
		playerclient = new PlayerClient();
//		if (!isRun) {
//			isRun = true;
//			new Thread() {
//
//				@Override
//				public void run() {
//
//					while (isRun) {
//						String log = pc.CLTLogData(100);
//						if (!TextUtils.isEmpty(log)) {
//							Log.d("WriteLogThread", log);
//						}
//					}
//
//				}
//
//			}.start();
//		}




		super.onCreate();
	}

	public synchronized PlayerClient getPlayerclient() {
		return playerclient;
	}

	public int getCurrentNodeId() {
		return currentNodeId;
	}

	public void setCurrentNodeId(int currentNodeId) {
		this.currentNodeId = currentNodeId;
	}

	public List<PlayNode> getNodeList() {
		return nodeList;
	}

	public synchronized List<PlayNode> getDvrAndCamera() {
		List<PlayNode> list = new ArrayList<PlayNode>();
		for (int i = 0; i < nodeList.size(); i++) {
			PlayNode dvrNode = nodeList.get(i);
			if (dvrNode.IsDvr()) {
				list.add(dvrNode);

			} else if (dvrNode.isCamera()
					&& TextUtils.isEmpty(dvrNode.getParentId())) {
				list.add(dvrNode);
			}
		}
		return list;
	}

	public void setNodeList(List<PlayNode> nodeList) {
		this.nodeList = nodeList;
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
