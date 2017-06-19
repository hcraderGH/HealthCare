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
		int preEle;
		preEle= (int) Math.floor((ele-35)*100/(41.5-35));
		if (preEle>=100){
			preEle=100;
		}else if(preEle>=90){
			preEle=90;
		}else if (preEle>=60){
			preEle=60;
		}else if (preEle>=45){
			preEle=45;
		}else if(preEle>=30){
			preEle=30;
		}else if(preEle>=15){
			preEle=15;
		}else{
			preEle=5;
		}
		return preEle;
	}
}
