package com.dafukeji.healthcare.util;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.dafukeji.healthcare.MyApplication;
import com.dafukeji.healthcare.ui.WelcomeActivity;

/**
 * Created by DevCheng on 2017/8/5.
 */

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private Application mApplication;


	public MyUncaughtExceptionHandler(Application application){
		this.mApplication=application;
		mDefaultHandler=Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (!handleException(e)&&mDefaultHandler!=null){
			//如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(t, e);
		}else{
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			Intent intent = new Intent(mApplication.getApplicationContext(), WelcomeActivity.class);
			PendingIntent restartIntent = PendingIntent.getActivity(
					mApplication.getApplicationContext(), -1, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			//退出程序
			AlarmManager mgr = (AlarmManager)mApplication.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
					restartIntent); // 1秒钟后重启应用
			MyApplication.getInstance().exit();
		}
	}


	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 *
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		//使用Toast来显示异常信息
		new Thread(){
			@Override
			public void run() {
				Looper.prepare();
				ToastUtil.showToast(mApplication.getApplicationContext(),"很抱歉,程序出现异常,即将退出.",1000);
				Looper.loop();
			}
		}.start();
		return true;
	}
}
