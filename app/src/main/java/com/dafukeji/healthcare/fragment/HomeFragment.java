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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.service.BatteryService;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.LogUtil;

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


	private MedicalFragment mMedicalFragment;
	private PhysicalFragment mPhysicalFragment;
	private FragmentManager mManager;
	private String[] fragmentNames;


	private RadioButton rbMedical;
	private RadioButton rbPhysical;

	private BluetoothAdapter mBluetoothLEAdapter;
	private String mDeviceAddress;
	private static BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
	private static boolean mConnected = false;

	private int selectedGrade =1;//档位
	private View mView;

	private BlueToothBroadCast mBlueToothBroadCast;

//	private boolean beginRemindBat =false;//当连接设备成功时，即提醒用户电量

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
			LogUtil.i(TAG, "onActivityResult:mDeviceAddress "+mDeviceAddress);
			mBluetoothLeService.connect(mDeviceAddress);
		}
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mView =inflater.inflate(R.layout.fragment_home,container,false);
		initViews();

		mManager=getActivity().getSupportFragmentManager();
		fragmentNames=getResources().getStringArray(R.array.array_frag_name);

		setTabSelection(0);

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

//		getActivity().startService(new Intent(getActivity(), ScanService.class));//开始时候开启扫描服务 TODO

		return mView;
	}

	private void initViews() {

		mWaveLoadingView= (WaveLoadingView) mView.findViewById(R.id.wlv_reminder_electric_quantity);

		ivDeviceStatusLogo= (ImageView) mView.findViewById(R.id.iv_home_device_status_logo);
		ivTempLogo= (ImageView) mView.findViewById(R.id.iv_home_current_temp_status_logo);
		btnDeviceStatus = (Button) mView.findViewById(R.id.btn_home_device_status);
		tvDeviceStatus= (TextView) mView.findViewById(R.id.tv_home_device_status);
		tvTempStatus= (TextView) mView.findViewById(R.id.tv_home_current_temp_status);
		tvCurrentTemp= (TextView) mView.findViewById(R.id.tv_home_current_temp);

		btnDeviceStatus.setClickable(true);
		btnDeviceStatus.setOnClickListener(this);


		rbMedical= (RadioButton) mView.findViewById(R.id.rb_medical);
		rbPhysical= (RadioButton) mView.findViewById(R.id.rb_physical);
		rbMedical.setOnClickListener(this);
		rbPhysical.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_home_device_status:
				if (mConnected){
					setDisplayStatus(!mConnected);
					mConnected=false;//在此处强制设为false

					int stimulate=3;//关机标志
					int stimulateGrade=0;
					int stimulateFrequency=0;
					int cauterizeGrade=0;
					int cauterizeTime=0;
					int needleType=0;
					int needleGrade=0;
					int needleFrequency=0;
					int medicineTime=0;
					int crc=stimulate+stimulateGrade+stimulateFrequency+cauterizeGrade+cauterizeTime
							+needleType+needleGrade+needleFrequency+medicineTime;

					byte[] setting=new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) stimulate, (byte) stimulateGrade
							, (byte) stimulateFrequency, (byte) cauterizeGrade, (byte) cauterizeTime, (byte)needleType, (byte) needleGrade
					,(byte)needleFrequency,(byte)medicineTime,(byte)crc};
					Log.i(TAG, "onClick: off"+Arrays.toString(setting));
					mBluetoothLeService.WriteValue(setting);
				}
				break;
			case R.id.rb_medical:
				setTabSelection(0);
				break;
			case R.id.rb_physical:
				setTabSelection(1);
				break;
		}
	}


	/**
	 * 根据传入的index参数来设置选中的tab页。
	 * @param index
	 * 每个tab页对应的下标。
	 */
	private void setTabSelection(int index){
		// 开启一个Fragment事务
		FragmentTransaction transaction=mManager.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments(transaction);
		switch (index){
//			case 0:
//				if (mMedicalFragment==null){
//					mMedicalFragment=new MedicalFragment();
//					transaction.add(R.id.fl_cure_content,mMedicalFragment);
//				}else{
//					transaction.show(mMedicalFragment);
//				}
//				break;
//			case 1:
//				if (mPhysicalFragment==null){
//					mPhysicalFragment=new PhysicalFragment();
//					transaction.add(R.id.fl_cure_content,mPhysicalFragment);
//				}else{
//					transaction.show(mPhysicalFragment);
//				}
//				break;

			case 0:
				if (mMedicalFragment==null){
					mMedicalFragment=new MedicalFragment();
					transaction.add(R.id.fl_cure_content,mMedicalFragment,fragmentNames[0]);
					transaction.addToBackStack(fragmentNames[0]);
				}else{
					transaction.show(mMedicalFragment);
				}

				break;
			case 1:
				if (mPhysicalFragment==null){
					mPhysicalFragment=new PhysicalFragment();
					transaction.add(R.id.fl_cure_content,mPhysicalFragment,fragmentNames[1]);
					transaction.addToBackStack(fragmentNames[0]);
				}else{
					transaction.show(mPhysicalFragment);
				}
				break;

		}
		transaction.commit();
	}

	/**
	 * 将所有的Fragment都置为隐藏状态。
	 * @param transaction
	 * 用于对Fragment执行操作的事务
	 */
	private void hideFragments(FragmentTransaction transaction) {
		if (mMedicalFragment != null) {
			transaction.hide(mMedicalFragment);
		}
		if (mPhysicalFragment != null) {
			transaction.hide(mPhysicalFragment);
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
				LogUtil.i(TAG,"mGattUpdateReceiver断开了连接");
//				if (mConnected){
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
//							setDisplayStatus(!mConnected);
							setDisplayStatus(false);
						}
					});
