package com.dafukeji.healthcare.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.dafukeji.healthcare.MyApplication;
import com.dafukeji.healthcare.constants.Constants;

/**
 * Created by DevCheng on 2017/6/3.
 */

public class SettingManager {

	//消息推送
	private boolean NOTIFICATION=true;
	//免打扰模式
	private boolean NO_DISTURBING=false;
	//自动升级
	private boolean AUTO_UPDATE=true;

	private static SettingManager instance=new SettingManager();

	public static SettingManager getInstance(){
		return instance;
	}

	private  SettingManager(){

	}

	public boolean isNOTIFICATION() {
		SharedPreferences sp= MyApplication.getInstance().getSharedPreferences(Constants.APP_SETTING, Context.MODE_PRIVATE);
		NOTIFICATION=sp.getBoolean(Constants.APP_SETTING_NOTIFICATION,true);
		return NOTIFICATION;
	}

	public void setNOTIFICATION(boolean NOTIFICATION) {
		SharedPreferences sp= MyApplication.getInstance().getSharedPreferences(Constants.APP_SETTING, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=sp.edit();
		editor.putBoolean(Constants.APP_SETTING_NOTIFICATION,NOTIFICATION);
		editor.commit();
		this.NOTIFICATION = NOTIFICATION;
	}

	public boolean isNO_DISTURBING() {
		SharedPreferences sp= MyApplication.getInstance().getSharedPreferences(Constants.APP_SETTING, Context.MODE_PRIVATE);
		NO_DISTURBING=sp.getBoolean(Constants.APP_SETTING_NO_DISTURBING,true);
		return NO_DISTURBING;
	}

	public void setNO_DISTURBING(boolean NO_DISTURBING) {
		SharedPreferences sp= MyApplication.getInstance().getSharedPreferences(Constants.APP_SETTING, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=sp.edit();
		editor.putBoolean(Constants.APP_SETTING_NO_DISTURBING,NO_DISTURBING);
		editor.commit();
		this.NO_DISTURBING = NO_DISTURBING;
	}

	public boolean isAUTO_UPDATE() {
		SharedPreferences sp=MyApplication.getInstance().getSharedPreferences(Constants.APP_SETTING, Context.MODE_PRIVATE);
		AUTO_UPDATE=sp.getBoolean(Constants.APP_SETTING_AUTO_UPDATE,true);
		return AUTO_UPDATE;
	}

	public void setAUTO_UPDATE(boolean AUTO_UPDATE) {
		SharedPreferences sp= MyApplication.getInstance().getSharedPreferences(Constants.APP_SETTING, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=sp.edit();
		editor.putBoolean(Constants.APP_SETTING_AUTO_UPDATE,AUTO_UPDATE);
		editor.commit();
		this.AUTO_UPDATE = AUTO_UPDATE;
	}
}
