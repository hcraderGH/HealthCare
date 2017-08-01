package com.dafukeji.healthcare.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.os.Message;
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
import com.dafukeji.healthcare.bean.Frame;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.service.BatteryService;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.CommonUtils;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import me.itangqi.waveloadingview.WaveLoadingView;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements View.OnClickListener {

	private WaveLoadingView mWaveLoadingView;
	private ImageView ivDeviceStatusLogo, ivTempLogo;
	private Button btnDeviceStatus;
	private TextView tvDeviceStatus, tvTempStatus, tvCurrentTemp;
	private int mRemindEle;

	private int mCurrentTemp;
	private boolean mSendNewCmdFlag;

	private MedicalFragment mMedicalFragment;
	private PhysicalFragment mPhysicalFragment;
	private FragmentManager mManager;
	private String[] fragmentNames;

	private long cmdOffTime;
	private long realOffTime;

	private RadioButton rbMedical;
	private RadioButton rbPhysical;

	private BluetoothAdapter mBluetoothLEAdapter;
	private String mDeviceAddress;
	private static BluetoothLeService mBluetoothLeService;
	private static boolean mConnected = false;

	private View mView;

	private BlueToothBroadCast mBlueToothBroadCast;

	private int mReceivedDataCount;
	private List<Integer> mEleList=new ArrayList<>();
	private int mEleSum;

//	private boolean beginRemindBat =false;//当连接设备成功时，即提醒用户电量

	private static String TAG = "测试HomeFragment";

	private byte[] frontData;
	private byte[] wholeData;

	@Override
	public void onAttach(Context context) {
		//注册接受蓝牙信息的广播
		mBlueToothBroadCast = new BlueToothBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.RECEIVE_GATT_STATUS);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(Constants.RECEIVE_BLUETOOTH_INFO);
		getActivity().registerReceiver(mBlueToothBroadCast, filter);

		super.onAttach(context);
	}

	public class BlueToothBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, final Intent intent) {
			//得到蓝牙的信息
//			mDeviceAddress = intent.getStringExtra(Constants.EXTRAS_DEVICE_ADDRESS);

			switch (intent.getAction()){
				case BluetoothDevice.ACTION_ACL_CONNECTED:

					LogUtil.i(TAG,"设备连接上了");
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setDisplayStatus(true);
						}
					});
					mConnected=true;

					break;
				case BluetoothDevice.ACTION_ACL_DISCONNECTED:
					LogUtil.i(TAG,"设备断开了");
					LogUtil.i(TAG,"设备断开所需的时间："+(System.currentTimeMillis()-cmdOffTime));
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setDisplayStatus(false);
						}
					});

					mSendNewCmdFlag=false;
					mConnected=false;
					break;
				case Constants.RECEIVE_GATT_STATUS:
					LogUtil.i(TAG, "mBluetoothLeService=" + mBluetoothLeService);
					boolean isGATTConnected = intent.getBooleanExtra(Constants.EXTRAS_GATT_STATUS, false);
					LogUtil.i(TAG, "连接状态isGATTConnected=" + isGATTConnected);

					if (isGATTConnected) {

						startSensorTimer();//当连接时，则开始发送传感命令

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setDisplayStatus(true);
							}
						});
					} else {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setDisplayStatus(false);
							}
						});
					}
					break;

				case Constants.RECEIVE_BLUETOOTH_INFO:
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							JudgeEleSetWare(intent.getIntExtra(Constants.RECEIVE_CURRENT_ELE,0));
							tvCurrentTemp.setText(intent.getIntExtra(Constants.RECEIVE_CURRENT_TEMP,0)+"℃");
						}
					});
					break;
			}
		}
	}

	// Code to manage Service lifecycle.
	public final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				LogUtil.i(TAG, "Unable to initialize Bluetooth");
				getActivity().finish();
			}

			LogUtil.i(TAG, "mBluetoothLeService is okay");
			// Automatically connects to the device upon successful start-up initialization.
			//mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_home, container, false);
		initViews();

		mManager = getActivity().getSupportFragmentManager();
		fragmentNames = getResources().getStringArray(R.array.array_frag_name);

		setTabSelection(0);

		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			LogUtil.i(TAG, "onCreateView: " + R.string.ble_not_supported);
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
		LogUtil.i(TAG, "Try to bindService=" + getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE));
		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		return mView;
	}

	private void initViews() {

		mWaveLoadingView = (WaveLoadingView) mView.findViewById(R.id.wlv_reminder_electric_quantity);

		ivDeviceStatusLogo = (ImageView) mView.findViewById(R.id.iv_home_device_status_logo);
		ivTempLogo = (ImageView) mView.findViewById(R.id.iv_home_current_temp_status_logo);
		btnDeviceStatus = (Button) mView.findViewById(R.id.btn_home_device_status);
		tvDeviceStatus = (TextView) mView.findViewById(R.id.tv_home_device_status);
		tvTempStatus = (TextView) mView.findViewById(R.id.tv_home_current_temp_status);
		tvCurrentTemp = (TextView) mView.findViewById(R.id.tv_home_current_temp);

		btnDeviceStatus.setClickable(true);
		btnDeviceStatus.setOnClickListener(this);


		rbMedical = (RadioButton) mView.findViewById(R.id.rb_medical);
		rbPhysical = (RadioButton) mView.findViewById(R.id.rb_physical);
		rbMedical.setOnClickListener(this);
		rbPhysical.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_home_device_status:
				if (mConnected) {

					cmdOffTime=System.currentTimeMillis();

					mSendNewCmdFlag = true;
					setDisplayStatus(false);//先直接变灰
					stopTimer();
					startConfigTimer();
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


	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0://接受到的电量

					JudgeEleSetWare(CommonUtils.eleFormula(msg.arg1));//TODO 电量的计算公式

					tvCurrentTemp.setText(msg.arg2+"℃");//温度的显示
					break;
			}
		}
	};


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.i(TAG,"关闭Running后重新发送传感命令");
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode==RESULT_OK){
			startSensorTimer();
		}
	}

	//发送传感命令
	private static Timer mTimer;
	private static TimerTask mTimerTask;
	private int retrySensorCount;
	private boolean isSensorReceived =false;
	private long mCurSendSensorTime;
	private void startSensorTimer(){
		if (mTimer==null){
			mTimer=new Timer();
		}
		if (mTimerTask==null){
			mTimerTask=new TimerTask() {
				@Override
				public void run() {
					if (!isSensorReceived){
						if (retrySensorCount>=6) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ToastUtil.showToast(getActivity(),"断开连接",1000);
									setDisplayStatus(false);
								}
							});

							stopTimer();
						}else{
							retrySensorCount++;
							mCurSendSensorTime =System.currentTimeMillis();
							isSensorReceived=false;
							byte[] sensorCmd = new byte[]{(byte) 0xFA, (byte) 0xFB, 6, 0, 0, 0, 0, 0, 0, 0, 0, 6};
							mBluetoothLeService.WriteValue(sensorCmd);
						}
					}else{
						retrySensorCount=0;
						mCurSendSensorTime =System.currentTimeMillis();
						isSensorReceived=false;
						LogUtil.i(TAG,"HomeFragment发送的传感命令");
						byte[] sensorCmd = new byte[]{(byte) 0xFA, (byte) 0xFB, 6, 0, 0, 0, 0, 0, 0, 0, 0, 6};
						mBluetoothLeService.WriteValue(sensorCmd);
					}

				}
			};
		}
		if (mTimer!=null&&mTimerTask!=null){
			mTimer.schedule(mTimerTask,0,400);
		}
	}


	private int retryConfigCount;
	private boolean isConfigReceived;
	private void startConfigTimer(){
		if (mTimer==null){
			mTimer=new Timer();
		}
		if (mTimerTask==null){
			mTimerTask=new TimerTask() {
				@Override
				public void run() {
					if (isSensorReceived){
						if (!isConfigReceived){
							if (retryConfigCount>=6){
								stopTimer();
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										setDisplayStatus(false);
										ToastUtil.showToast(getActivity(),"断开连接",1000);
									}
								});
							}else{
								retryConfigCount++;
								isConfigReceived=false;
								sendPowerOffCmd();
							}
						}
					}else{
						try {
							Thread.sleep(400-(System.currentTimeMillis()- mCurSendSensorTime));
							isConfigReceived=false;
							sendPowerOffCmd();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
		}
		if (mTimer!=null&&mTimerTask!=null){
			mTimer.schedule(mTimerTask,0,400);
		}

	}

	public static void stopTimer(){
		if (mTimer!=null){
			mTimer.cancel();
			mTimer=null;
		}
		if (mTimerTask!=null){
			mTimerTask.cancel();
			mTimerTask=null;
		}
	}

	/**
	 * 发送关机命令
	 */
	private void sendPowerOffCmd() {
		int stimulate = 3;//关机标志  TODO 关机命令有3改为5
		int stimulateGrade = 0;
		int stimulateFrequency = 0;
		int cauterizeGrade = 0;
		int cauterizeTime = 0;
		int needleType = 0;
		int needleGrade = 0;
		int needleFrequency = 0;
		int medicineTime = 0;
		int crc = stimulate + stimulateGrade + stimulateFrequency + cauterizeGrade + cauterizeTime
				+ needleType + needleGrade + needleFrequency + medicineTime;

		final byte[] setting = new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) stimulate, (byte) stimulateGrade
				, (byte) stimulateFrequency, (byte) cauterizeGrade, (byte) cauterizeTime, (byte) needleType, (byte) needleGrade
				, (byte) needleFrequency, (byte) medicineTime, (byte) crc};
		Log.i(TAG, "onClick: off" + Arrays.toString(setting));
		mBluetoothLeService.WriteValue(setting);
	}


	/**
	 * 根据传入的index参数来设置选中的tab页。
	 *
	 * @param index 每个tab页对应的下标。
	 */
	private void setTabSelection(int index) {
		// 开启一个Fragment事务
		FragmentTransaction transaction = mManager.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments(transaction);
		switch (index) {
			case 0:
				if (mMedicalFragment == null) {
					mMedicalFragment = new MedicalFragment();
					transaction.add(R.id.fl_cure_content, mMedicalFragment, fragmentNames[0]);
					transaction.addToBackStack(fragmentNames[0]);
				} else {
					transaction.show(mMedicalFragment);
				}

				break;
			case 1:
				if (mPhysicalFragment == null) {
					mPhysicalFragment = new PhysicalFragment();
					transaction.add(R.id.fl_cure_content, mPhysicalFragment, fragmentNames[1]);
					transaction.addToBackStack(fragmentNames[0]);
				} else {
					transaction.show(mPhysicalFragment);
				}
				break;

		}
		transaction.commit();
	}

	/**
	 * 将所有的Fragment都置为隐藏状态。
	 *
	 * @param transaction 用于对Fragment执行操作的事务
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
	private void setDisplayStatus(boolean isConnected) {
		if (isConnected) {
			ivDeviceStatusLogo.setBackgroundResource(R.mipmap.ic_circle_green);
			ivTempLogo.setBackgroundResource(R.mipmap.ic_circle_yellow);

			btnDeviceStatus.setBackground(getResources().getDrawable(R.drawable.selector_power_off));

			tvDeviceStatus.setTextColor(getResources().getColor(R.color.connect_status));
			tvTempStatus.setTextColor(getResources().getColor(R.color.connect_status));
			tvCurrentTemp.setTextColor(Color.parseColor("#FFFFFF"));

//			Toasty.success(getActivity(), "连接设备成功", Toast.LENGTH_SHORT).show();
		} else {

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
				LogUtil.i(TAG, "Only gatt, just wait");
//				ToastUtil.showToast(getActivity(), "连接成功，现在可以正常通信！",1000);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				LogUtil.i(TAG, "mGattUpdateReceiver断开了连接");

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setDisplayStatus(false);
					}
				});

				mSendNewCmdFlag = false;
				mConnected = false;

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //可以开始干活了

				LogUtil.i(TAG, "In what we need");

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据

				mConnected = true;

				byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				LogUtil.i(TAG, "onReceive: " + (data == null ? "data为null" : Arrays.toString(data)));
				if (data != null) {
					//TODO 接收数据处理
					//当校验码前面的数据相加不等于校验码时表示数据错误
					boolean crcIsRight = CommonUtils.IsCRCRight(data);
					if (!crcIsRight) {
						LogUtil.i(TAG, "数据校验出现错误");
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

					//当接受下位机的强刺激位为4时表示下位机收到了关机命令
					if (data[2]== 4) {
						LogUtil.i(TAG,"发送关机命令成功");
						Intent gattIntent = new Intent();
						gattIntent.putExtra(Constants.EXTRAS_GATT_STATUS_FORM_HOME, false);
						gattIntent.setAction(Constants.RECEIVE_GATT_STATUS_FROM_HOME);
						getActivity().sendBroadcast(gattIntent);
						setDisplayStatus(false);
						stopTimer();
					}

					if (data[2]==6) {
						isSensorReceived=true;
						Intent gattIntent = new Intent();
						gattIntent.putExtra(Constants.EXTRAS_GATT_STATUS_FORM_HOME, true);
						gattIntent.setAction(Constants.RECEIVE_GATT_STATUS_FROM_HOME);
						getActivity().sendBroadcast(gattIntent);

						mCurrentTemp = ConvertUtils.byte2unsignedInt(data[3]);
						mRemindEle = ConvertUtils.byte2unsignedInt(data[7]);


						mReceivedDataCount++;
						mEleList.add(mRemindEle);
						if (mReceivedDataCount % 10 == 0 || mReceivedDataCount == 1) {
							if (mReceivedDataCount < 20) {
								//发送电量的广播
								Intent batIntent = new Intent();
								batIntent.putExtra(Constants.EXTRAS_BATTERY_ELECTRIC_QUANTITY, mRemindEle);
								getActivity().sendBroadcast(batIntent);

								for (int i = 0; i < mEleList.size(); i++) {
									mEleSum = mEleSum + mEleList.get(i);
								}
								mRemindEle = (int) Math.floor(mEleSum / mEleList.size());
								mEleSum = 0;
								mEleList.clear();
								Message msg = Message.obtain();
								msg.what = 0;
								msg.arg1 = mRemindEle;
								msg.arg2 = mCurrentTemp;
								mHandler.sendMessage(msg);
							}
						}
						if (mReceivedDataCount % 60 == 0) {
							//发送电量的广播
							Intent batIntent = new Intent();
							batIntent.putExtra(Constants.EXTRAS_BATTERY_ELECTRIC_QUANTITY, mRemindEle);
							getActivity().sendBroadcast(batIntent);

							for (int i = 0; i < mEleList.size(); i++) {
								mEleSum = mEleSum + mEleList.get(i);
							}
							mRemindEle = (int) Math.floor(mEleSum / mEleList.size());
							mEleSum = 0;
							mEleList.clear();

							Message msg = Message.obtain();
							msg.what = 0;
							msg.arg1 = mRemindEle;
							msg.arg2 = mCurrentTemp;
							mHandler.sendMessage(msg);
						}
					}
				}
			}
		}
	};

	/**
	 * 根据电量来设置Ware的颜色-
	 */
	private void JudgeEleSetWare(int ele) {

		mWaveLoadingView.startAnimation();//当断开接连时,停止了动画，所以要开始动画

		mWaveLoadingView.setProgressValue(ele);
		mWaveLoadingView.setCenterTitle(ele + "%");
		if (ele >= Constants.EXTRAS_BATTERY_WARN) {
			mWaveLoadingView.setCenterTitleColor(Color.parseColor("#cc000000"));
			mWaveLoadingView.setWaveColor(getResources().getColor(R.color.battery_electric_quantity_normal));
		} else if (ele < Constants.EXTRAS_BATTERY_WARN && ele >= Constants.EXTRAS_BATTERY_DANGER) {
			mWaveLoadingView.setCenterTitleColor(getResources().getColor(R.color.battery_electric_quantity_warn));
			mWaveLoadingView.setWaveColor(getResources().getColor(R.color.battery_electric_quantity_warn));
		} else {
			Intent intent = new Intent(getActivity(), BatteryService.class);
			getActivity().startService(intent);
			mWaveLoadingView.setCenterTitleColor(getResources().getColor(R.color.battery_electric_quantity_danger));
			mWaveLoadingView.setWaveColor(getResources().getColor(R.color.battery_electric_quantity_danger));
		}
	}

	public static BluetoothLeService getBluetoothLeService() {
		return mBluetoothLeService;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unregisterReceiver(mBlueToothBroadCast);
		getActivity().unregisterReceiver(mGattUpdateReceiver);
		getActivity().unbindService(mServiceConnection);

		LogUtil.i(TAG, "We are in destroy");
	}


	public static boolean getConnectStatus(){
		return mConnected;
	}

	public void disConnect() {

		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}
	}

	public void disableBlueTooth() {
		if (mBluetoothLEAdapter != null) {
			mBluetoothLEAdapter.disable();
			mBluetoothLEAdapter = null;
		}
	}
}
