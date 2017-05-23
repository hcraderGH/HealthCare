package com.dafukeji.healthcare.viewpagercards;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.dafukeji.healthcare.MyApplication;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.ui.RunningActivity;
import com.dafukeji.healthcare.util.ToastUtil;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.TimePickerDialog;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter, View.OnClickListener {

	private Button btnCauterizeGrade, btnNeedleGrade, btnMedicalTemp;
	private Button btnCauterizeTime, btnNeedleTime, btnMedicalTime;
	private Button btnCauterizeStart, btnNeedleStart, btnMedicalStart;
	private Dialog.Builder mBuilder;
	private DialogFragment mFragment;
	private int[] sustainTime = new int[2];
	private long originalTime;
	private String selectGrade;
	private FragmentManager mFragmentManager;

	private List<CardView> mViews;
	private List<CardItem> mData;
	private float mBaseElevation;
	private View mView;
	private Context mContext;
	private static String TAG = "测试CardPagerAdapter";

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

		switch (position) {
			case 0:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_cauterize, container, false);
				btnCauterizeGrade = (Button) mView.findViewById(R.id.btn_cauterize_grade);
				btnCauterizeTime = (Button) mView.findViewById(R.id.btn_cauterize_time);
				btnCauterizeStart = (Button) mView.findViewById(R.id.btn_cauterize_start);
				btnCauterizeGrade.setOnClickListener(this);
				btnCauterizeTime.setOnClickListener(this);
				btnCauterizeStart.setOnClickListener(this);
				break;
			case 1:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_needle, container, false);
				btnNeedleGrade = (Button) mView.findViewById(R.id.btn_needle_grade);
				btnNeedleTime = (Button) mView.findViewById(R.id.btn_needle_time);
				btnNeedleStart = (Button) mView.findViewById(R.id.btn_needle_start);
				btnNeedleGrade.setOnClickListener(this);
				btnNeedleTime.setOnClickListener(this);
				btnNeedleStart.setOnClickListener(this);
				break;
			case 2:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_medicine, container, false);
				btnMedicalTemp = (Button) mView.findViewById(R.id.btn_medical_temp);
				btnMedicalTime = (Button) mView.findViewById(R.id.btn_medical_time);
				btnMedicalStart = (Button) mView.findViewById(R.id.btn_medical_start);
				btnMedicalTemp.setOnClickListener(this);
				btnMedicalTime.setOnClickListener(this);
				btnMedicalStart.setOnClickListener(this);
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
			case R.id.btn_cauterize_grade:
				getGrade(new String[]{"一档", "二档", "三档", "四档", "五档"}, btnCauterizeGrade, "请选择灸的强度");
				break;
			case R.id.btn_needle_grade:
				getGrade(new String[]{"一档", "二档", "三档", "四档", "五档"}, btnNeedleGrade, "请选择针的强度");
				break;
			case R.id.btn_medical_temp:
				getGrade(new String[]{"40℃", "42℃", "44℃", "46℃", "48℃", "50℃"}, btnMedicalTemp, "请选择药的加热温度");
				break;
			case R.id.btn_cauterize_time:
				getSustainTime(btnCauterizeTime);
				break;
			case R.id.btn_needle_time:
				getSustainTime(btnNeedleTime);
				break;
			case R.id.btn_medical_time:
				getSustainTime(btnMedicalTime);
				break;
			case R.id.btn_cauterize_start:

				if (!HomeFragment.getBlueToothStatus()) {
					ToastUtil.showToast(mContext, "请连接设备", 1000);
					return;
				}

				if (btnCauterizeTime.getText().toString().equals("0分钟")) {
					ToastUtil.showToast(mContext, "请设定持续时间", 1000);
					return;
				} else {
					byte[] settings = new byte[]{0x31, 0x32, 0x33};
//                    mBluetoothLeService.WriteValue(settings);
					HomeFragment.getBluetoothLeService().WriteValue(settings);
					Intent intent = new Intent(mContext, RunningActivity.class);
					Log.i(TAG, "onClick: originalTime" + originalTime);
					intent.putExtra(Constants.CURE_TYPE, Constants.CURE_CAUTERIZE);
					intent.putExtra(Constants.ORIGINAL_TIME, originalTime);
					mContext.startActivity(intent);
				}

				break;
			case R.id.btn_needle_start:
				if (!HomeFragment.getBlueToothStatus()) {
					ToastUtil.showToast(mContext, "请连接设备", 1000);
					return;
				}

				if (btnCauterizeTime.getText().toString().equals("0分钟")) {
					ToastUtil.showToast(mContext, "请设定持续时间", 1000);
					return;
				} else {
					byte[] settings = new byte[]{0x31, 0x32, 0x33};
//                    mBluetoothLeService.WriteValue(settings);
					HomeFragment.getBluetoothLeService().WriteValue(settings);
					Intent intent = new Intent(mContext, RunningActivity.class);
					Log.i(TAG, "onClick: originalTime" + originalTime);
					intent.putExtra(Constants.CURE_TYPE, Constants.CURE_NEEDLE);
					intent.putExtra(Constants.ORIGINAL_TIME, originalTime);
					mContext.startActivity(intent);
				}
				break;
			case R.id.btn_medical_start:
				if (!HomeFragment.getBlueToothStatus()) {
					ToastUtil.showToast(mContext, "请连接设备", 1000);
					return;
				}

				if (btnCauterizeTime.getText().toString().equals("0分钟")) {
					ToastUtil.showToast(mContext, "请设定持续时间", 1000);
					return;
				} else {
					byte[] settings = new byte[]{0x31, 0x32, 0x33};
//                    mBluetoothLeService.WriteValue(settings);
					HomeFragment.getBluetoothLeService().WriteValue(settings);
					Intent intent = new Intent(mContext, RunningActivity.class);
					Log.i(TAG, "onClick: originalTime" + originalTime);
					intent.putExtra(Constants.CURE_TYPE, Constants.CURE_MEDICINE);
					intent.putExtra(Constants.ORIGINAL_TIME, originalTime);
					mContext.startActivity(intent);
				}
				break;
		}
	}


	private String displayTime(int[] time) {
		String displayTime;
		int hour = time[0];
		int minute = time[1];
		if (hour == 0) {
			displayTime = minute + "分钟";
		} else if (minute < 10) {
			displayTime = hour + "小时" + "0" + minute + "分钟";
		} else {
			displayTime = hour + "小时" + minute + "分钟";
		}
		return displayTime;
	}

	private void getSustainTime(final Button btnTime) {

		RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
				.setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
					@Override
					public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {

						sustainTime[0] = hourOfDay;
						sustainTime[1] = minute;
						Log.i(TAG, "onPositiveActionClicked: sustainTime" + sustainTime[0] + "   " + sustainTime[1]);
						originalTime = (sustainTime[0] * 60 + sustainTime[1]) * 60 * 1000;
						Log.i(TAG, "onPositiveActionClicked: originalTime" + originalTime);
						btnTime.setText(displayTime(sustainTime));
						ToastUtil.showToast(mContext, "您选择的持续时间是" + hourOfDay + "小时" + minute + "分钟", 1500);
					}
				})
				.setStartTime(00, 00)
				.setDoneText("确定")
				.setCancelText("取消")
				.setThemeLight();
		rtpd.show(mFragmentManager, "timePickerDialogFragment");

