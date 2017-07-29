package com.dafukeji.healthcare.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.bean.Frame;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.ui.RunningActivity;
import com.dafukeji.healthcare.util.CommonUtils;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.ToastUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerPhysicalAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

/**
 * Created by DevCheng on 2017/6/1.
 */

public class PhysicalFragment extends Fragment {

	private View mView;
	private ViewPager mViewPager;
	private CardPagerPhysicalAdapter mCardAdapter;
	private ShadowTransformer mCardShadowTransformer;

	private Button btnStart;

	private int mStimulate;//强刺激的类型，3表示关机
	private int mKneadType = 1;//默认为按功能
	private int mKneadGrade = 1;//默认为1档
	private int mKneadFrequency = 1;//默认为1档
	private int mKneadTime = 20;//默认为3档20分钟

	private static String TAG = "测试PhysicalFragment";

	private boolean isGATTConnected;

	private boolean mSendNewCmdFlag;

	private byte[] frontData;
	private byte[] wholeData;

	private BlueToothBroadCast mBlueToothBroadCast;

	@Override
	public void onAttach(Context context) {
		//注册接受蓝牙信息的广播
		mBlueToothBroadCast = new BlueToothBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.RECEIVE_GATT_STATUS_FROM_HOME);
		getActivity().registerReceiver(mBlueToothBroadCast, filter);
		super.onAttach(context);
	}

	class BlueToothBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//得到蓝牙的服务连接
			isGATTConnected = intent.getBooleanExtra(Constants.EXTRAS_GATT_STATUS_FORM_HOME, false);
			LogUtil.i(TAG, "onReceive  isGATTConnectedFromHome:" + isGATTConnected);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_home_physical, container, false);
		initViews();
		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
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

				byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				LogUtil.i(TAG, "onReceive: " + (data == null ? "data为null" : Arrays.toString(data)));
				if (data != null) {
					//TODO 接收数据处理
					boolean crcIsRight = CommonUtils.IsCRCRight(data);
					if (!crcIsRight) {
						//误码纠正
						if (data.length > 11) {
							frontData = new byte[data.length - 11];
							System.arraycopy(data, 11, frontData, 0, frontData.length);
							LogUtil.i(TAG, "截取的frontData:" + Arrays.toString(frontData));
							data = Arrays.copyOfRange(data, 0, 11);
							if (!CommonUtils.IsCRCRight(data)) {
								return;
							}
							LogUtil.i(TAG, "截取的data:" + Arrays.toString(data));
						} else if (data.length < 11) {
							wholeData = new byte[11];
							if (frontData != null) {
								System.arraycopy(frontData, 0, wholeData, 0, frontData.length);
								System.arraycopy(data, 0, wholeData, frontData.length, data.length);
								data = wholeData;
								LogUtil.i(TAG, "拼接的data：" + Arrays.toString(data));
								if (!CommonUtils.IsCRCRight(data)) {
									return;
								}
								wholeData = null;
								frontData = null;
							} else {
								return;
							}
						} else {//data.length==11

						}
					}

					if (mSendNewCmdFlag && data[2] != 6) {

						mSendNewCmdFlag=false;
						stopTimer();

						Intent intent2 = new Intent(getActivity(), RunningActivity.class);
						intent2.putExtra(Constants.CURE_TYPE, Constants.CURE_PHYSICAL);
						intent2.putExtra(Constants.ORIGINAL_TIME, mKneadTime);
						intent2.putExtra(Constants.CURRENT_TEMP, ConvertUtils.byte2unsignedInt(data[3]));
						intent2.putExtra(Constants.CURRENT_TIME, System.currentTimeMillis());
						getActivity().startActivity(intent2);
					}
				}
			}
		}
	};

	private Timer mTimer;
	private TimerTask mTimerTask;
	private int retryConfigCount;
	private boolean isConfigReceived;
	private void startConfigTimer(){
		if (mTimer!=null){
			mTimer=new Timer();
		}
		if (mTimerTask!=null){
			mTimerTask=new TimerTask() {
				@Override
				public void run() {
					if (!isConfigReceived){
						if (retryConfigCount>=6){
							ToastUtil.showToast(getActivity(),"断开连接",1000);
							stopTimer();
							Intent intent=new Intent();
							intent.putExtra(Constants.EXTRAS_GATT_STATUS,false);
							intent.setAction(Constants.RECEIVE_GATT_STATUS);
							getActivity().sendBroadcast(intent);
						}else{
							retryConfigCount++;
							isConfigReceived=false;
							sendPhysicalCmd();
						}
					}
				}
			};
		}
		if (mTimer!=null&&mTimerTask!=null){
			mTimer.schedule(mTimerTask,0,400);
		}
	}

	private void stopTimer(){
		if (mTimer!=null){
			mTimer.cancel();
			mTimer=null;
		}
		if (mTimerTask!=null){
			mTimerTask.cancel();
			mTimerTask=null;
		}
	}

	private void initViews() {
		//初始化ViewPagerCard
		mViewPager = (ViewPager) mView.findViewById(R.id.vp_cure);

		mCardAdapter = new CardPagerPhysicalAdapter(getActivity(), getActivity().getSupportFragmentManager());
		mCardAdapter.addCardItem(new CardItem(R.string.needle));
		mCardAdapter.addCardItem(new CardItem(R.string.needle));

		mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
		mCardShadowTransformer.enableScaling(true);
		mViewPager.setAdapter(mCardAdapter);
		mViewPager.setPageTransformer(false, mCardShadowTransformer);
		mViewPager.setOffscreenPageLimit(2);

		btnStart = (Button) mView.findViewById(R.id.btn_physical_start);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {


				if (!isGATTConnected) {
					Toasty.warning(getActivity(), "请连接设备", getResources().getInteger(R.integer.toasty_duration)).show();
					return;
				}
				if (CureSPUtil.isSaved(Constants.SP_PHYSICAL_STIMULATE, getActivity())) {
					mStimulate = CureSPUtil.getSP(Constants.SP_PHYSICAL_STIMULATE, getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TYPE, getActivity())) {
					mKneadType = CureSPUtil.getSP(Constants.SP_KNEAD_TYPE, getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_GRADE, getActivity())) {
					mKneadGrade = CureSPUtil.getSP(Constants.SP_KNEAD_GRADE, getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_FREQUENCY, getActivity())) {
					mKneadFrequency = CureSPUtil.getSP(Constants.SP_KNEAD_FREQUENCY, getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TIME_GRADE, getActivity())) {
					mKneadTime = CureSPUtil.getKneadTimeByPosition(CureSPUtil.getSP(Constants.SP_KNEAD_TIME_GRADE, getActivity()));
				}

				HomeFragment.stopTimer();

				mSendNewCmdFlag = true;
				startConfigTimer();


			}
		});
	}

	private void sendPhysicalCmd(){
		int cauterizeGrade = 0;
		int cauterizeTime = 0;
		HomeFragment.getBluetoothLeService().WriteValue(CureSPUtil.setSettingData(mStimulate, cauterizeGrade, cauterizeTime
				, mKneadType, mKneadGrade, mKneadFrequency, mKneadTime));
	}

	@Override
	public void onDestroy() {
		LogUtil.i(TAG, "onDestroy()");
		getActivity().unregisterReceiver(mGattUpdateReceiver);
		getActivity().unregisterReceiver(mBlueToothBroadCast);
		super.onDestroy();
	}
}
