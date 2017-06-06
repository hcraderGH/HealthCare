package com.dafukeji.healthcare.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.LeRecyclerAdapter;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.StatusBar;
import com.dafukeji.healthcare.util.ToastUtil;
import com.orhanobut.logger.Logger;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;
import com.romainpiel.shimmer.ShimmerTextView;
import com.skyfishjy.library.RippleBackground;

import es.dmoral.toasty.Toasty;

public class DeviceScanActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private LeRecyclerAdapter mLeDeviceRecyclerAdapter;
	private BluetoothAdapter mBluetoothAdapter;
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

	private static BluetoothLeService mBluetoothLeService;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setupActionBar();//必须放在setContentView方法前面
		setContentView(R.layout.activity_device_scan);

		//设定默认返回值为取消
		setResult(RESULT_CANCELED);

		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();


//		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//		LogUtil.i(TAG, "Try to bindService=" + this.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE));

		initScanCallback();
		initWidgets();
		mayRequestLocation();
	}


//	// Code to manage Service lifecycle.
//	public final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName componentName, IBinder service) {
//			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//			if (!mBluetoothLeService.initialize()) {
//				LogUtil.i(TAG, "Unable to initialize Bluetooth");
//				finish();
//			}
//
//			LogUtil.i(TAG, "mBluetoothLeService is okay");
//
//			// Automatically connects to the device upon successful start-up initialization.
//			//mBluetoothLeService.connect(mDeviceAddress);
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName componentName) {
//			mBluetoothLeService = null;
//		}
//	};


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

		mProgressDialog=new ProgressDialog(this);
		mProgressDialog.setMessage("正在搜索蓝牙，请稍等...");

		mRecyclerView = (RecyclerView) findViewById(R.id.rlv_scan_devices);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mLeDeviceRecyclerAdapter = new LeRecyclerAdapter(this);
		mLeDeviceRecyclerAdapter.setOnItemClickListener(new LeRecyclerAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				final BluetoothDevice device = mLeDeviceRecyclerAdapter.getDevice(position);
				if (device == null) return;
//				boolean isConnected=mBluetoothLeService.connect(device.getAddress());
//				if (!isConnected){
//					Toasty.warning(DeviceScanActivity.this,"未能连接上设备，请重启设备",Toast.LENGTH_LONG).show();
//					return;
//				}

				Intent intent = new Intent();
				intent.putExtra(Constants.EXTRAS_DEVICE_NAME, device.getName());
				intent.putExtra(Constants.EXTRAS_DEVICE_ADDRESS, device.getAddress());
				intent.setAction(Constants.RECEIVE_BLUETOOTH_INFO);
				sendBroadcast(intent);
				finish();
				if (mScanning) {
					stopScan();
					mScanning = false;
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
					mSBScanString.setVisibility(View.INVISIBLE);//当点击搜索时，则隐藏文字
					openBlueTooth();
					Logger.i("DeviceScan蓝牙打开了？"+mBluetoothAdapter.isEnabled());
					if (!mBluetoothAdapter.isEnabled()) {
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


//	private void setupActionBar(){
//		ActionBar actionBar=getSupportActionBar();
//		if (actionBar!=null){
//			actionBar.setDisplayHomeAsUpEnabled(true);//为ActionBar的左边添加返回图标并执行返回功能
//		}
//	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.scan_menu,menu);
//		if (!mScanning){//没有进行扫描时
//			menu.findItem(R.id.menu_stop).setVisible(false);
//			menu.findItem(R.id.menu_scan).setVisible(true);
//			menu.findItem(R.id.menu_refresh).setActionView(null);
//		}else{
//			menu.findItem(R.id.menu_stop).setVisible(true);
//			menu.findItem(R.id.menu_scan).setVisible(false);
//			menu.findItem(R.id.menu_refresh).setActionView(
//					R.layout.actionbar_indeterminate_progress);
//		}
//
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//
//		switch (item.getItemId()){
//
//			case R.id.menu_scan:
//				openBlueTooth();
//				Logger.i("蓝牙是否打开了"+mBluetoothAdapter.isEnabled());
//				mLeDeviceRecyclerAdapter.clear();
//				scanLeDevice(true);
//				break;
//			case R.id.menu_stop:
//				scanLeDevice(false);
//				break;
//
//			case android.R.id.home:
//				NavUtils.navigateUpFromSameTask(this);
//				break;
//		}
//		return true;
//	}

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
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	public final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1: // Notify change
					mLeDeviceRecyclerAdapter.notifyDataSetChanged();
					break;
			}
		}
	};

	private void scanLeDevice(final boolean enable) {
		if (enable) {
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

			mScanning = true;
			mLeDeviceRecyclerAdapter.clear();
			startScan();
			Logger.i("开始扫描，蓝牙设备");
		} else {
			mScanning = false;
			stopScan();
		}

//		//对Menu进行重新绘制
//		invalidateOptionsMenu();
	}

	private void startScan() {
		mProgressDialog.show();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
		} else {
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		}
	}

	private void stopScan() {
		mProgressDialog.dismiss();
		tbScan.setChecked(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
		} else {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}


	@TargetApi(21)
	private void initScanCallback() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			mScanCallback =
					new ScanCallback() {
						@Override
						public void onScanResult(int callbackType, final ScanResult result) {

							LogUtil.i(TAG,"蓝牙的信号强度："+result.getRssi());

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

							LogUtil.i(TAG,"蓝牙的信号强度："+rssi);

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


//	public void unbindService(){
//		unbindService(mServiceConnection);
//		if (mBluetoothLeService != null) {
//			mBluetoothLeService.close();
//			mBluetoothLeService = null;
//		}
//	}
}
