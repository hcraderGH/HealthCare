package com.dafukeji.healthcare;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MyApplication extends Application {

	private List<Activity> mList = new ArrayList<Activity>();
	private static MyApplication instance;
	private static Context context;


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

	public static Context getContext(){
		return context;
	}
}
