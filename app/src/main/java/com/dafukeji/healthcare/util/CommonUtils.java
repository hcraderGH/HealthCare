package com.dafukeji.healthcare.util;

/**
 * Created by DevCheng on 2017/6/9.
 */

public class CommonUtils {

	public static boolean IsCRCRight(byte[] data){
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
}
