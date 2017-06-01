package com.dafukeji.healthcare.constants;

/**
 * Created by DevCheng on 2017/4/9.
 */

public class Constants {

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	public static final String NOTIFY_CHARACTERISTIC_UUID="0003cdd1-0000-1000-8000-00805f9b0131";

	public static final String WRITE_CHARACTERISTIC_UUID="0003cdd2-0000-1000-8000-00805f9b0131";

	public static final String ORIGINAL_TIME="original_time";

	public static final String RECEIVE_BLUETOOTH_INFO="receive_bluetooth_info";
	public static final String RECEIVE_GATT_STATUS="receive_gatt_status";
	public static final String EXTRAS_GATT_STATUS="extras_gatt_status";


	public static final String CURE_TYPE="cure_type";
	public static final int CURE_CAUTERIZE=3;
	public static final int CURE_NEEDLE=4;
	public static final int CURE_KNEAD=5;
	public static final int CURE_MEDICINE=6;
	public static final int DEVICE_POWER_OFF=2;

//	public static final String SP_CURE_CAUTERIZE="sp_cure_cauterize";
//	public static final String SP_CURE_NEEDLE="sp_cure_needle";
//	public static final String SP_CURE_MEDICINE="sp_cure_medicine";
//	public static final String SP_CURE_TIME="sp_cure_time";
//	public static final String SP_CURE_GRADE ="sp_cure_grade";
//	public static final String SP_CURE_INTENSITY="sp_cure_intensity";
//	public static final String SP_CURE_FREQUENCY="sp_cure_frequency";


	public static final String SP_CURE="sp_cure";
	public static final String SP_CAUTERIZE_GRADE="sp_cauterize_grade";
	public static final String SP_CAUTERIZE_TIME_GRADE="sp_cauterize_time_grade";
	public static final String SP_NEEDLE_TYPE="sp_needle_type";
	public static final String SP_KNEAD_GRADE="sp_knead_grade";
	public static final String SP_KNEAD_FREQUENCY="sp_knead_frequency";
//	public static final String SP_KNEAD_TIME_GRADE="sp_knead_time_grade";
	public static final String SP_MEDICINE_GRADE="sp_cauterize_grade";
	public static final String SP_MEDICINE_TIME_GRADE="sp_medicine_time_grade";

	public static final String BATTERY_ELECTRIC_QUANTITY="battery_electric_quantity";
	public static final String EXTRAS_BATTERY_ELECTRIC_QUANTITY="extras_battery_electric_quantity";


	public static final int EXTRAS_BATTERY_WARN=30;
	public static final int EXTRAS_BATTERY_DANGER=15;


//	public static final String SP_CURE_CAUTERIZE_TIME="sp_cure_cauterize_time";
//	public static final String SP_CURE_NEEDLE_TIME="sp_cure_needle_time";
//	public static final String SP_CURE_MEDICINE_TIME="sp_cure_medicine_time";

}
