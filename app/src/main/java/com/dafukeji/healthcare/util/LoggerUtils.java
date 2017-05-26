package com.dafukeji.healthcare.util;

import com.dafukeji.healthcare.MyApplication;
import com.orhanobut.logger.Logger;

public class LoggerUtils {

	public static final int VERBOSE=1;
	public static final int DEBUG=2;
	public static final int INFO=3;
	public static final int WARN=4;
	public static final int ERROR=5;
	public static final int NOTHING= 6;

	public static final int LEVEL=MyApplication.isTest()?VERBOSE:NOTHING;
	
	public static void v(String tag,String msg){
		if(LEVEL<=VERBOSE){
			Logger.v(tag, msg);
		}
	}
	
	public static void d(String tag,String msg){
		if(LEVEL<=DEBUG){
			Logger.d(tag, msg);
		}
	}
	
	public static void i(String tag,String msg){
		if(LEVEL<=INFO){
			Logger.i(tag, msg);
		}
	}
	
	public static void w(String tag,String msg){
		if(LEVEL<=WARN){
			Logger.w(tag, msg);
		}
	}
	
	public static void e(String tag,String msg){
		if(LEVEL<=ERROR){
			Logger.e(tag, msg);
		}
	}
}
