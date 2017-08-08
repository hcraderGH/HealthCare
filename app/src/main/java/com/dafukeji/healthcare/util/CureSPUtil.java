package com.dafukeji.healthcare.util;

import android.content.Context;

import com.dafukeji.healthcare.constants.Constants;

/**
 * Created by DevCheng on 2017/6/2.
 */

public class CureSPUtil {

	/**
	 * 通过点击的位置获取灸的温度
	 */
	public static int getTempByPosition(int position) {//记得同步改变RunningActivity中的getCauterizeGrade()
		int temp = 0;
		switch (position) {
			case 1:
				temp = 39+2;
				break;
			case 2:
				temp = 42+2;
				break;
			case 3:
				temp = 45+2;
				break;
			case 4:
				temp = 48+2;
				break;
			case 5:
				temp = 51;
				break;
		}
		return temp;
	}

	/**
	 * 通过点击的位置获取药的时间
	 *
	 * @param position
	 * @return
	 */
	public static int getMedicineTimeByPosition(int position) {
		int time = 0;
		switch (position) {
			case 1:
				time = 10;
				break;
			case 2:
				time = 15;
				break;
			case 3:
				time = 20;
				break;
			case 4:
				time = 25;
				break;
			case 5:
				time = 30;
				break;
		}
		return time;
	}


	/**
	 * 通过点击的位置获取灸的时间
	 *
	 * @param position
	 * @return
	 */
	public static int getCauterizeTimeByPosition(int position) {
		int time = 0;
		switch (position) {
			case 1:
				time = 5;
				break;
			case 2:
				time = 10;
				break;
			case 3:
				time = 15;
				break;
		}
		return time;
	}

	/**
	 * 通过点击的位置获取针的时间
	 *
	 * @param position 从1开始
	 * @return
	 */
	public static int getKneadTimeByPosition(int position) {
		int time = 0;
		switch (position) {
			case 1:
				time = 10;
//				time=1;//TODO测试
				break;
			case 2:
				time = 15;
				break;
			case 3:
				time = 20;
				break;
			case 4:
				time = 25;
				break;
			case 5:
				time = 30;
				break;
		}
		return time;
	}


	public static int getSP(String keyName,Context context) {
		SPUtils spUtils = new SPUtils(Constants.SP_CURE,context);
		return spUtils.getInt(keyName);
	}

	/**
	 * 偏好设置选择的参数
	 *
	 * @param keyName
	 * @param keyValue
	 */
	public static void setSP(String keyName, int keyValue,Context context) {
		SPUtils spUtils = new SPUtils(Constants.SP_CURE,context);
		spUtils.put(keyName, keyValue);
	}


	public static int getStimulateLevel(int level) {
		int stimulateLevel;
		if (level <= 7) {
			stimulateLevel = level + 2;
		} else {
			stimulateLevel = 9;
		}
		return stimulateLevel;
	}

	/**
	 * 属性是否设置了
	 *
	 * @return
	 */
	public static boolean isSaved(String keyName, Context context) {
		SPUtils spUtils;
		boolean isExist;
		spUtils = new SPUtils(Constants.SP_CURE,context);
		isExist = spUtils.contains(keyName);
		return isExist;
	}

	/**
	 * 设置发送的数据(目前不设置强刺激的具体强度和频率
	 *
	 * @return
	 */
	public static byte[] setSettingData(int stimulate,
	                              int cauterizeGrade, int cauterizeTime, int needleType,
	                              int needleGrade, int needleFrequency, int medicineTime) {
//		byte[] s = new byte[12];
//		s[0] = (byte) 0xFA;
//		s[1] = (byte) 0xFB;
//		s[2] = (byte) stimulate;
//		s[3] = (byte) CureSPUtil.getStimulateLevel(needleGrade);
//		s[4] = (byte) CureSPUtil.getStimulateLevel(needleFrequency);
//		s[5] = (byte) cauterizeGrade;
//		s[6] = (byte) cauterizeTime;
////		s[7] = (byte) needleType;//
//		s[7]=1;//TODO 目前只有按的功能
//		s[8] = (byte) (needleGrade+1);
//		s[9] = (byte) (needleFrequency+1);
//		s[10] = (byte) medicineTime;

		byte[] s = new byte[12];
		s[0] = (byte) 0xFA;
		s[1] = (byte) 0xFB;
		s[2] = (byte) 0;
		if (stimulate==1){
			s[3] = (byte) (needleGrade);
			s[4] = (byte) (needleFrequency);
			s[8] = (byte) (needleGrade+2);//Run宁Activity中相应改变
			s[9] = (byte) (needleFrequency+2);
		}else if (stimulate==0){
			s[3]=0;
			s[4]=0;
			s[8] = (byte) (needleGrade);
			s[9] = (byte) (needleFrequency);
		}
		s[5] = (byte) cauterizeGrade;
		s[6] = (byte) cauterizeTime;
//		s[7] = (byte) needleType;//
		s[7]=1;//TODO 目前只有按的功能
		s[10] = (byte) medicineTime;

		s[11] = (byte) (s[2] + s[3] + s[4] + s[5] + s[6] + s[7] + s[8] + s[9] + s[10]);
		return s;
	}
}