//        if (mBuilder != null) {
//            return;
//        }
//        mBuilder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light, 24, 00) {
//            @Override
//            public void onPositiveActionClicked(DialogFragment fragment) {
//                TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
//                int hour = dialog.getHour();
//                int minute = dialog.getMinute();
//                sustainTime[0]=hour;
//                sustainTime[1]=minute;
//                Log.i(TAG, "onPositiveActionClicked: sustainTime"+sustainTime[0]+"   "+sustainTime[1]);
//                originalTime =(sustainTime[0]*60+sustainTime[1])*60*1000;
//                Log.i(TAG, "onPositiveActionClicked: originalTime"+ originalTime);
//                btnTime.setText(displayTime(sustainTime));
//                ToastUtil.showToast(mContext, "您选择的持续时间是" +hour+"小时"+minute+"分钟", 1500);
//                mBuilder=null;
//                mFragment=null;
//                super.onPositiveActionClicked(fragment);//此代码必须放在下面
//            }
//
//            @Override
//            public void onNegativeActionClicked(DialogFragment fragment) {
//                mBuilder=null;
//                mFragment=null;
//                super.onNegativeActionClicked(fragment);
//            }
//        };
//        mBuilder.positiveAction("确定")
//                .negativeAction("取消");
//        mFragment = DialogFragment.newInstance(mBuilder);
//        mFragment.show(mFragmentManager, null);
	}

	private void getGrade(final String[] grade, final Button btn, String reminder) {

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
				.setTitle(reminder)
				.setSingleChoiceItems(grade, 1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectGrade = grade[which];
					}
				})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						btn.setText(selectGrade);
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
}
