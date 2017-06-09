package com.dafukeji.healthcare.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.LeRecyclerAdapter;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.ToastUtil;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.skyfishjy.library.RippleBackground;

import java.util.Arrays;
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

	private ScanCallback mScanCallback;
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback;

	// Stops scanning after SCAN_PERIOD seconds.
	private static final long SCAN_PERIOD = 3000;
	public static final int REQUEST_ENABLE_BT = 1;

	private static String TAG="测试DeviceScanActivity";

	private ProgressDialog mProgressDialog;

	private BluetoothLeService mBluetoothLeService;

	private BluetoothDevice device;

	private TimerTask mTimerTask;
	private Timer mTimer;
	private int mOverTime ;

	private boolean isItemClicked=false;

	private static boolean isConnected=false;


	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setupActionBar();//必须放在setContentView方法前面
		setContentView(R.layout.activity_device_scan);

		initScanCallback();
		initWidgets();
		mayRequestLocation();

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
		// 若蓝牙没打开
		if (!mBluetoothLEAdapter.isEnabled()) {
			mBluetoothLEAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限

		}

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

				byte[] init=new byte[]{(byte)0xFA,(byte)0xFB,6,0,0,0,0,0,0,0,0,6};
				HomeFragment.getBluetoothLeService().WriteValue(init);

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据

				if (!isItemClicked){
					return;
				}

				final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				LogUtil.i(TAG, "onReceive: " + (data==null?"data为null":Arrays.toString(data)));
				if (data != null) {

					isItemClicked=false;
					if (mProgressDialog.isShowing()) {
						stopTimer();//获取到数据的情况下，停止计时
						mProgressDialog.dismiss();
					}
					Toasty.success(DeviceScanActivity.this, "连接设备成功", 500).show();
					//TODO 接收数据处理
					Intent intent2=new Intent();
					intent2.putExtra(Constants.EXTRAS_GATT_STATUS,true);
					intent2.setAction(Constants.RECEIVE_GATT_STATUS);
					sendBroadcast(intent2);
					finish();

				}else{
					Toasty.warning(DeviceScanActivity.this, "连接失败，请重连或重启设备", 500).show();
				}
			}else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
				LogUtil.i(TAG,"设备断开了");
				isConnected=false;
				if (isItemClicked){
					mBluetoothLeService.connect(device.getAddress());
				}
			}else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
				LogUtil.i(TAG,"设备连接上了");
				isConnected=true;
			}
		}
	};


	private void startTimer() {
		mOverTime=21000;//连接断开的时间（华为与其他机器是否一样）
		if (mTimer == null) {
			mTimer = new Timer();
		}
		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					mOverTime=mOverTime-1000;
					if (mOverTime==0) {
						Message msg = Message.obtain();
						msg.what = 2;
						mHandler.sendMessage(msg);
					}
				}
			};
		}

		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, 1000, 1000);
		}
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}


	private static final int REQUEST_FINE_LOCATION=0;
	private void mayRequestLocation() {
		if (Build.VERSION.SDK_INT >= 23) {
			int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
			if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
				//判断是否需要 向用户解释，为什么要申请该权限
				if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
					Toast.makeText(this,R.string.ble_need,Toast.LENGTH_LONG).show();

				ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_FINE_LOCATION);
				return;

			}else{

			}
		} else {

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_FINE_LOCATION:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// The requested permission is granted.
					if (mScanning == false) {
						scanLeDevice(true);
					}
				} else{
					// The user disallowed the requested permission.
				}
				break;
		}
	}


	private void initWidgets() {

		mRecyclerView = (RecyclerView) findViewById(R.id.rlv_scan_devices);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mLeDeviceRecyclerAdapter = new LeRecyclerAdapter(this);
		mLeDeviceRecyclerAdapter.setOnItemClickListener(new LeRecyclerAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {

				isItemClicked=true;

				if (mScanning) {
					stopScan();
					mScanning = false;
				}

				LogUtil.i(TAG,"onItemClick isConnected="+isConnected);

				if (!isConnected){
					device= mLeDeviceRecyclerAdapter.getDevice(position);
					mBluetoothLeService.connect(device.getAddress());
				}else{
					mProgressDialog=new ProgressDialog(DeviceScanActivity.this);
					mProgressDialog.setMessage("正在连接设备，请稍等...");
					mProgressDialog.setCancelable(false);//设置进度条是否可以按退回键取消
					mProgressDialog.setCanceledOnTouchOutside(true);//设置点击进度对话框外的区域对话框是否消失
					mProgressDialog.show();
					startTimer();//开始连接倒计时
				}
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
				if (tbScan.isChecked()) {

					mProgressDialog=new ProgressDialog(this);
					mProgressDialog.setMessage("正在搜索设备，请稍等...");
					mProgressDialog.setCancelable(true);
					mProgressDialog.setCanceledOnTouchOutside(true);

					mSBScanString.setVisibility(View.INVISIBLE);//当点击搜索时，则隐藏文字
					openBlueTooth();
					LogUtil.i(TAG,"DeviceScan蓝牙打开了？"+mBluetoothLEAdapter.isEnabled());
					if (!mBluetoothLEAdapter.isEnabled()) {
						return;
					}
					mRippleBackground.startRippleAnimation();
					mLeDeviceRecyclerAdapter.clear();
					mLeDeviceRecyclerAdapter.notifyDataSetChanged();
					scanLeDevice(true);
				} else {
					mSBScanString.setVisibility(View.VISIBLE);
					mRippleBackground.stopRippleAnimation();
					scanLeDevice(false);
				}
				break;
		}
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
		}
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1: // Notify change
					mLeDeviceRecyclerAdapter.notifyDataSetChanged();
					break;
				case 2://连接超时
					stopTimer();
//					mBluetoothLeService.disconnect();
//					mBluetoothLeService.close();
//					mBluetoothLeService=null;
					if (mProgressDialog!=null){
						mProgressDialog.dismiss();
					}
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
						stopScan();
					}
				}
			}, SCAN_PERIOD);
			mLeDeviceRecyclerAdapter.clear();
			startScan();
			LogUtil.i(TAG,"开始扫描，蓝牙设备");
		} else {
			mScanning = false;
			stopScan();
		}

	}

	private void startScan() {
		mProgressDialog.show();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBluetoothLEAdapter.getBluetoothLeScanner().startScan(mScanCallback);
		} else {
			mBluetoothLEAdapter.startLeScan(mLeScanCallback);
		}
	}

	private void stopScan() {
		mProgressDialog.dismiss();

		tbScan.setChecked(false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBluetoothLEAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
		} else {
			mBluetoothLEAdapter.stopLeScan(mLeScanCallback);
		}
	}

	@TargetApi(21)
	private void initScanCallback() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			mScanCallback =
					new ScanCallback() {
						@Override
						public void onScanResult(int callbackType, final ScanResult result) {

//							LogUtil.i(TAG,"蓝牙的信号强度："+result.getRssi());

							if (result.getDevice().getName().equals(Constants.MATCH_DEVICE_NAME)) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										mLeDeviceRecyclerAdapter.addDevice(result.getDevice());
										mHandler.sendEmptyMessage(1);
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

//							LogUtil.i(TAG,"蓝牙的信号强度："+rssi);

							if (device.getName().equals(Constants.MATCH_DEVICE_NAME)) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										mLeDeviceRecyclerAdapter.addDevice(device);
										mHandler.sendEmptyMessage(1);
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
		unbindService(mServiceConnection);
		unregisterReceiver(mGattUpdateReceiver);
		super.onDestroy();
	}
}
