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
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dafukeji.healthcare.BluetoothLeService;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.ToastUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;

import java.util.ArrayList;

public class HomeFragment extends Fragment{


	private Button btnCauterizeGrade,btnNeedleGrade,btnMedicalTemp;
	private Button btnCauterizeTime,btnNeedleTime,btnMedicalTime;
	private Button btnCauterizeStart,btnNeedleStart,btnMedicalStart;

	private int[] sustainTime=new int[2];
	private long originalTime;
	private String selectGrade;

	private String TAG ="测试HomeFragment";


	private BluetoothAdapter mBluetoothLEAdapter;
	private String mDeviceName;
	private String mDeviceAddress;
	private static BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
	private static boolean mConnected = false;

	private int selectedGrade =1;//档位
	private View mView;

	private Dialog.Builder mBuilder;
	private DialogFragment mFragment;

	private BlueToothBroadCast mBlueToothBroadCast;

	private ViewPager mViewPager;
	private CardPagerAdapter mCardAdapter;
	private ShadowTransformer mCardShadowTransformer;
	private View mInflaterView;
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
			Log.i("HomeFragment", "onActivityResult:mDeviceAddress "+mDeviceAddress);
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
			Log.i(TAG, "onCreateView: "+R.string.ble_not_supported);
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
		Log.d(TAG, "Try to bindService=" + getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE));
		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		return mView;
	}


	private void initViews() {

		//初始化ViewPagerCard
		mViewPager = (ViewPager)mView.findViewById(R.id.vp_cure);

		mCardAdapter=new CardPagerAdapter(getActivity(),getActivity().getSupportFragmentManager());
		mCardAdapter.addCardItem(new CardItem(R.string.cauterize));
		mCardAdapter.addCardItem(new CardItem(R.string.needle));
		mCardAdapter.addCardItem(new CardItem(R.string.medical));

		mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
		mCardShadowTransformer.enableScaling(true);
		mViewPager.setAdapter(mCardAdapter);
		mViewPager.setPageTransformer(false, mCardShadowTransformer);
		mViewPager.setOffscreenPageLimit(3);
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
				Log.e(TAG, "Only gatt, just wait");
				ToastUtil.showToast(getActivity(), "连接成功，现在可以正常通信！",1000);
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				mConnected = false;
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){ //可以开始干活了
				mConnected = true;
				Log.e(TAG, "In what we need");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
				Log.e(TAG, "DATA");
				byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				if (data != null) {
					//TODO 接收数据处理
					Log.i(TAG, "onReceive: "+data.toString());
//					tvCurrentTemp.setText(ConvertUtils.bytes2HexString(data)+"℃");//TODO 注意此处获取的数据
				}
			}
		}
	};


	// Code to manage Service lifecycle.
	public final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				getActivity().finish();
			}

			Log.e(TAG, "mBluetoothLeService is okay");
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
		Log.d(TAG, "We are in destroy");
	}

	public void unBindService(){
		getActivity().unregisterReceiver(mGattUpdateReceiver);
		getActivity().unbindService(mServiceConnection);
		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}

		if (mBluetoothLEAdapter != null) {
			mBluetoothLEAdapter.disable();
		}
	}
}
