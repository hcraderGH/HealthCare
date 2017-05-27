package com.dafukeji.healthcare.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by DevCheng on 2017/3/27.
 */

public class StatusBar {


	public static SystemBarTintManager tintManager;

	/**
	 * 设置沉浸式状态栏
	 * @param activity
	 * @param color toolbar的颜色
	 */
	@TargetApi(19)
	public static void setImmersiveStatusBar(Activity activity,int color){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			tintManager = new SystemBarTintManager(activity);
			tintManager.setStatusBarTintColor(activity.getResources().getColor(color));
			tintManager.setStatusBarTintEnabled(true);
		}
	}

	/**
	 * 设置沉浸式状态栏不覆盖状态栏
	 * @param activity
	 * @View view toolbar这个控件
	 * @param color toolbar的颜色
	 */
	@TargetApi(19)
	public static void setImmersiveStatusBar(Activity activity, View view, int color){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			tintManager = new SystemBarTintManager(activity);
			tintManager.setStatusBarTintColor(activity.getResources().getColor(color));
			tintManager.setStatusBarTintEnabled(true);
			int statusBarHeight=ScreenUtil.getStatusBarHeight(activity);
//			view.setPadding(0,statusBarHeight,0,0);
		}
	}
}
