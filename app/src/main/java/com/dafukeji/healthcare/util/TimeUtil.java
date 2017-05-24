package com.dafukeji.healthcare.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by DevCheng on 2017/5/24.
 */

public class TimeUtil {

	public static String date2String(long date,String formatString){
		SimpleDateFormat sdf=new SimpleDateFormat(formatString, Locale.CHINA);
		return sdf.format(date);
	}

	public static String[]  getSubtractedString(long after,long before){
		String [] time = new String[3];
		long s=after-before;
		int hour= (int) Math.floor(s/(60*60*1000));
		if (hour<10){
			time[0]="0"+hour;
		}else{
			time[0]=String.valueOf(hour);
		}

		int minute= (int) Math.floor((s-hour*60*60*1000)/(1000*60));
		if (minute<10){
			time[1]="0"+minute;
		}else {
			time[1]=String.valueOf(minute);
		}

		int second= (int) Math.floor((s-hour*60*60*1000-minute*1000*60)/1000);
		if (second<10){
			time[2]="0"+second;
		}else {
			time[2]=String.valueOf(second);
		}

		return time;
	}
}
