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
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerPhysicalAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;

import java.util.Arrays;

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

	private static String TAG="测试PhysicalFragment";

	private boolean isGATTConnected;


	private boolean mSendNewCmdFlag;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_home_physical,container,false);
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
							int cauterizeGrade=0;
							int cauterizeTime=0;
							LogUtil.i(TAG, "发送的物理治疗的数据Settings:" + Arrays.toString(CureSPUtil.setSettingData(mStimulate, cauterizeGrade, cauterizeTime
									, mKneadType, mKneadGrade, mKneadFrequency, mKneadTime)));
							HomeFragment.getBluetoothLeService().WriteValue(CureSPUtil.setSettingData(mStimulate, cauterizeGrade, cauterizeTime
									, mKneadType, mKneadGrade, mKneadFrequency, mKneadTime));
						} else {
							mSendNewCmdFlag=false;

							Frame.preFrameId=Frame.curFrameId;
							LogUtil.i(TAG,"已经进入了方法");
							Intent intent2 = new Intent(getActivity(), RunningActivity.class);
							intent2.putExtra(Constants.CURE_TYPE,Constants.CURE_PHYSICAL);
							intent2.putExtra(Constants.ORIGINAL_TIME, mKneadTime);
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
		mViewPager = (ViewPager)mView.findViewById(R.id.vp_cure);

		mCardAdapter=new CardPagerPhysicalAdapter(getActivity(),getActivity().getSupportFragmentManager());
		mCardAdapter.addCardItem(new CardItem(R.string.needle));
		mCardAdapter.addCardItem(new CardItem(R.string.needle));

		mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
		mCardShadowTransformer.enableScaling(true);
		mViewPager.setAdapter(mCardAdapter);
		mViewPager.setPageTransformer(false, mCardShadowTransformer);
		mViewPager.setOffscreenPageLimit(2);

		btnStart= (Button) mView.findViewById(R.id.btn_physical_start);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {


				if (!isGATTConnected){
					Toasty.warning(getActivity(),"请连接设备", Toast.LENGTH_SHORT).show();
					return;
				}
				if (CureSPUtil.isSaved(Constants.SP_PHYSICAL_STIMULATE,getActivity())){
					mStimulate=CureSPUtil.getSP(Constants.SP_PHYSICAL_STIMULATE,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TYPE,getActivity())){
					mKneadType=CureSPUtil.getSP(Constants.SP_KNEAD_TYPE,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_GRADE,getActivity())){
					mKneadGrade=CureSPUtil.getSP(Constants.SP_KNEAD_GRADE,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_FREQUENCY,getActivity())){
					mKneadFrequency=CureSPUtil.getSP(Constants.SP_KNEAD_FREQUENCY,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TIME_GRADE,getActivity())){
					mKneadTime=CureSPUtil.getKneadTimeByPosition(CureSPUtil.getSP(Constants.SP_KNEAD_TIME_GRADE,getActivity()));
				}



//				if (HomeFragment.getBluetoothLeService()==null){
//					return;
//				}

				mSendNewCmdFlag=true;

				int cauterizeGrade=0;
				int cauterizeTime=0;
				LogUtil.i(TAG, "发送的物理治疗的数据Settings:" + Arrays.toString(CureSPUtil.setSettingData(mStimulate, cauterizeGrade, cauterizeTime
						, mKneadType, mKneadGrade, mKneadFrequency, mKneadTime)));
				HomeFragment.getBluetoothLeService().WriteValue(CureSPUtil.setSettingData(mStimulate, cauterizeGrade, cauterizeTime
						, mKneadType, mKneadGrade, mKneadFrequency, mKneadTime));
			}
		});
	}

	@Override
	public void onDestroy() {
		LogUtil.i(TAG,"onDestroy()");
//		getActivity().unregisterReceiver(mBlueToothBroadCast);
		super.onDestroy();
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
