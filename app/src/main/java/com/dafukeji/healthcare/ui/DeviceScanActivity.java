package com.dafukeji.healthcare.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.LeRecyclerAdapter;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.bean.Battery;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.ToastUtil;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

public class DeviceScanActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private LeRecyclerAdapter mLeDeviceRecyclerAdapter;
	private BluetoothAdapter mBluetoothLEAdapter;
	private RecyclerView mRecyclerView;
	private boolean mScanning = false;

	private Toolbar mToolbar;
	private ImageView ivBack;
	private ToggleButton tbScan;
	private RippleBackground mRippleBackground;
	private ShimmerTextView mSBScanString;

	private LinearLayout llNoBluetooth;

	private ScanCallback mScanCallback;
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback;

	// Stops scanning after SCAN_PERIOD seconds.
	private static final long SCAN_PERIOD = 6000;
	public static final int REQUEST_ENABLE_BT = 1;

	private static String TAG="测试DeviceScanActivity";

	private ProgressDialog mProgressDialog;
	private ProgressDialog mScanDialog;

	private BluetoothLeService mBluetoothLeService;

	private BluetoothDevice device;

	private static boolean isConnected=false;

	//电压显示相关
	private int mVoltageCount;
	private List<Integer> mVoltages =new ArrayList<>();

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setupActionBar();//必须放在setContentView方法前面
		setContentView(R.layout.activity_device_scan);

		initWidgets();
		initScanCallback();

		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			LogUtil.i(TAG, "onCreate: " + R.string.ble_not_supported);
			finish();
		}

		// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager =
				(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothLEAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothLEAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

//		// 若蓝牙没打开
//		if (!mBluetoothLEAdapter.isEnabled()) {
//			mBluetoothLEAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
//
//		}

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		LogUtil.i(TAG,"Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));//已经在HomeFragment中进行绑定服务了
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

	}

	// Code to manage Service lifecycle.
	public final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				LogUtil.i(TAG, "Unable to initialize Bluetooth");
				finish();
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


	//注册接收的事件
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothDevice.ACTION_UUID);

		intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

		return intentFilter;
	}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
				LogUtil.i(TAG, "Only gatt, just wait");
