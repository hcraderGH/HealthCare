package com.dafukeji.healthcare;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.CleanUtils;
import com.blankj.utilcode.util.Utils;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.SPUtils;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import java.util.ArrayList;
import java.util.List;

import static anet.channel.util.Utils.context;

public class MyApplication extends Application {

	private List<Activity> mList = new ArrayList<Activity>();
	private static MyApplication instance;

	private static String TAG="测试";
	private static boolean isClearSP=false;
	private static boolean isTest=true;//TODO 当不处于测试的时候应该设置为false

	@Override
	public void onCreate() {
		super.onCreate();

		//初始化代码工具
		Utils.init(this);

		//初始化Bugly
		Bugly.init(getApplicationContext(), "6e15e6f72b", isTest);//推荐测试时使用true，运行时使用false
//		CrashReport.initCrashReport(getApplicationContext(),"6e15e6f72b",isTest);

		if (isTest){
			//初始化内存泄露
//			LeakCanary.install(this);
			//初始化日志
//			initLogger();//不习惯使用

			CleanUtils.cleanInternalDbByName(Constants.CURE_DB_NAME);//测试时删除数据库
		}

		//TODO 通过推送透传或者升级的方式将关于Cure的配置清空

		//初始化Umeng
		initUmeng();

	}

	private void initUmeng() {
		PushAgent mPushAgent = PushAgent.getInstance(this);
		//注册推送服务，每次调用register方法都会回调该接口
		//注册推送服务，无论推送是否开启都需要调用此方法
		mPushAgent.register(new IUmengRegisterCallback() {

			@Override
			public void onSuccess(String deviceToken) {
				//注册成功会返回device token
				Log.i("device token",deviceToken);
			}

			@Override
			public void onFailure(String s, String s1) {

			}
		});
		mPushAgent.setDebugMode(isTest);//false关闭日志输出，默认为true输出日志
		UmengNotificationClickHandler notificationClickHandler=new UmengNotificationClickHandler(){
			@Override
			public void dealWithCustomAction(Context context, UMessage uMessage) {
				//在此处理自定义行为，其中自定义行为的内容，存放在UMessage.custom中
				//UmengNotificationClickHandler是在BroadcastReceiver中被调用，因此若需启动Activity，需为Intent添加Flag：Intent.FLAG_ACTIVITY_NEW_TASK，否则无法启动Activity。
			}
		};
		mPushAgent.setNotificationClickHandler(notificationClickHandler);

		//自定义通知栏样式
		UmengMessageHandler messageHandler = new UmengMessageHandler() {
			@Override
			public Notification getNotification(Context context, UMessage msg) {
				switch (msg.builder_id) {
//					case 1:
//
					default:
						//默认为0，若填写的builder_id并不存在，也使用默认。
						return super.getNotification(context, msg);
				}
			}
		};
		mPushAgent.setMessageHandler(messageHandler);
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
