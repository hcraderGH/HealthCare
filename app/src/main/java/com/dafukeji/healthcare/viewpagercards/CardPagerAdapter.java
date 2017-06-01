package com.dafukeji.healthcare.viewpagercards;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.SPUtils;
import com.dafukeji.healthcare.util.ToastUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter, View.OnClickListener {

	private Button btnCauterizeGrade, btnKneadGrade, btnMedicalGrade;
	private Button btnCauterizeTime, btnNeedleTime, btnMedicalTime;
//	private Button btnCauterizeStart, btnNeedleStart, btnMedicalStart;
	private Button btnKneadFrequency;
	private Button btnNeedleType;

	private Spinner spnCauterizeGrade;


	private int[] sustainTime = new int[2];
	private int originalTime=1;//不能默认为0否则出错之后被除数会为0
	private String selectGrade;
	private FragmentManager mFragmentManager;

	private List<CardView> mViews;
	private List<CardItem> mData;
	private float mBaseElevation;
	private View mView;
	private Context mContext;

	private static String TAG="测试CardPagerAdapter";

	private String[] mCauterizeGrades=new String[]{"一档", "二档", "三档", "四档", "五档","六档"};
	private String[] mKneadGrade=new String[]{"一档", "二档", "三档", "四档", "五档","六档", "七档", "八档", "九档"};
	private String[] mKneadFrequency=new String[]{"一档", "二档", "三档", "四档", "五档","六档", "七档", "八档", "九档"};
	private String[] mMedicineGrade =new String[]{"一档", "二档", "三档", "四档", "五档"};

	public CardPagerAdapter(Context context, FragmentManager fragmentManager) {
		this.mFragmentManager = fragmentManager;
		this.mContext = context;
		mData = new ArrayList<>();
		mViews = new ArrayList<>();
	}

	public void addCardItem(CardItem item) {
		mViews.add(null);
		mData.add(item);
	}

	public float getBaseElevation() {
		return mBaseElevation;
	}

	@Override
	public CardView getCardViewAt(int position) {
		return mViews.get(position);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		int type=position+Constants.CURE_CAUTERIZE;//TODO 如果类型的常量改变了这边也要改变

		Logger.i("instantiateItem: type"+type);

		switch (position) {
			case 0:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_cauterize, container, false);
//				btnCauterizeGrade = (Button) mView.findViewById(R.id.btn_cauterize_grade);
//				btnCauterizeTime = (Button) mView.findViewById(R.id.btn_cauterize_time);
//				btnCauterizeStart = (Button) mView.findViewById(R.id.btn_cauterize_start);
//				btnCauterizeGrade.setOnClickListener(this);
//				btnCauterizeTime.setOnClickListener(this);
//				btnCauterizeStart.setOnClickListener(this);

//				if (isSaved(type,Constants.SP_CURE_INTENSITY)){
//					btnCauterizeGrade.setText(mCauterizeGrades[getSP(type,Constants.SP_CURE_INTENSITY)]);
//				}
//
//				if (isSaved(type,Constants.SP_CURE_TIME)){
//					btnCauterizeTime.setText(getSP(type,Constants.SP_CURE_TIME)+"分钟");
//				}

				break;
			case 1:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_needle, container, false);
//				btnKneadGrade = (Button) mView.findViewById(R.id.btn_knead_grade);
//				btnNeedleTime = (Button) mView.findViewById(R.id.btn_knead_time);
//				btnNeedleStart = (Button) mView.findViewById(R.id.btn_needle_start);
//				btnKneadFrequency = (Button) mView.findViewById(R.id.btn_knead_frequency);
//				btnKneadFrequency.setOnClickListener(this);
//				btnKneadGrade.setOnClickListener(this);
//				btnNeedleTime.setOnClickListener(this);
//				btnNeedleStart.setOnClickListener(this);
//
//				if (isSaved(type,Constants.SP_CURE_INTENSITY)) {
//					btnKneadGrade.setText(mKneadGrade[getSP(type,Constants.SP_CURE_INTENSITY)]);
//				}
//				if (isSaved(type,Constants.SP_CURE_TIME)) {
//					btnNeedleTime.setText(getSP(type,Constants.SP_CURE_TIME)+"分钟");
//				}
//				if (isSaved(type,Constants.SP_CURE_FREQUENCY)) {
//					btnKneadFrequency.setText(mKneadFrequency[getSP(type,Constants.SP_CURE_FREQUENCY)]);
//				}
				break;
			case 2:

				break;
			case 3:
//				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_medicine, container, false);
//				btnMedicalGrade = (Button) mView.findViewById(R.id.btn_medical_grade);
//				btnMedicalTime = (Button) mView.findViewById(R.id.btn_medical_time);
//				btnMedicalStart = (Button) mView.findViewById(R.id.btn_medical_start);
//				btnMedicalGrade.setOnClickListener(this);
//				btnMedicalTime.setOnClickListener(this);
//				btnMedicalStart.setOnClickListener(this);
//
//				if (isSaved(type,Constants.SP_CURE_GRADE)) {
//					btnMedicalGrade.setText(mMedicineGrade[getSP(type,Constants.SP_CURE_GRADE)]);
//				}
//				if (isSaved(type,Constants.SP_CURE_TIME)) {
//					btnMedicalTime.setText(getSP(type,Constants.SP_CURE_TIME)+"分钟");
//				}
				break;
		}

		container.addView(mView);
		bind(mData.get(position), mView);
		CardView cardView = (CardView) mView.findViewById(R.id.cardView);

		if (mBaseElevation == 0) {
			mBaseElevation = cardView.getCardElevation();
		}

		cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
		mViews.set(position, cardView);
		return mView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
		mViews.set(position, null);
	}

	private void bind(CardItem item, View view) {
		TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
		titleTextView.setText(item.getTitle());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//			case R.id.btn_cauterize_grade:
//				getGrade(mCauterizeGrades,
//						btnCauterizeGrade, "请选择灸的强度",Constants.CURE_CAUTERIZE,Constants.SP_CURE_INTENSITY);
//				break;
//			case R.id.btn_knead_grade:
//				getGrade(mKneadGrade,
//						btnKneadGrade, "请选择针的强度",Constants.CURE_NEEDLE,Constants.SP_CURE_INTENSITY);
//				break;
//
//			case R.id.btn_knead_frequency:
//				getGrade(mKneadFrequency,
//						btnKneadFrequency, "请选择针的频率",Constants.CURE_NEEDLE,Constants.SP_CURE_FREQUENCY);
//				break;
//
//			case R.id.btn_medical_grade://温度相当于强度
//				getGrade(mMedicineGrade,
//						btnMedicalGrade, "请选择药的加热温度",Constants.CURE_MEDICINE,Constants.SP_CURE_GRADE);
//				break;
//			case R.id.btn_cauterize_time:
//				getSustainTime(btnCauterizeTime,Constants.CURE_CAUTERIZE);
//				break;
//			case R.id.btn_knead_time:
//				getSustainTime(btnNeedleTime,Constants.CURE_NEEDLE);
//				break;
//			case R.id.btn_medical_time:
//				getSustainTime(btnMedicalTime,Constants.CURE_MEDICINE);
//				break;
//			case R.id.btn_cauterize_start:
//
//				if (!HomeFragment.getBlueToothStatus()) {
////					ToastUtil.showToast(mContext, "请连接设备", 1000);
//					Toasty.warning(mContext,"请连接设备", Toast.LENGTH_SHORT).show();
//					return;
//				}
//
//				if (btnCauterizeTime.getText().toString().equals("0分钟")) {
//					ToastUtil.showToast(mContext, "请设定持续时间", 1000);
//					return;
//				} else {
//					//TODO　目前只是发档位并不是发真实的频率、温度值等
//					byte[] settings;
//					int type=Constants.CURE_CAUTERIZE;
//					int temp=getSP(type,Constants.SP_CURE_INTENSITY);
//					int intensity=0;
//					int time=getSP(type,Constants.SP_CURE_TIME);
//					int frequency=0;
//					settings=setSettingData(type,temp,intensity,time,frequency);
//					LogUtil.i(TAG,"onClick: setting btn_cauterize_start"+ Arrays.toString(settings));
//					HomeFragment.getBluetoothLeService().WriteValue(settings);
//
//					Intent intent = new Intent(mContext, RunningActivity.class);
//					LogUtil.i(TAG,"originalTime btn_cauterize_start" + getSP(type,Constants.SP_CURE_TIME));
//					intent.putExtra(Constants.CURE_TYPE, Constants.CURE_CAUTERIZE);
//					intent.putExtra(Constants.ORIGINAL_TIME, getSP(type,Constants.SP_CURE_TIME));
//					mContext.startActivity(intent);
//				}
//
//				break;
//			case R.id.btn_needle_start:
//				if (!HomeFragment.getBlueToothStatus()) {
////					ToastUtil.showToast(mContext, "请连接设备", 1000);
//					Toasty.warning(mContext,"请连接设备", Toast.LENGTH_SHORT).show();
//					return;
//				}
//
//				if (btnNeedleTime.getText().toString().equals("0分钟")) {
//					ToastUtil.showToast(mContext, "请设定持续时间", 1000);
//					return;
//				} else {
//					byte[] settings;
//					int type=Constants.CURE_NEEDLE;
//					int temp=0;
//					int intensity=getSP(type,Constants.SP_CURE_INTENSITY);
//					int time=getSP(type,Constants.SP_CURE_TIME);
//					int frequency=getSP(type,Constants.SP_CURE_FREQUENCY);
//					settings=setSettingData(type,temp,intensity,time,frequency);
//					LogUtil.i(TAG,"onClick: setting btn_needle_start"+ Arrays.toString(settings));
//
//					HomeFragment.getBluetoothLeService().WriteValue(settings);
//					Intent intent = new Intent(mContext, RunningActivity.class);
//					LogUtil.i(TAG,"onClick: originalTime btn_needle_start" + getSP(type,Constants.SP_CURE_TIME));
//					intent.putExtra(Constants.CURE_TYPE, Constants.CURE_NEEDLE);
//					intent.putExtra(Constants.ORIGINAL_TIME, getSP(type,Constants.SP_CURE_TIME));
//					mContext.startActivity(intent);
//				}
//				break;
//			case R.id.btn_medical_start:
//				if (!HomeFragment.getBlueToothStatus()) {
////					ToastUtil.showToast(mContext, "请连接设备", 1000);
//					Toasty.warning(mContext,"请连接设备", Toast.LENGTH_SHORT).show();
//					return;
//				}
//
//				if (btnMedicalTime.getText().toString().equals("0分钟")) {
//					ToastUtil.showToast(mContext, "请设定持续时间", 1000);
//					return;
//				} else {
//					byte[] settings;
//					int type=Constants.CURE_MEDICINE;
//					int temp=getSP(type,Constants.SP_CURE_GRADE);
//					int intensity=0;
//					int time=getSP(type,Constants.SP_CURE_TIME);//传递的时间单位为分钟
//					int frequency=0;
//					settings=setSettingData(type,temp,intensity,time,frequency);
//					LogUtil.i(TAG,"onClick: setting btn_medical_start"+ Arrays.toString(settings));
//
//					HomeFragment.getBluetoothLeService().WriteValue(settings);
//					Intent intent = new Intent(mContext, RunningActivity.class);
//					LogUtil.i(TAG,"onClick: originalTime btn_medical_start" + getSP(type,Constants.SP_CURE_TIME));
//					intent.putExtra(Constants.CURE_TYPE, Constants.CURE_MEDICINE);
//					intent.putExtra(Constants.ORIGINAL_TIME, getSP(type,Constants.SP_CURE_TIME));//传递的时间单位为分钟
//					mContext.startActivity(intent);
//				}
//				break;
		}
	}


	/**
	 * 设置发送的数据
	 * @param type
	 * @return
	 */
	private byte[] setSettingData(int type,int temp,int intensity,int time,int frequency){
		byte[] s=new byte[9];
		s[0]= (byte) 0xFA;
		s[1]= (byte) 0xFB;
		s[2]= (byte) type;
		s[3]= (byte) temp;
		s[4]= (byte) intensity;
		s[5]= (byte) time;
		s[6]= (byte) frequency;

		s[7]= (byte) (s[0]+s[1]+s[2]+s[3]+s[4]+s[5]+s[6]);
		s[8]= (byte) 0xFE;
		return s;
	}

	private String displayTime(int[] time) {
		String displayTime;
		int hour = time[0];
		int minute = time[1];

		//显示为00时00分钟
//		if (hour == 0) {
//			displayTime = minute + "分钟";
//		} else if (minute < 10) {
//			displayTime = hour + "小时" + "0" + minute + "分钟";
//		} else {
//			displayTime = hour + "小时" + minute + "分钟";
//		}

		//显示为00分钟
		if (hour == 0) {
			displayTime = minute + "分钟";
		} else {
			displayTime=(hour*60+minute)+"分钟";
		}
		return displayTime;
	}

	private void getSustainTime(final Button btnTime, final String keyName) {

		RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
				.setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
					@Override
					public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {

						sustainTime[0] = hourOfDay;
						sustainTime[1] = minute;
						Logger.i("onPositiveActionClicked: sustainTime" + sustainTime[0] + "   " + sustainTime[1]);
						originalTime = sustainTime[0] * 60 + sustainTime[1];

						setSP(keyName,originalTime);//保存的时间单位为分钟

						LogUtil.i(TAG,"getSustainTime: originalTime"+originalTime);
						btnTime.setText(displayTime(sustainTime));

//						ToastUtil.showToast(mContext, "您选择的持续时间是" + hourOfDay + "小时" + minute + "分钟", 1500);
						ToastUtil.showToast(mContext, "您选择的持续时间是" + (hourOfDay *60+ minute) + "分钟", 1500);
					}
				})
				.setStartTime(00, 00)
				.setDoneText("确定")
				.setCancelText("取消")
				.setThemeLight();
		rtpd.show(mFragmentManager, "timePickerDialogFragment");

	}


	/**
	 * 属性是否设置了
	 * @return
	 */