//				}
				mConnected=false;
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){ //可以开始干活了

				mConnected = true;
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setDisplayStatus(mConnected);
					}
				});
				LogUtil.i(TAG,"In what we need");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据

				final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				if (data != null) {

					Intent gattIntent=new Intent();
					gattIntent.putExtra(Constants.EXTRAS_GATT_STATUS,mConnected);
					gattIntent.setAction(Constants.RECEIVE_GATT_STATUS);
					getActivity().sendBroadcast(gattIntent);

					//TODO 接收数据处理
					LogUtil.i(TAG,"onReceive: "+ Arrays.toString(data));

					//当校验码前面的数据相加不等于校验码时表示数据错误
//					if (!(data[2] + data[3] + data[4] + data[5] + data[6]+data[7]== data[8])) {//TODO 测试此处有错误
//						return;
//					}

					mCurrentTemp=ConvertUtils.byte2unsignedInt(data[3]);
					mRemindEle=ConvertUtils.byte2unsignedInt(data[7]);

					//发送电量的广播
					Intent batIntent=new Intent();
					batIntent.putExtra(Constants.EXTRAS_BATTERY_ELECTRIC_QUANTITY,mRemindEle);
					getActivity().sendBroadcast(batIntent);

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							JudgeEleSetWare((int) Math.ceil(mRemindEle*10/4.3));//TODO 电量公式
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
//		getActivity().unregisterReceiver(mBlueToothBroadCast);
//		getActivity().unregisterReceiver(mGattUpdateReceiver);
//		getActivity().unbindService(mServiceConnection);
//		if (mBluetoothLeService != null) {
//			mBluetoothLeService.close();
//			mBluetoothLeService = null;
//		}
//
//		if (mBluetoothLEAdapter != null) {
//			mBluetoothLEAdapter.disable();
//		}
		LogUtil.i(TAG,"We are in destroy");
	}

	public void disConnect(){

//		if (mConnected){
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					setDisplayStatus(!mConnected);
					setDisplayStatus(false);
				}
			});
			mConnected=false;
//		}
		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}

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
	}

	public void disableBlueTooth(){
		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}
	}
}
