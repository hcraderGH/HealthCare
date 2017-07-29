package com.dafukeji.healthcare.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by DevCheng on 2017/7/27.
 */

public class SendCmdService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();


	}


	@Override
	public int onStartCommand(Intent intent,int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
