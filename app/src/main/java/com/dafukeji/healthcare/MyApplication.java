package com.dafukeji.healthcare;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class MyApplication extends Application {

	private List<Activity> mList = new ArrayList<Activity>();
	private static MyApplication instance;

	private static String TAG="测试MyApplication";

	@Override
	public void onCreate() {

		super.onCreate();
	}

	public synchronized static MyApplication getInstance() {
		if (instance == null) {
			instance = new MyApplication();
		}
		return instance;
	}


	public void addActivity(Activity activity) {
		mList.add(activity);
	}

	public void exit() {
		try {
			for (Activity activity : mList) {
				if (activity != null) {
					activity.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
