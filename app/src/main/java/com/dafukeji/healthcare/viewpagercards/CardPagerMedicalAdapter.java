package com.dafukeji.healthcare.viewpagercards;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

public class CardPagerMedicalAdapter extends PagerAdapter implements CardAdapter,AdapterView.OnItemSelectedListener {

//	private Button btnCauterizeGrade, btnKneadGrade, btnMedicalGrade;
//	private Button btnCauterizeTime, btnNeedleTime, btnMedicalTime;
////	private Button btnCauterizeStart, btnNeedleStart, btnMedicalStart;
//	private Button btnKneadFrequency;
//	private Button btnNeedleType;


	private Spinner spnCauterizeGrade, spnCauterizeTimeGrade;
	private Spinner spnNeedleType, spnNeedleGrade, spnNeedleFrequency;
	private Spinner spnMedicineTimeGrade;

	private SwitchCompat scStimulate;

	private FragmentManager mFragmentManager;

	private List<CardView> mViews;
	private List<CardItem> mData;
	private float mBaseElevation;
	private View mView;
	private Context mContext;

	private static String TAG = "测试CardPagerMedicalAdapter";

	private String[] mCauterizeGrades = new String[]{"一档", "二档", "三档", "四档", "五档"};
	private String[] mCauterizeTimeGrades = new String[]{"10分钟", "20分钟", "30分钟"};

	private String[] mNeedleTypes = new String[]{"按", "提", "左旋按", "右旋按", "左旋提", "右旋提"};
	private String[] mNeedleGrades = new String[]{"一档", "二档", "三档", "四档", "五档", "六档", "七档", "八档", "九档"};
	private String[] mNeedleFrequencies = new String[]{"一档", "二档", "三档", "四档", "五档", "六档", "七档", "八档", "九档"};

	private String[] mMedicineTimeGrades = new String[]{"10分钟", "15分钟", "20分钟", "25分钟", "30分钟"};

	public CardPagerMedicalAdapter(Context context, FragmentManager fragmentManager) {
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
		switch (position) {
			case 0:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_cauterize, container, false);
				spnCauterizeGrade = (Spinner) mView.findViewById(R.id.spn_cauterize_grade);
				ArrayAdapter<String> adapter0 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mCauterizeGrades);
				spnCauterizeGrade.setAdapter(adapter0);
				spnCauterizeGrade.setOnItemSelectedListener(this);

