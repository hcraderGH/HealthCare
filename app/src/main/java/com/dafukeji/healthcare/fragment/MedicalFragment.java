package com.dafukeji.healthcare.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.bean.Frame;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.ui.DeviceScanActivity;
import com.dafukeji.healthcare.ui.RunningActivity;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.ToastUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerMedicalAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;

/**
 * Created by DevCheng on 2017/6/1.
 */
public class MedicalFragment extends Fragment {

	private View mView;
	private ViewPager mViewPager;
	private CardPagerMedicalAdapter mCardAdapter;
	private ShadowTransformer mCardShadowTransformer;

	private static String TAG = "测试MedicalFragment";

	private Button btnStart;

	private int mStimulate;//强刺激的类型，3表示关机
	private int mCauterizeGrade = 40;//初始的一档对应的温度
	private int mCauterizeTime = 10;//初始的加热时间
	private int mNeedleType = 1;//默认为按功能
	private int mNeedleGrade = 1;//默认为1档
	private int mNeedleFrequency = 1;//默认为1档
	private int mMedicineTime = 20;//默认为3档20分钟

	private boolean isGATTConnected;

	private boolean mSendNewCmdFlag;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_home_medical, container, false);
		initViews();

		return mView;
	}

	//注册接收的事件
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothDevice.ACTION_UUID);
		return intentFilter;
	}


	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
				LogUtil.i(TAG, "Only gatt, just wait");

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				LogUtil.i(TAG, "mGattUpdateReceiver断开了连接");


				isGATTConnected = false;
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //可以开始干活了


			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
				isGATTConnected = true;

				final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				if (data != null) {

					//TODO 接收数据处理
					LogUtil.i(TAG, "onReceive: " + Arrays.toString(data));

					//当校验码前面的数据相加不等于校验码时表示数据错误
					if (!(data[2] + data[3] + data[4] + data[5] + data[6]+data[7]+data[8]==ConvertUtils.byte2unsignedInt(data[9])) ) {
						return;
					}

					if (mSendNewCmdFlag) {
						Frame.curFrameId = ConvertUtils.byte2unsignedInt(data[8]);
						LogUtil.i(TAG,"Frame.curFrameId="+Frame.curFrameId);
						LogUtil.i(TAG,"Frame.preFrameId="+Frame.preFrameId);
						if (Frame.preFrameId == Frame.curFrameId) {
							byte[] settings = CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
									, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime);


							LogUtil.i(TAG,"已经进入了方法");
							DeviceScanActivity.getBluetoothLeService().WriteValue(settings);
						} else {
							mSendNewCmdFlag=false;

							Frame.preFrameId=Frame.curFrameId;
							Intent intent2 = new Intent(getActivity(), RunningActivity.class);
							intent2.putExtra(Constants.CURE_TYPE, Constants.CURE_MEDICAL);
							intent2.putExtra(Constants.ORIGINAL_TIME, mCauterizeTime + mMedicineTime);
							intent2.putExtra(Constants.CURRENT_TEMP, ConvertUtils.byte2unsignedInt(data[3]));
							intent2.putExtra(Constants.CURRENT_TIME,System.currentTimeMillis());
							getActivity().startActivity(intent2);
						}
					}
				}
			}
		}
	};

	private void initViews() {
		//初始化ViewPagerCard
		mViewPager = (ViewPager) mView.findViewById(R.id.vp_cure);

		mCardAdapter = new CardPagerMedicalAdapter(getActivity(), getActivity().getSupportFragmentManager());
		mCardAdapter.addCardItem(new CardItem(R.string.cauterize));
		mCardAdapter.addCardItem(new CardItem(R.string.needle));
		mCardAdapter.addCardItem(new CardItem(R.string.medical));

		mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
		mCardShadowTransformer.enableScaling(true);
		mViewPager.setAdapter(mCardAdapter);
		mViewPager.setPageTransformer(false, mCardShadowTransformer);
		mViewPager.setOffscreenPageLimit(3);

		btnStart = (Button) mView.findViewById(R.id.btn_medical_start);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!isGATTConnected) {
					Toasty.warning(getActivity(), "请连接设备", Toast.LENGTH_SHORT).show();
					return;
				}

				if (CureSPUtil.isSaved(Constants.SP_MEDICAL_STIMULATE, getActivity())) {
					mStimulate = CureSPUtil.getSP(Constants.SP_MEDICAL_STIMULATE, getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_GRADE, getActivity())) {
					mCauterizeGrade = CureSPUtil.getTempByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_GRADE, getActivity()));
				}
				if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_TIME_GRADE, getActivity())) {
					mCauterizeTime = CureSPUtil.getCauterizeTimeByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_TIME_GRADE, getActivity()));
				}
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_TYPE, getActivity())) {
					mNeedleType = CureSPUtil.getSP(Constants.SP_NEEDLE_TYPE, getActivity());
				}
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_GRADE, getActivity())) {
					mNeedleGrade = CureSPUtil.getSP(Constants.SP_NEEDLE_GRADE, getActivity());
				}
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_FREQUENCY, getActivity())) {
					mNeedleFrequency = CureSPUtil.getSP(Constants.SP_NEEDLE_FREQUENCY, getActivity());
				}
				if (CureSPUtil.isSaved(Constants.SP_MEDICINE_TIME_GRADE, getActivity())) {
					mMedicineTime = CureSPUtil.getMedicineTimeByPosition(CureSPUtil.getSP(Constants.SP_MEDICINE_TIME_GRADE, getActivity()));
				}

				LogUtil.i(TAG, "发送的药物治疗数据Settings:" + Arrays.toString(CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
						, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime)));

				if (DeviceScanActivity.getBluetoothLeService() == null) {
					return;
				}


				LogUtil.i(TAG, "HomeFragment.getBluetoothLeService()" + DeviceScanActivity.getBluetoothLeService());

				mSendNewCmdFlag = true;

				byte[] settings = CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
						, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime);

				LogUtil.i(TAG,"获取服务:"+DeviceScanActivity.getBluetoothLeService());

				DeviceScanActivity.getBluetoothLeService().WriteValue(settings);
			}
		});
	}


	@Override
	public void onStart() {
		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		super.onStart();
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mGattUpdateReceiver);
		super.onStop();
	}
}
