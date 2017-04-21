package com.dafukeji.healthcare.util;


import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dafukeji.healthcare.R;

public class ToastUtil {

	private static TextView textView;
	private static View v;
	private static Toast mToast;
	private static Handler mHandler = new Handler();
	private static Runnable r = new Runnable() {
		public void run() {
			mToast.cancel();
		}
	};

	public static void showToast(Context mContext, String text, int duration) {

		mHandler.removeCallbacks(r);
		if (mToast != null){
			textView.setText(text);
		}
		else{
			v = LayoutInflater.from(mContext).inflate(R.layout.eplay_toast, null);
			textView = (TextView) v.findViewById(R.id.tv_toast);
			textView.setText(text);
			mToast = new Toast(mContext);
			mToast.setDuration(duration);
			mToast.setView(v);
		}
//			mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
			
		mHandler.postDelayed(r, duration);

		mToast.show();
	}

	public static void showToast(Context mContext, int resId, int duration) {
		showToast(mContext, mContext.getResources().getString(resId), duration);
	}
	
	
	/**
	 * 取消Toast
	 */
	public static void cancelToast(){
		if(mToast!=null){
			mToast.cancel();
		}
	}
	
	/**
	 * Toast在界面中显示的位置
	 * @param gravity
	 * @param xOffset
	 * @param yOffset
	 */
	public void setGravity(int gravity, int xOffset, int yOffset) {
		if (mToast != null) {
			mToast.setGravity(gravity, xOffset, yOffset);
		}
	}
}