				if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_GRADE,mContext)) {
					spnCauterizeGrade.setSelection(CureSPUtil.getSP(Constants.SP_CAUTERIZE_GRADE,mContext));
				}

				spnCauterizeTimeGrade = (Spinner) mView.findViewById(R.id.spn_cauterize_time_grade);
				ArrayAdapter<String> adapter01 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mCauterizeTimeGrades);
				spnCauterizeTimeGrade.setAdapter(adapter01);
				spnCauterizeTimeGrade.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_TIME_GRADE,mContext)) {
					spnCauterizeTimeGrade.setSelection(CureSPUtil.getSP(Constants.SP_CAUTERIZE_TIME_GRADE,mContext));
				}

				scStimulate = (SwitchCompat) mView.findViewById(R.id.switch_stimulate);
				scStimulate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							CureSPUtil.setSP(Constants.SP_MEDICAL_STIMULATE, isChecked ? 1 : 0,mContext);
					}
				});

				if (CureSPUtil.isSaved(Constants.SP_MEDICAL_STIMULATE,mContext)){
					scStimulate.setChecked(CureSPUtil.getSP(Constants.SP_MEDICAL_STIMULATE,mContext)==1);
				}

				break;
			case 1:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_needle, container, false);

				spnNeedleType = (Spinner) mView.findViewById(R.id.spn_needle_type);
				ArrayAdapter<String> adapter1 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mNeedleTypes);
				spnNeedleType.setAdapter(adapter1);
				spnNeedleType.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_TYPE,mContext)) {
					spnNeedleType.setSelection(CureSPUtil.getSP(Constants.SP_NEEDLE_TYPE,mContext));
				}

				spnNeedleGrade = (Spinner) mView.findViewById(R.id.spn_needle_grade);
				ArrayAdapter<String> adapter11 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mNeedleGrades);
				spnNeedleGrade.setAdapter(adapter11);
				spnNeedleGrade.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_GRADE,mContext)) {
					spnNeedleGrade.setSelection(CureSPUtil.getSP(Constants.SP_NEEDLE_GRADE,mContext));
				}

				spnNeedleFrequency = (Spinner) mView.findViewById(R.id.spn_needle_frequency);
				ArrayAdapter<String> adapter12 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mNeedleFrequencies);
				spnNeedleFrequency.setAdapter(adapter12);
				spnNeedleFrequency.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_FREQUENCY,mContext)) {
					spnNeedleFrequency.setSelection(CureSPUtil.getSP(Constants.SP_NEEDLE_FREQUENCY,mContext));
				}

				break;

			case 2:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_medicine, container, false);
				spnMedicineTimeGrade = (Spinner) mView.findViewById(R.id.spn_medical_time_grade);
				ArrayAdapter<String> adapter3 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mMedicineTimeGrades);
				spnMedicineTimeGrade.setAdapter(adapter3);
				spnMedicineTimeGrade.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_MEDICINE_TIME_GRADE,mContext)) {
					spnMedicineTimeGrade.setSelection(CureSPUtil.getSP(Constants.SP_MEDICINE_TIME_GRADE,mContext));
				}

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
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
			case R.id.spn_cauterize_grade:
				CureSPUtil.setSP(Constants.SP_CAUTERIZE_GRADE, position,mContext);
				break;
			case R.id.spn_cauterize_time_grade:
				CureSPUtil.setSP(Constants.SP_CAUTERIZE_TIME_GRADE, position,mContext);
				break;

			case R.id.spn_needle_type:
				CureSPUtil.setSP(Constants.SP_NEEDLE_TYPE, position,mContext);//0表示无，所以在此处+1
				break;
			case R.id.spn_needle_grade:
				CureSPUtil.setSP(Constants.SP_NEEDLE_GRADE, position,mContext);//0表示无，所以在此处+1,一共9档
				break;
			case R.id.spn_needle_frequency:
				CureSPUtil.setSP(Constants.SP_NEEDLE_FREQUENCY, position,mContext);//0表示无，所以在此处+1,一共9档
				break;

			case R.id.spn_medical_time_grade:
				CureSPUtil.setSP(Constants.SP_MEDICINE_TIME_GRADE, position,mContext);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

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

//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
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
//		}
//	}


//	private void getSustainTime(final Button btnTime, final String keyName) {
//
//		RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
//				.setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
//					@Override
//					public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
//
//						sustainTime[0] = hourOfDay;
//						sustainTime[1] = minute;
//						Logger.i("onPositiveActionClicked: sustainTime" + sustainTime[0] + "   " + sustainTime[1]);
//						originalTime = sustainTime[0] * 60 + sustainTime[1];
//
//						setSP(keyName,originalTime);//保存的时间单位为分钟
//
//						LogUtil.i(TAG,"getSustainTime: originalTime"+originalTime);
//						btnTime.setText(displayTime(sustainTime));
//
////						ToastUtil.showToast(mContext, "您选择的持续时间是" + hourOfDay + "小时" + minute + "分钟", 1500);
//						ToastUtil.showToast(mContext, "您选择的持续时间是" + (hourOfDay *60+ minute) + "分钟", 1500);
//					}
//				})
//				.setStartTime(00, 00)
//				.setDoneText("确定")
//				.setCancelText("取消")
//				.setThemeLight();
//		rtpd.show(mFragmentManager, "timePickerDialogFragment");
//
//	}

}

