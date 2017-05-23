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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dafukeji.healthcare.BluetoothLeService;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.ui.RunningActivity;
import com.dafukeji.healthcare.util.ToastUtil;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.TimePickerDialog;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener{


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
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
	private boolean mConnected = false;

	private int selectedGrade =1;//档位
	private View view;

	private Dialog.Builder mBuilder;
	private DialogFragment mFragment;

	private BlueToothBroadCast mBlueToothBroadCast;

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
		view=inflater.inflate(R.layout.fragment_home,container,false);
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
		return view;
	}


	private void initViews() {
		btnCauterizeGrade= (Button) view.findViewById(R.id.btn_cauterize_grade);
		btnNeedleGrade= (Button) view.findViewById(R.id.btn_needle_grade);
		btnMedicalTemp= (Button) view.findViewById(R.id.btn_medical_temp);

		btnCauterizeTime= (Button) view.findViewById(R.id.btn_cauterize_time);
		btnNeedleTime= (Button) view.findViewById(R.id.btn_needle_time);
		btnMedicalTime= (Button) view.findViewById(R.id.btn_medical_time);

		btnCauterizeStart= (Button) view.findViewById(R.id.btn_cauterize_start);
		btnNeedleStart= (Button) view.findViewById(R.id.btn_needle_start);
		btnMedicalStart= (Button) view.findViewById(R.id.btn_medical_start);


		btnCauterizeGrade.setOnClickListener(this);
		btnNeedleGrade.setOnClickListener(this);
		btnMedicalTemp.setOnClickListener(this);

		btnCauterizeTime.setOnClickListener(this);
		btnNeedleTime.setOnClickListener(this);
		btnMedicalTime.setOnClickListener(this);

		btnCauterizeStart.setOnClickListener(this);
		btnNeedleStart.setOnClickListener(this);
		btnMedicalStart.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_cauterize_grade:
				getGrade(new String[]{"一档","二档","三档","四档","五档"},btnCauterizeGrade);
				break;
			case R.id.btn_needle_grade:
				getGrade(new String[]{"一档","二档","三档","四档","五档"},btnNeedleGrade);
				break;
			case R.id.btn_medical_temp:
				getGrade(new String[]{"40℃", "42℃", "44℃", "46℃", "48℃", "50℃"},btnMedicalTemp);
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

				if (!mConnected){
					ToastUtil.showToast(getActivity(),"请连接设备",1000);
					return;
				}

				if (btnCauterizeTime.getText().toString().equals("0分钟")){
					ToastUtil.showToast(getActivity(),"请设定持续时间",1000);
					return;
				}else{
					byte[] settings=new byte[]{0x31,0x32,0x33};
					mBluetoothLeService.WriteValue(settings);
					Intent intent=new Intent(getActivity(),RunningActivity.class);
					Log.i(TAG, "onClick: originalTime"+originalTime);
					intent.putExtra(Constants.CURE_TYPE,Constants.CURE_CAUTERIZE);
					intent.putExtra(Constants.ORIGINAL_TIME, originalTime);
					startActivity(intent);
				}

				break;
			case R.id.btn_needle_start:

				break;
			case R.id.btn_medical_start:

				break;
		}
	}

	private String displayTime(int[] time){
		String displayTime;
		int hour =time[0];
		int minute =time[1];
		if (hour == 0) {
			displayTime = minute + "分钟";
		} else if (minute < 10) {
			displayTime = hour + "小时" +"0"+minute+ "分钟";
		} else {
			displayTime = hour + "小时" + minute + "分钟";
		}
		return displayTime;
	}

	private void getSustainTime(final Button btnTime) {
		if (mBuilder != null) {
			return;
		}
				mBuilder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light, 24, 00) {
					@Override
					public void onPositiveActionClicked(DialogFragment fragment) {
						TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
						int hour = dialog.getHour();
						int minute = dialog.getMinute();
						sustainTime[0]=hour;
						sustainTime[1]=minute;
						Log.i(TAG, "onPositiveActionClicked: sustainTime"+sustainTime[0]+"   "+sustainTime[1]);
						originalTime =(sustainTime[0]*60+sustainTime[1])*60*1000;
						Log.i(TAG, "onPositiveActionClicked: originalTime"+ originalTime);
						btnTime.setText(displayTime(sustainTime));
						ToastUtil.showToast(getActivity(), "您选择的持续时间是" +hour+"小时"+minute+"分钟", 1500);
						mBuilder=null;
						mFragment=null;
						super.onPositiveActionClicked(fragment);//此代码必须放在下面
					}

					@Override
					public void onNegativeActionClicked(DialogFragment fragment) {
						mBuilder=null;
						mFragment=null;
						super.onNegativeActionClicked(fragment);
					}
				};
				mBuilder.positiveAction("确定")
						.negativeAction("取消");
		mFragment = DialogFragment.newInstance(mBuilder);
		mFragment.show(getActivity().getSupportFragmentManager(), null);
	}

	private void getGrade(String[] grade, final Button btn) {
		if (mBuilder != null) {
			return;
		}
		mBuilder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light) {
			@Override
			public void onPositiveActionClicked(DialogFragment fragment) {
				ToastUtil.showToast(getActivity(), "您选择了" + getSelectedValue(), 1500);
				selectGrade = (String) getSelectedValue();
				btn.setText(selectGrade);
				mBuilder=null;
				mFragment=null;
				super.onPositiveActionClicked(fragment);
			}

			@Override
			public void onNegativeActionClicked(DialogFragment fragment) {
				mBuilder=null;
				mFragment=null;
				super.onNegativeActionClicked(fragment);
			}
		};
		((SimpleDialog.Builder) mBuilder).items(grade, 0)
				.title("加热温度选择")
				.positiveAction("确定")
				.negativeAction("取消");
		mFragment = DialogFragment.newInstance(mBuilder);
		mFragment.show(getActivity().getSupportFragmentManager(), null);
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


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unregisterReceiver(mBlueToothBroadCast);
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
		Log.d(TAG, "We are in destroy");
	}
}
