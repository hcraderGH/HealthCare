package com.dafukeji.healthcare.fragment;

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
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.ui.RunningActivity;
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
	private BlueToothBroadCast2 mBlueToothBroadCast;

	@Override
	public void onAttach(Context context) {
//		//注册接受蓝牙信息的广播
//		mBlueToothBroadCast=new BlueToothBroadCast();
//		IntentFilter filter=new IntentFilter();
//		filter.addAction(Constants.RECEIVE_GATT_STATUS);
//		getActivity().registerReceiver(mBlueToothBroadCast,filter);
//		super.onAttach(context);

		LogUtil.i(TAG,"onAttach()");
	}

	class BlueToothBroadCast2 extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//得到蓝牙的服务连接
			isGATTConnected= intent.getBooleanExtra(Constants.EXTRAS_GATT_STATUS,false);
			LogUtil.i(TAG,"onReceive  isGATTConnected:"+isGATTConnected);
		}
	}


	@Override
	public void onResume() {
		//注册接受蓝牙信息的广播
		mBlueToothBroadCast=new BlueToothBroadCast2();
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constants.RECEIVE_GATT_STATUS);
		getActivity().registerReceiver(mBlueToothBroadCast,filter);
		LogUtil.i(TAG,"onResume()");

		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_home_physical,container,false);
		initViews();

		return mView;
	}

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


				int cauterizeGrade=0;
				int cauterizeTime=0;

				LogUtil.i(TAG, "发送的物理治疗的数据Settings:" + Arrays.toString(CureSPUtil.setSettingData(mStimulate, cauterizeGrade, cauterizeTime
						, mKneadType, mKneadGrade, mKneadFrequency, mKneadTime)));

				if (HomeFragment.getBluetoothLeService()==null){
					return;
				}

				LogUtil.i(TAG,"HomeFragment.getBluetoothLeService()"+HomeFragment.getBluetoothLeService());
				HomeFragment.getBluetoothLeService().WriteValue(CureSPUtil.setSettingData(mStimulate, cauterizeGrade, cauterizeTime
						, mKneadType, mKneadGrade, mKneadFrequency, mKneadTime));

				Intent intent = new Intent(getActivity(), RunningActivity.class);
				intent.putExtra(Constants.CURE_TYPE,Constants.CURE_PHYSICAL);
				intent.putExtra(Constants.ORIGINAL_TIME, mKneadTime);
				getActivity().startActivity(intent);
			}
		});
	}

	@Override
	public void onDestroy() {
		LogUtil.i(TAG,"onDestroy()");
		getActivity().unregisterReceiver(mBlueToothBroadCast);
		super.onDestroy();
	}
}
