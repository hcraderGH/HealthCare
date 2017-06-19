package com.dafukeji.healthcare;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.CleanUtils;
import com.blankj.utilcode.util.Utils;
import com.dafukeji.healthcare.constants.Config;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.ui.MainActivity;
import com.dafukeji.healthcare.util.AppBlockCanaryContext;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.SPUtils;
import com.dafukeji.healthcare.util.SettingManager;
import com.facebook.stetho.DumperPluginsProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.dumpapp.DumperPlugin;
import com.github.moduth.blockcanary.BlockCanary;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.BuglyStrategy;
import com.tencent.bugly.beta.Beta;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

	private List<Activity> mList = new ArrayList<>();
	private static MyApplication instance;

	private static String TAG="测试MyApplication";
	private static boolean isClearSP=false;
	private static boolean isClearDB=false;
	private static boolean isTest=false ;//TODO 当不处于测试的时候应该设置为false


	private SPUtils mSpUtils;
	@Override
	public void onCreate() {
		super.onCreate();

		instance=this;

		//初始化代码工具
		Utils.init(this);
		setClearSPAndBD();//初次安装的时候是否清空偏好设置和数据库

		//初始化Bugly
		initBugly();

		//初始化Umeng
		initUmeng();

		if (isTest){
			//初始化日志
//			initLogger();//不习惯使用

			//初始化内存泄露
			LeakCanary.install(this);

			//初始化性能检测组件，找到卡顿元凶
			BlockCanary.install(this,new AppBlockCanaryContext()).start();

			//初始化Stetho
			Stetho.initializeWithDefaults(this);
		}

	}

	private void initBugly() {

		/***** Beta高级设置 *****/
		/**
		 * true表示app启动自动初始化升级模块; false不会自动初始化;
		 * 开发者如果担心sdk初始化影响app启动速度，可以设置为false，
		 * 在后面某个时刻手动调用Beta.init(getApplicationContext(),false);
		 */
		Beta.autoInit = true;

		/**
		 * true表示初始化时自动检查升级; false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法;
		 */
		LogUtil.i(TAG,"SettingManager.getInstance().isAUTO_UPDATE()"+SettingManager.getInstance().isAUTO_UPDATE());
		Beta.autoCheckUpgrade = SettingManager.getInstance().isAUTO_UPDATE();

		/**
		 * 设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略);
		 */
		Beta.upgradeCheckPeriod = 60 * 1000;
		/**
		 * 设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
		 */
		Beta.initDelay = 1 * 1000;

		//此处的通知栏指的是升级弹窗弹出后，点击升级后下载的状态
		/**
		 * 设置通知栏大图标，largeIconId为项目中的图片资源;
		 */
		Beta.largeIconId = R.mipmap.ic_launcher;
		/**
		 * 设置状态栏小图标，smallIconId为项目中的图片资源Id;
		 */
		Beta.smallIconId = R.mipmap.ic_launcher;
		/**
		 * 设置更新弹窗默认展示的banner，defaultBannerId为项目中的图片资源Id;
		 * 当后台配置的banner拉取失败时显示此banner，默认不设置则展示“loading“;
		 */
		Beta.defaultBannerId = R.mipmap.ic_default_banner;
		/**
		 * 设置sd卡的Download为更新资源保存目录;
		 * 后续更新资源会保存在此目录，需要在manifest中添加WRITE_EXTERNAL_STORAGE权限;
		 */
		Beta.storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		/**
		 * 已经确认过的弹窗在APP下次启动自动检查更新时会再次显示;
		 */
		Beta.showInterruptedStrategy = true;
		/**
		 * 只允许在MainActivity上显示更新弹窗，其他activity上不显示弹窗; 不设置会默认所有activity都可以显示弹窗;
		 */
		Beta.canShowUpgradeActs.add(MainActivity.class);

		/***** Bugly高级设置 *****/
		BuglyStrategy strategy = new BuglyStrategy();
		/**
		 * 设置app渠道号
		 */
		strategy.setAppChannel(Config.BUGLY_APP_CHANNEL);

		/***** 统一初始化Bugly产品，包含Beta *****/
		Bugly.init(this, Config.BUGLY_APP_ID,true,strategy);//推荐测试时使用true，运行时使用false
	}

	private void setClearSPAndBD() {
		//TODO 通过推送透传或者升级的方式将关于sp和db清空
		mSpUtils=new SPUtils(Constants.APP_SETTING,this);
		if (isClearSP){
			if (!mSpUtils.getBoolean(Constants.SP_CLEARED,false)) {
				CleanUtils.cleanInternalSP();
				if (mSpUtils==null){
					mSpUtils=new SPUtils(Constants.APP_SETTING,this);
				}
				mSpUtils.put(Constants.SP_CLEARED, true);
			}
		}

		if (isClearDB){
			if (!mSpUtils.getBoolean(Constants.DB_CLEARED,false)) {
				CleanUtils.cleanInternalDbs();//删除所有数据库
				mSpUtils.put(Constants.DB_CLEARED, true);
			}
		}
	}

	private void initUmeng() {
		PushAgent mPushAgent = PushAgent.getInstance(this);

		//注册推送服务，每次调用register方法都会回调该接口
		//注册推送服务，无论推送是否开启都需要调用此方法

		mPushAgent.register(new IUmengRegisterCallback() {

			@Override
			public void onSuccess(String deviceToken) {
				//注册成功会返回device token
				LogUtil.i("device token",deviceToken);
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
