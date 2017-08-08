package com.dafukeji.healthcare.util;

public class CommonUtils {

	/**
	 * 判断校验核是否正确
	 *
	 * @param data
	 * @return
	 */
	public static boolean IsCRCRight(byte[] data) {

		//先判断获取的数据的字节数是否正确
		if (data.length != 13) {
			return false;
		}

		int cmdSum =
				ConvertUtils.byte2unsignedInt(data[2]) +
						ConvertUtils.byte2unsignedInt(data[3]) +
						ConvertUtils.byte2unsignedInt(data[4]) +
						ConvertUtils.byte2unsignedInt(data[5]) +
						ConvertUtils.byte2unsignedInt(data[6]) +
						ConvertUtils.byte2unsignedInt(data[7]) +
						ConvertUtils.byte2unsignedInt(data[8]) +
						ConvertUtils.byte2unsignedInt(data[9]) +
						ConvertUtils.byte2unsignedInt(data[10]);
		if (((cmdSum % 256) == ConvertUtils.byte2unsignedInt(data[11]))) {
			return true;
		} else {
			return false;
		}
	}

	public static int eleFormula(int voltage) {
		int perEle;
		if (voltage >= 42) {
			perEle = 100;
		} else if (voltage == 41) {
			perEle = 90;
		} else if (voltage == 40) {
			perEle = 80;
		} else if (voltage == 39) {
			perEle = 60;
		} else if (voltage == 38) {
			perEle = 40;
		} else if (voltage == 37) {
			perEle = 20;
		} else if (voltage == 36) {
			perEle = 10;
		} else {
			perEle = 5;
		}
		return perEle;
	}


	/**
	 * 通过剩余电量来分段电量的显示
	 * @param perEle
	 * @return
	 */
	public static int getOptimizePerEle(int perEle) {
		int ele = 0;
		if (perEle >= 90) {
			ele = 100;
		} else if (perEle >= 80) {
			ele = 90;
		} else if (perEle >= 60) {
			ele = 80;
		} else if (perEle >= 40) {
			ele = 60;
		} else if (perEle >= 20) {
			ele = 40;
		} else if (perEle >= 10) {
			ele = 20;
		} else if (perEle >= 5) {
			ele = 10;
		} else {
			ele = 5;
		}
		return ele;
	}


	public static int getRunningVoltageByGrade(int grade){
		int voltage=0;
		switch (grade){
			case 9:
				voltage=46;
				break;
			case 18:
				voltage=60;
				break;
			case 27:
				voltage=74;
				break;
			case 36:
				voltage=88;
				break;
			case 50:
				voltage=115;
				break;
			case 75:
				voltage=128;
				break;
			case 90:
				voltage=135;
				break;
			case 115:
				voltage=146;
				break;
			case 140:
				voltage=158;
				break;
			case 165:
				voltage=170;
				break;
			case 200:
				voltage=182;
				break;
		}
		return voltage;
	}
}