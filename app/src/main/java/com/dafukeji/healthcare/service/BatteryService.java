package com.dafukeji.healthcare.service;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.bean.Battery;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.ui.MainActivity;
import com.wenming.library.NotifyUtil;

import java.util.List;

/**
 * Created by DevCheng on 2017/5/27.
 */

public class BatteryService extends Service {


	private BlueToothBroadCast mBlueToothBroadCast;
	private int bat=100;
	private long beforeTime;
	private long currentTime;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {

		//注册接受蓝牙电量的广播
		mBlueToothBroadCast=new BlueToothBroadCast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constants.BATTERY_ELECTRIC_QUANTITY);
		registerReceiver(mBlueToothBroadCast,filter);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		beforeTime=System.currentTimeMillis();
		return super.onStartCommand(intent, flags, startId);

	}

	private void DangerNotify() {
		int requestCode=0;
		Intent notifyIntent = new Intent(this, MainActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(this,
				requestCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		int smallIcon = R.mipmap.ic_tai_ji;
		String ticker = "您有一条新通知";
		String title = "温馨提醒";
		String content = "连接的设备电量只剩下"+bat+"%，请及时充电";

		NotifyUtil notify1 = new NotifyUtil(this, 1);
		notify1.notify_normal_singline(pIntent, smallIcon, ticker, title, content, true, true, false);
	}


	class BlueToothBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//得到蓝牙的信息
			bat= intent.getIntExtra(Constants.EXTRAS_BATTERY_ELECTRIC_QUANTITY,0);

			if (bat<Constants.EXTRAS_BATTERY_DANGER&&(!isRunningForeground())) {
				if (Battery.isFirstNotify) {
					DangerNotify();
				}else{
					if (currentTime-beforeTime>60*60*1000){//每隔1个小时通知一次
						beforeTime=currentTime;
						DangerNotify();
					}
				}
			}
		}
	}

	public boolean isRunningForeground(){
		String packageName=getPackageName(this);
		String topActivityClassName=getTopActivityName(this);
		System.out.println("packageName="+packageName+",topActivityClassName="+topActivityClassName);
		if (packageName!=null&&topActivityClassName!=null&&topActivityClassName.startsWith(packageName)) {
			System.out.println("---> isRunningForeGround");
			return true;
		} else {
			System.out.println("---> isRunningBackGround");
			return false;
		}
	}

	public  String getTopActivityName(Context context){
		String topActivityClassName=null;
		ActivityManager activityManager =
				(ActivityManager)(context.getSystemService(android.content.Context.ACTIVITY_SERVICE )) ;
		List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1) ;
		if(runningTaskInfos != null){
			ComponentName f=runningTaskInfos.get(0).topActivity;
			topActivityClassName=f.getClassName();
		}
		return topActivityClassName;
	}

	public String getPackageName(Context context){
		String packageName = context.getPackageName();
		return packageName;
	}
}
