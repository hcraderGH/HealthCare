package com.dafukeji.healthcare.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dafukeji.healthcare.service.BatteryService;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;

import es.dmoral.toasty.Toasty;
import me.itangqi.waveloadingview.WaveLoadingView;

public class HomeFragment extends Fragment implements View.OnClickListener{

	private WaveLoadingView mWaveLoadingView;
	private ImageView ivDeviceStatusLogo,ivTempLogo;
	private Button btnDeviceStatus;
	private TextView tvDeviceStatus,tvTempStatus,tvCurrentTemp;
	private int mRemindEle;
	private int mCurrentTemp;

	private BluetoothAdapter mBluetoothLEAdapter;
	private String mDeviceName;
	private String mDeviceAddress;
	private static BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
	private static boolean mConnected = false;

	private int selectedGrade =1;//档位
	private View mView;

	private BlueToothBroadCast mBlueToothBroadCast;

//	private boolean beginRemindBat =false;//当连接设备成功时，即提醒用户电量

	private ViewPager mViewPager;
	private CardPagerAdapter mCardAdapter;
	private ShadowTransformer mCardShadowTransformer;

	private MaterialDialog mMaterialDialog;
	private static String TAG="测试HomeFragment";
	@Override
	public void onAttach(Context context) {
		//注册接受蓝牙信息的广播
		mBlueToothBroadCast=new BlueToothBroadCast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constants.RECEIVE_BLUETOOTH_INFO);
		getActivity().registerReceiver(mBlueToothBroadCast,filter);
		super.onAttach(context);
	}


	class BlueToothBroadCast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//得到蓝牙的信息
			mDeviceAddress= intent.getStringExtra(Constants.EXTRAS_DEVICE_ADDRESS);
			LogUtil.i("HomeFragment", "onActivityResult:mDeviceAddress "+mDeviceAddress);
			mBluetoothLeService.connect(mDeviceAddress);
		}
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mView =inflater.inflate(R.layout.fragment_home,container,false);
		initViews();

		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			LogUtil.i(TAG,"onCreateView: "+R.string.ble_not_supported);
			getActivity().finish();
		}

		// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothLEAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothLEAdapter == null) {
			Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}
		// 若蓝牙没打开
		if (!mBluetoothLEAdapter.isEnabled()) {
			mBluetoothLEAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
		}
		Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
		LogUtil.i(TAG,"Try to bindService=" + getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE));
		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		return mView;
	}


	private void initViews() {

		//初始化ViewPagerCard
		mViewPager = (ViewPager)mView.findViewById(R.id.vp_cure);

		mCardAdapter=new CardPagerAdapter(getActivity(),getActivity().getSupportFragmentManager());
		mCardAdapter.addCardItem(new CardItem(R.string.cauterize));
		mCardAdapter.addCardItem(new CardItem(R.string.needle));
		mCardAdapter.addCardItem(new CardItem(R.string.knead));
		mCardAdapter.addCardItem(new CardItem(R.string.medical));

		mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
		mCardShadowTransformer.enableScaling(true);
		mViewPager.setAdapter(mCardAdapter);
		mViewPager.setPageTransformer(false, mCardShadowTransformer);
		mViewPager.setOffscreenPageLimit(4);

		mWaveLoadingView= (WaveLoadingView) mView.findViewById(R.id.wlv_reminder_electric_quantity);

		ivDeviceStatusLogo= (ImageView) mView.findViewById(R.id.iv_home_device_status_logo);
		ivTempLogo= (ImageView) mView.findViewById(R.id.iv_home_current_temp_status_logo);
		btnDeviceStatus = (Button) mView.findViewById(R.id.btn_home_device_status);
		tvDeviceStatus= (TextView) mView.findViewById(R.id.tv_home_device_status);
		tvTempStatus= (TextView) mView.findViewById(R.id.tv_home_current_temp_status);
		tvCurrentTemp= (TextView) mView.findViewById(R.id.tv_home_current_temp);

		btnDeviceStatus.setClickable(true);
		btnDeviceStatus.setOnClickListener(this);



		//TODO 当没有连接设备时的测试
//		final Handler handler = new Handler();
//		Runnable runnable = new Runnable() {
//			@Override
//			public void run() {
//				getActivity().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						mRemindEle=(int)(Math.random() * 100);//TODO 测试用
//						Intent intent=new Intent();
//						intent.putExtra(Constants.EXTRAS_BATTERY_ELECTRIC_QUANTITY,mRemindEle);
//						intent.setAction(Constants.BATTERY_ELECTRIC_QUANTITY);
//						getActivity().sendBroadcast(intent);
//						JudgeEleSetWare(mRemindEle);
//					}
//				});
//				handler.postDelayed(this, 1000);
//			}
//		};
//		handler.postDelayed(runnable,1000);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_home_device_status:
				if (mConnected){
					Logger.i("btn_home_device_status");
					setDisplayStatus(!mConnected);
					mConnected=false;//在此处强制设为false

					int type=Constants.DEVICE_POWER_OFF;
					int temp=0;
					int intensity=0;
					int time=0;
					int frequency=0;
					int crc=0xFA+0xFB+type+temp+intensity+time+frequency;
					byte[] setting=new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) type, (byte) temp
							, (byte) intensity, (byte) time, (byte) frequency, (byte)crc, (byte) 0xFE};
					Log.i(TAG, "onClick: off"+Arrays.toString(setting));
					mBluetoothLeService.WriteValue(setting);
				}
				break;
		}
	}


	/**
	 * 根据连接状态设置
	 */
	private void setDisplayStatus(boolean isConnected){
		if (isConnected){
			ivDeviceStatusLogo.setBackgroundResource(R.mipmap.ic_circle_green);
			ivTempLogo.setBackgroundResource(R.mipmap.ic_circle_yellow);

			btnDeviceStatus.setBackground(getResources().getDrawable(R.drawable.selector_power_off));

			tvDeviceStatus.setTextColor(getResources().getColor(R.color.connect_status));
			tvTempStatus.setTextColor(getResources().getColor(R.color.connect_status));
			tvCurrentTemp.setTextColor(Color.parseColor("#FFFFFF"));

			Toasty.success(getActivity(),"连接设备成功",Toast.LENGTH_SHORT).show();

		}else{
			ivDeviceStatusLogo.setBackgroundResource(R.mipmap.ic_circle_gray);
			ivTempLogo.setBackgroundResource(R.mipmap.ic_circle_gray);

			btnDeviceStatus.setBackgroundResource(R.mipmap.ic_device_disconnect);
			tvDeviceStatus.setTextColor(getResources().getColor(R.color.disconnect_status));
			tvTempStatus.setTextColor(getResources().getColor(R.color.disconnect_status));
			tvCurrentTemp.setTextColor(getResources().getColor(R.color.disconnect_status));

			mWaveLoadingView.setWaveColor(getResources().getColor(R.color.disconnect_status));
			mWaveLoadingView.cancelAnimation();
		}
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


	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
				LogUtil.i(TAG,"Only gatt, just wait");
//				ToastUtil.showToast(getActivity(), "连接成功，现在可以正常通信！",1000);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				if (mConnected){
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setDisplayStatus(!mConnected);
						}
					});
				}
				mConnected=false;
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){ //可以开始干活了
				mConnected = true;
				Intent gattIntent=new Intent();
				gattIntent.putExtra(Constants.EXTRAS_GATT_STATUS,mConnected);
				gattIntent.setAction(Constants.RECEIVE_GATT_STATUS);
				getActivity().sendBroadcast(gattIntent);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setDisplayStatus(mConnected);
					}
				});
				LogUtil.i(TAG,"In what we need");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
				LogUtil.i(TAG,"DATA");
				final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				if (data != null) {
					//TODO 接收数据处理
					LogUtil.i(TAG,"onReceive: "+ Arrays.toString(data));
//					mCurrentTemp=(int) data[3];//TODO int和byte之间的转换
//					mRemindEle=(int)data[8];

					//发送电量的广播
					Intent batIntent=new Intent();
					batIntent.putExtra(Constants.EXTRAS_BATTERY_ELECTRIC_QUANTITY,mRemindEle);
					getActivity().sendBroadcast(batIntent);

//					mRemindEle=(int)(Math.random() * 100);//TODO 测试用

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							JudgeEleSetWare((int) Math.ceil(((ConvertUtils.byte2unsignedInt(data[7])*100))));
							tvCurrentTemp.setText(ConvertUtils.byte2unsignedInt(data[3])+"℃");
						}
					});
				}
			}
		}
	};

	/**
	 * 根据电量来设置Ware的颜色
	 */
	private void JudgeEleSetWare(int ele) {

		mWaveLoadingView.startAnimation();//当断开接连时,停止了动画，所以要开始动画

		mWaveLoadingView.setProgressValue(ele);
		mWaveLoadingView.setCenterTitle(ele+"%");
		if (ele>=Constants.EXTRAS_BATTERY_WARN){
			mWaveLoadingView.setCenterTitleColor(Color.parseColor("#cc000000"));
			mWaveLoadingView.setWaveColor(getResources().getColor(R.color.battery_electric_quantity_normal));
		}else if (ele<Constants.EXTRAS_BATTERY_WARN&&ele>=Constants.EXTRAS_BATTERY_DANGER){
			mWaveLoadingView.setCenterTitleColor(getResources().getColor(R.color.battery_electric_quantity_warn));
			mWaveLoadingView.setWaveColor(getResources().getColor(R.color.battery_electric_quantity_warn));
		}else{
			Intent intent=new Intent(getActivity(), BatteryService.class);
			getActivity().startService(intent);
//			if (!beginRemindBat){
//				if (mMaterialDialog==null){//此处需要判断是否为空，求变量要为全局，否则，在对话框后，一直会new出新的对话框
//					mMaterialDialog=new MaterialDialog.Builder(getActivity())
//							.title("提示")
//							.positiveText("确定")
//							.onPositive(new MaterialDialog.SingleButtonCallback() {
//								            @Override
//								            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//									            beginRemindBat =true;
//								            }
//							            }
//							).content("设备电量过低，请及时充电")
//							.iconRes(R.mipmap.ic_warn_red)
//							.maxIconSize(64).build();
//					mMaterialDialog.show();
//				}
//			}
			mWaveLoadingView.setCenterTitleColor(getResources().getColor(R.color.battery_electric_quantity_danger));
			mWaveLoadingView.setWaveColor(getResources().getColor(R.color.battery_electric_quantity_danger));
		}
	}


	// Code to manage Service lifecycle.
	public final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				LogUtil.i(TAG,"Unable to initialize Bluetooth");
				getActivity().finish();
			}

			LogUtil.i(TAG,"mBluetoothLeService is okay");
			// Automatically connects to the device upon successful start-up initialization.
			//mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	public static boolean getBlueToothStatus(){
		return mConnected;
	}

	public static BluetoothLeService getBluetoothLeService(){
		return mBluetoothLeService;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unregisterReceiver(mBlueToothBroadCast);
		getActivity().unregisterReceiver(mGattUpdateReceiver);
		getActivity().unbindService(mServiceConnection);
		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}

		if (mBluetoothLEAdapter != null) {
			mBluetoothLEAdapter.disable();
		}
		LogUtil.i(TAG,"We are in destroy");
	}

	public void disConnect(){

		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}
	}
}
