package com.dafukeji.healthcare.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by DevCheng on 2017/3/21.
 */

public class ConvertUtils {

	private ConvertUtils() {
		throw new UnsupportedOperationException("u can't instantiate me...");
	}

	private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	/**
	 * byteArr转hexString
	 * <p>例如：</p>
	 * bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns 00A8
	 *
	 * @param bytes 字节数组
	 * @return 16进制大写字符串
	 */
	public static String bytes2HexString(byte[] bytes) {
		if (bytes == null) return null;
		int len = bytes.length;
		if (len <= 0) return null;
		char[] ret = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
			ret[j++] = hexDigits[bytes[i] & 0x0f];
		}
		return new String(ret);
	}


	/**
	 * 将4字节的byte数组转成一个int值
	 * @param b
	 * @return
	 */
	public static int byteArray2int(byte[] b){
		byte[] a = new byte[4];
		int i = a.length - 1,j = b.length - 1;
		for (; i >= 0 ; i--,j--) {//从b的尾部(即int值的低位)开始copy数据
			if(j >= 0)
				a[i] = b[j];
			else
				a[i] = 0;//如果b.length不足4,则将高位补0
		}
		int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
		int v1 = (a[1] & 0xff) << 16;
		int v2 = (a[2] & 0xff) << 8;
		int v3 = (a[3] & 0xff) ;
		return v0 + v1 + v2 + v3;
	}



	/**
	 * 通过byte数组取得float
	 *
	 * @param b
	 * @return
	 */
	public static float getFloat(byte[] b) {
		int l;
		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		return Float.intBitsToFloat(l);
	}


	/**
	 * 通过byte数组获取double，Byte数组元素个数为8个
	 * @param b
	 * @return
	 */
	public static double getDouble(byte[] b) {
		long l;
		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		l &= 0xffffffffl;
		l |= ((long) b[4] << 32);
		l &= 0xffffffffffl;
		l |= ((long) b[5] << 40);
		l &= 0xffffffffffffl;
		l |= ((long) b[6] << 48);
		l &= 0xffffffffffffffl;
		l |= ((long) b[7] << 56);
		return Double.longBitsToDouble(l);
	}

	/**
	 * 为double类型设置小数点
	 * @param originalData 原始的double数值
	 * @param decimalPointCount 小数点的个数
	 * @return
	 */
	public static String setDecimalPointCount(double originalData,int decimalPointCount){
		StringBuilder stb=new StringBuilder("0.");
		for (int i = 1; i <decimalPointCount; i++) {
			stb.append("0");
		}
		stb.append("#");
		DecimalFormat df=new DecimalFormat(stb.toString());
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(originalData);
	}


	/**
	 * byte类型转为无符号位的int类型
	 * @param b byte
	 * @return int
	 */
	public static int byte2unsignedInt(byte b){
		return b&0xff;
	}
}