//	private boolean isSaved(String keyName){
//		SPUtils spUtils;
//		boolean isExist = false;
//		switch (type){
//			case Constants.CURE_CAUTERIZE:
//				spUtils=new SPUtils(Constants.SP_CURE_CAUTERIZE,mContext);
//				isExist=spUtils.contains(keyName);
//				break;
//			case Constants.CURE_NEEDLE:
//				spUtils=new SPUtils(Constants.SP_CURE_NEEDLE,mContext);
//				isExist=spUtils.contains(keyName);
//				break;
//			case Constants.CURE_MEDICINE:
//				spUtils=new SPUtils(Constants.SP_CURE_MEDICINE,mContext);
//				isExist=spUtils.contains(keyName);
//		}
//		return isExist;
//	}


	/**
	 * 偏好设置选择的参数
	 * @param keyName
	 * @param keyValue
	 */
	private void setSP(String keyName, int keyValue){
		SPUtils spUtils=new SPUtils(Constants.SP_CURE,mContext);
		spUtils.put(keyName,keyValue);

	}


	private int getSP(String spName, String keyName){
		SPUtils spUtils=new SPUtils(spName,mContext);
		return spUtils.getInt(keyName);
	}

	private int mWhich;
//	private void getGrade(final String[] grade, final Button btn, String reminder
//			, final int type, final String keyName) {
//
//		int checkedItem;
//		if (isSaved(type,keyName)){
//			checkedItem=getSP(type,keyName);
//		}else{
//			checkedItem=0;
//		}
//
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
//				.setTitle(reminder)
//				.setSingleChoiceItems(grade, checkedItem , new DialogInterface.OnClickListener() {//0表示的是默认的第一个选项
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						selectGrade = grade[which];
//						mWhich=which;
//
//					}
//				})
//				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						setSPOfType(type,keyName,mWhich);
//						Logger.i("onClick: getSP"+getSP(type,keyName));
//
//						btn.setText(selectGrade);
//						dialog.dismiss();
//					}
//				})
//				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//		builder.create().show();
//	}
}
