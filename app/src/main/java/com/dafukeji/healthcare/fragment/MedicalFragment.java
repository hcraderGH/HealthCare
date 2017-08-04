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
import android.widget.Button;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.ui.RunningActivity;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.ToastUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerMedicalAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

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

	private BlueToothBroadCast mBlueToothBroadCast;

	private byte[] frontData;
	private byte[] wholeData;
	private byte[] settings;

	@Override
	public void onAttach(Context context) {
		//注册接受蓝牙信息的广播

		LogUtil.i(TAG,"onAttach()");
		mBlueToothBroadCast=new BlueToothBroadCast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constants.RECEIVE_GATT_STATUS_FROM_HOME);
		getActivity().registerReceiver(mBlueToothBroadCast,filter);
		super.onAttach(context);
	}

	public class BlueToothBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//得到蓝牙的服务连接
			isGATTConnected= intent.getBooleanExtra(Constants.EXTRAS_GATT_STATUS_FORM_HOME,false);
			LogUtil.i(TAG,"onReceive  isGATTConnectedFromHome:"+isGATTConnected);
//			if (isGATTConnected){
//				btnStart.setBackgroundResource(R.drawable.selector_connect);
//				btnStart.setTextColor(Color.WHITE);
//			}else{
//				btnStart.setBackgroundResource(R.drawable.shape_disconnect);
//				btnStart.setTextColor(getResources().getColor(R.color.item_divider));
//			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_home_medical, container, false);
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
				LogUtil.i(TAG, "onReceive: " + (data==null?"data为null":Arrays.toString(data)));
				if (data != null) {
					//TODO 接收数据处理
					boolean crcIsRight= ConvertUtils.CommonUtils.IsCRCRight(data);
					if (!crcIsRight){
						//误码纠正
						if (data.length > 13) {
							frontData = new byte[data.length - 13];
							System.arraycopy(data, 13, frontData, 0, frontData.length);
							LogUtil.i(TAG, "截取的frontData:" + Arrays.toString(frontData));
							data = Arrays.copyOfRange(data, 0, 13);
							if (!ConvertUtils.CommonUtils.IsCRCRight(data)) {
								return;
							}
							LogUtil.i(TAG, "截取的data:" + Arrays.toString(data));
						} else if (data.length < 13) {
							wholeData = new byte[13];
							if (frontData != null) {
								System.arraycopy(frontData, 0, wholeData, 0, frontData.length);
								System.arraycopy(data, 0, wholeData, frontData.length, data.length);
								data = wholeData;
								LogUtil.i(TAG, "拼接的data：" + Arrays.toString(data));
								if (!ConvertUtils.CommonUtils.IsCRCRight(data)) {
									return;
								}
								wholeData = null;
								frontData = null;
							} else {
								return;
							}
						}else {//data.length==11

						}
					}

					if (data[2]!=6&&mSendNewCmdFlag){

						mSendNewCmdFlag=false;
						retryConfigCount=0;

						stopTimer();
						Intent intent2 = new Intent(getActivity(), RunningActivity.class);
						intent2.putExtra(Constants.CURE_TYPE, Constants.CURE_MEDICAL);
						intent2.putExtra(Constants.ORIGINAL_TIME, mCauterizeTime + mMedicineTime);
						intent2.putExtra(Constants.CURRENT_TEMP, ConvertUtils.byte2unsignedInt(data[3]));
						intent2.putExtra(Constants.CURRENT_TIME,System.currentTimeMillis());
						intent2.putExtra(Constants.SETTING,settings);
						getActivity().startActivityForResult(intent2,0);
					}
				}
			}
		}
	};


	private Timer mTimer;
	private TimerTask mTimerTask;
	private int retryConfigCount;
	private boolean isConfigReceived=false;
	private void startConfigTimer(){
		if (mTimer==null){
			mTimer=new Timer();
		}
		if (mTimerTask==null){
			mTimerTask=new TimerTask() {
				@Override
				public void run() {
					if (!isConfigReceived){
						if (retryConfigCount>=6){
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ToastUtil.showToast(getActivity(),"断开连接",1000);
								}
							});
							stopTimer();
							Intent intent=new Intent();
							intent.putExtra(Constants.EXTRAS_GATT_STATUS,false);
							intent.setAction(Constants.RECEIVE_GATT_STATUS);
							getActivity().sendBroadcast(intent);
						}else{
							retryConfigCount++;
							isConfigReceived=false;
							sendMedicalCmd();
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
					Toasty.warning(getActivity(), "请连接设备", getResources().getInteger(R.integer.toasty_duration)).show();
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

				if (HomeFragment.getBluetoothLeService() == null) {
					return;
				}

				LogUtil.i(TAG,"HomeFragment.getBluetoothLeService()"+HomeFragment.getBluetoothLeService());

				HomeFragment.stopTimer();

				mSendNewCmdFlag=true;
				startConfigTimer();

			}
		});
	}

	private void sendMedicalCmd(){

		settings = CureSPUtil.setSettingData(0, mCauterizeGrade, mCauterizeTime
				, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime);
		LogUtil.i(TAG,"发送的配置命令:"+Arrays.toString(settings));
		HomeFragment.getBluetoothLeService().WriteValue(settings);
	}


	@Override
	public void onDestroyView() {
		stopTimer();
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(mGattUpdateReceiver);
		getActivity().unregisterReceiver(mBlueToothBroadCast);
		super.onDestroy();
	}
}