//				ToastUtil.showToast(getActivity(), "连接成功，现在可以正常通信！",1000);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				LogUtil.i(TAG, "mGattUpdateReceiver断开了连接");

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //可以开始干活了

				LogUtil.i(TAG, "In what we need");
				startSensorTimer();

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据

				final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				LogUtil.i(TAG, "onReceive: " + (data==null?"data为null":Arrays.toString(data)));
				if (data != null) {

					mVoltageCount++;
					mVoltages.add(ConvertUtils.byte2unsignedInt(data[7]));
					LogUtil.i(TAG,"mVoltageCount="+ mVoltageCount);


					//延迟跳转为了获取稳定的电压值
					if (mVoltageCount==6){
						stopTimer();//获取到数据的情况下,停止发送传感命令

						if (mProgressDialog!=null&&mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}

						Toasty.success(DeviceScanActivity.this, "连接设备成功", 500).show();

						//TODO 接收数据处理
						Intent intent2=new Intent();
						intent2.putExtra(Constants.EXTRAS_GATT_STATUS,true);
						intent2.setAction(Constants.RECEIVE_GATT_STATUS);
						sendBroadcast(intent2);

						Intent intent3 =new Intent();
						intent3.putExtra(Constants.RECEIVE_CURRENT_TEMP, ConvertUtils.byte2unsignedInt(data[3]));
						intent3.putExtra(Constants.RECEIVE_CURRENT_VOLTAGE, getMaxVoltage(mVoltages));
						Battery.ORIGINAL_VOLTAGE=getMaxVoltage(mVoltages);
						intent3.setAction(Constants.RECEIVE_BLUETOOTH_INFO);
						sendBroadcast(intent3);

						DeviceScanActivity.this.setResult(RESULT_OK);//通过广播发送的不需要此处
						finish();
					}
				}else{
					Toasty.warning(DeviceScanActivity.this, "连接失败，请重连或重启设备", 500).show();
				}
			}

		}
	};


	//TODO 此方法可以删去
	private int getAverageVoltage(List<Integer> voltages){
		int sum = 0;
		for (int i = 0; i < voltages.size(); i++) {
			sum+=voltages.get(i);
		}
		LogUtil.i(TAG,"电压平均值："+(int) Math.ceil(sum/voltages.size()));
		return (int) Math.ceil(sum/voltages.size());
	}

	private int getMaxVoltage(List<Integer> voltages){
		return Collections.max(voltages);
	}

	private TimerTask mSensorTask;
	private Timer mTimer;
	private int mSensorCmdCount;
	private void startSensorTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		if (mSensorTask == null) {
			mSensorTask = new TimerTask() {
				@Override
				public void run() {
					mSensorCmdCount++;
					if (mSensorCmdCount>=20){
						Message msg=new Message();
						msg.what=2;
						mHandler.sendMessage(msg);
					}

					LogUtil.i(TAG,"DeviceSanActivity发送的传感命令");
					byte[] init=new byte[]{(byte)0xFA,(byte)0xFB,6,0,0,0,0,0,0,0,0,6};
					if (mBluetoothLeService!=null) {
						mBluetoothLeService.WriteValue(init);
					}
				}
			};
		}

		if (mTimer != null && mSensorTask != null) {
			mTimer.schedule(mSensorTask,0, 400);
		}
	}

	private void stopTimer(){

		if(mSensorTask !=null){
			mSensorTask.cancel();
			mSensorTask =null;
		}

		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}


	private void initWidgets() {

		llNoBluetooth= (LinearLayout) findViewById(R.id.ll_no_bluetooth);

		mRecyclerView = (RecyclerView) findViewById(R.id.rlv_scan_devices);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mLeDeviceRecyclerAdapter = new LeRecyclerAdapter(this);
		mLeDeviceRecyclerAdapter.setOnItemClickListener(new LeRecyclerAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {

				if (mScanning) {
					stopScan();
					mScanning = false;
				}

				LogUtil.i(TAG,"onItemClick isConnected="+isConnected);

				device= mLeDeviceRecyclerAdapter.getDevice(position);

				mBluetoothLeService.connect(device.getAddress());
				if (mProgressDialog!=null){
					return;
				}

				mProgressDialog = new ProgressDialog(DeviceScanActivity.this);
				mProgressDialog.setMessage("正在连接设备，请稍等...");
				mProgressDialog.setCancelable(false);//设置进度条是否可以按退回键取消
				mProgressDialog.setCanceledOnTouchOutside(false);//设置点击进度对话框外的区域对话框是否消失
				mProgressDialog.show();

//					stopTimer();//确保不发生java.lang.IllegalStateException: TimerTask is scheduled already


			}
		});
		mRecyclerView.setAdapter(mLeDeviceRecyclerAdapter);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		mRippleBackground = (RippleBackground) findViewById(R.id.rb_scan);
		tbScan = (ToggleButton) findViewById(R.id.tb_scan);
		tbScan.setOnCheckedChangeListener(this);

		mSBScanString= (ShimmerTextView) findViewById(R.id.stv_scan_string);
		Shimmer shimmer=new Shimmer();
		shimmer.start(mSBScanString);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.tb_scan:
				LogUtil.i(TAG,"tbScan的状态："+tbScan.isChecked());
				if (tbScan.isChecked()) {
					mayRequestLocation();
				} else {

					if (mBluetoothLEAdapter.isEnabled()) {
						mSBScanString.setVisibility(View.VISIBLE);
						mRippleBackground.stopRippleAnimation();
						scanLeDevice(false);
					}
				}
				break;
		}
	}


	/**
	 * 在权限及蓝牙都开启的情况下显示搜索
	 */
	private void displayScan() {
		mRippleBackground.startRippleAnimation();
		mLeDeviceRecyclerAdapter.clear();
		mLeDeviceRecyclerAdapter.notifyDataSetChanged();

		mScanDialog=new ProgressDialog(this);
		mScanDialog.setMessage("正在搜索设备，请稍等...");
		mScanDialog.setCancelable(true);
		mScanDialog.setCanceledOnTouchOutside(true);
		mScanDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mScanning){
					scanLeDevice(false);
				}

				dialog.dismiss();
			}
		});

		mSBScanString.setVisibility(View.INVISIBLE);//当点击搜索时，则隐藏文字
		scanLeDevice(true);
	}

	private void openBlueTooth() {
		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			ToastUtil.showToast(this, "您的设备不支持低功耗蓝牙", 1000);
			finish();
			return;
		}

		// Ensures Bluetooth is available on the device and it is enabled. If not,
		// displays a dialog requesting user permission to enable Bluetooth.
		if (mBluetoothLEAdapter == null || !mBluetoothLEAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}else{
			//蓝牙已经开启了
			displayScan();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_ENABLE_BT){
			if (mBluetoothLEAdapter.isEnabled()){
				if (tbScan.isChecked()){
					displayScan();
				}
			}else {
				tbScan.setChecked(false);
			}
		}
	}

	private static final int REQUEST_COARSE_LOCATION=0;
	private void mayRequestLocation() {
		if (Build.VERSION.SDK_INT >= 23) {
			try {
				//校验是否已具有模糊定位权限，Android6.0蓝牙需要此权限
				int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
				if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
					//判断是否需要 向用户解释，为什么要申请该权限
					if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
						Toast.makeText(this,R.string.ble_need,Toast.LENGTH_LONG).show();

					ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_COARSE_LOCATION);

				}else{
					//具有权限,则去打开蓝牙
					openBlueTooth();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			//系统不高于6.0直接执行打开蓝牙
			openBlueTooth();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_COARSE_LOCATION:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// The requested permission is granted.

					//如果权限被允许了，则打开蓝牙
					openBlueTooth();
				} else{
					// The user disallowed the requested permission.
					LogUtil.i(TAG,"disallowed the requested location permission");

					//如果用户将模糊定位权限关闭了，则弹出对话框提示转到应用详情界面去设置权限
					tbScan.setChecked(false);
					showPermissionDialog();

				}
				break;
		}
	}

	private void showPermissionDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(DeviceScanActivity.this)
				.setTitle("提醒")
				.setMessage("开启蓝牙需要将定位权限设置为允许")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						startActivity(getAppDetailSettingIntent());
					}
				});
		builder.create().show();

	}

	/**
	 * 获取应用详情界面Intent
	 */
	private Intent getAppDetailSettingIntent(){
		Intent localIntent=new Intent();
		localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (Build.VERSION.SDK_INT>=9){
			localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			localIntent.setData(Uri.fromParts("package",getPackageName(),null));
		}else if (Build.VERSION.SDK_INT<=8){
			localIntent.setAction(Intent.ACTION_VIEW);
			localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
		}
		return localIntent;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1: // Notify change
					llNoBluetooth.setVisibility(View.GONE);
					mRecyclerView.setVisibility(View.VISIBLE);

					mLeDeviceRecyclerAdapter.notifyDataSetChanged();
					break;
				case 2://连接超时

					stopTimer();
					mProgressDialog.dismiss();
					mSensorCmdCount=0;
					mVoltageCount=0;
					mLeDeviceRecyclerAdapter.clear();
					mLeDeviceRecyclerAdapter.notifyDataSetChanged();
					Toasty.warning(DeviceScanActivity.this,"连接超时，请重连或重启设备",500).show();
					break;
			}
		}

	};


	private void scanLeDevice(final boolean enable) {
		if (enable) {
			mScanning = true;
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mScanning) {
						mScanning = false;
						LogUtil.i(TAG,"3000自动停止扫描");
						stopScan();
					}
				}
			}, SCAN_PERIOD);
			startScan();
			LogUtil.i(TAG,"开始扫描，蓝牙设备");
		} else {
			mScanning = false;
			stopScan();
		}

	}

	private void startScan() {
		if(mScanDialog!=null){
			mScanDialog.show();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					mBluetoothLEAdapter.getBluetoothLeScanner().startScan(mScanCallback);//这是耗时操作
				} else {
					mBluetoothLEAdapter.startLeScan(mLeScanCallback);
				}
			}
		}).start();
	}

	private void stopScan() {
		if (mScanDialog!=null&&mScanDialog.isShowing()){
			mScanDialog.dismiss();
		}
		tbScan.setChecked(false);

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					mBluetoothLEAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
				} else {
					mBluetoothLEAdapter.stopLeScan(mLeScanCallback);
				}
			}
		}).start();
	}

	@TargetApi(21)
	private void initScanCallback() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			mScanCallback =
					new ScanCallback() {
						@Override
						public void onScanResult(int callbackType, final ScanResult result) {

							if (result.getDevice().getName().equals(Constants.MATCH_DEVICE_NAME)) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											mLeDeviceRecyclerAdapter.addDevice(result.getDevice());
											if (mHandler!=null) {
												mHandler.sendEmptyMessage(1);
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
							}
						}
					};
		}else{
			mLeScanCallback =
					new BluetoothAdapter.LeScanCallback() {

						@Override
						public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

							if (device.getName().equals(Constants.MATCH_DEVICE_NAME)) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											mLeDeviceRecyclerAdapter.addDevice(device);
											if (mHandler!=null) {
												mHandler.sendEmptyMessage(1);
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
							}
						}
					};
		}
	}

	@Override
	protected void onDestroy() {
		LogUtil.i(TAG,"onDestroy()");
		if (mProgressDialog!=null&&mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
			mProgressDialog=null;
		}
		if (mScanDialog!=null&&mScanDialog.isShowing()){
			mScanDialog.dismiss();
			mScanDialog=null;
		}
		stopTimer();
		unbindService(mServiceConnection);
		unregisterReceiver(mGattUpdateReceiver);
		super.onDestroy();
	}
}
