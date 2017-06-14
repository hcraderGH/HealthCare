package com.dafukeji.healthcare.util;

/**
 * Created by DevCheng on 2017/6/9.
 */

public class CommonUtils {

	/**
	 * 判断校验核是否正确
	 * @param data
	 * @return
	 */
	public static boolean IsCRCRight(byte[] data){

		//先判断获取的数据的字节数是否正确
		if (data.length!=11){
			return false;
		}

		int cmdSum=ConvertUtils.byte2unsignedInt(data[2]) +
				ConvertUtils.byte2unsignedInt(data[3])+ConvertUtils.byte2unsignedInt(data[4] )+
				ConvertUtils.byte2unsignedInt(data[5]) +
				ConvertUtils.byte2unsignedInt(data[6])+
				ConvertUtils.byte2unsignedInt(data[7])+
				ConvertUtils.byte2unsignedInt(data[8]);
		if (((cmdSum%256)== ConvertUtils.byte2unsignedInt(data[9]))) {
			return true;
		}else{
			return false;
		}
	}

	public static int eleFormula(int ele){
		int preEle = 0;
		if (ele>=41.5){
			preEle=100;
		}else if(ele>=40.8){
			preEle=90;
		}else if (ele>=40){
			preEle=80;
		}else if (ele>=39.3){
			preEle=70;
		}else if (ele>=38.7){
			preEle=60;
		}else if (ele>=38.2){
			preEle=50;
		}else if (ele>=37.9){
			preEle=40;
		}else if (ele>=37.7){
			preEle=30;
		}else if (ele>=37.3){
			preEle=20;
		}else if (ele>=37){
			preEle=15;
		}else if (ele>=38.2){
			preEle=50;
		}else if (ele>=36.8){
			preEle=10;
		}else if (ele>=35){
			preEle=5;
		}

		return preEle;
	}

}
