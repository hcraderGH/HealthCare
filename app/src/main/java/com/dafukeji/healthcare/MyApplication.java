package com.dafukeji.healthcare;

import android.app.Activity;
import android.app.Application;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

	private List<Activity> mList = new ArrayList<Activity>();
	private static MyApplication instance;

	private static String TAG="测试";

	private static boolean isTest=true;//TODO 当不处于测试的时候应该设置为false

	@Override
	public void onCreate() {
		super.onCreate();



		//初始化Bugly
		Bugly.init(getApplicationContext(), "6e15e6f72b", isTest);//推荐测试时使用true，运行时使用false
//		CrashReport.initCrashReport(getApplicationContext(),"6e15e6f72b",isTest);

		if (isTest){
			//初始化内存泄露
			LeakCanary.install(this);
			//初始化日志
//			initLogger();//不习惯使用
		}
	}

	private void initLogger() {
		Logger.init(TAG)
				.methodCount(0)//配置Log中调用堆栈的函数行数,
//		.hideThreadInfo()//隐藏Log中的线程信息
		.methodOffset(2);// 设置调用堆栈的函数偏移值，0的话则从打印该Log的函数开始输出堆栈信息
//		.logLevel(isTest?LogLevel.FULL:LogLevel.NONE);//设置Log的是否输出，LogLevel.NONE即无Log输出
	}

	public synchronized static MyApplication getInstance() {
		if (instance == null) {
			instance = new MyApplication();
		}
		return instance;
	}

	public static boolean isTest(){
		return isTest;
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
