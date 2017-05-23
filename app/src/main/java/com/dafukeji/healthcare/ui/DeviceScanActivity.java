package com.dafukeji.healthcare.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.LeRecyclerAdapter;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.util.StatusBar;
import com.dafukeji.healthcare.util.ToastUtil;
import com.skyfishjy.library.RippleBackground;

public class DeviceScanActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private LeRecyclerAdapter mLeDeviceRecyclerAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private RecyclerView mRecyclerView;
	private boolean mScanning = false;

	private Toolbar mToolbar;
	private ImageView ivBack;
	private ToggleButton tbScan;
	private RippleBackground mRippleBackground;

	private ScanCallback mScanCallback;
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback;

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 3000;
	public static final int REQUEST_ENABLE_BT = 1;

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

		initScanCallback();

		initWidgets();
		StatusBar.setImmersiveStatusBar(this, mToolbar, R.color.app_bar_color);

		mayRequestLocation();
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
				final BluetoothDevice device = mLeDeviceRecyclerAdapter.getDevice(position);
				if (device == null) return;
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
					openBlueTooth();
					Log.i("DeviceScan蓝牙打开了？", mBluetoothAdapter.isEnabled() + "");
					if (!mBluetoothAdapter.isEnabled()) {
						return;
					}
					mRippleBackground.startRippleAnimation();
					mLeDeviceRecyclerAdapter.clear();
					scanLeDevice(true);
				} else {
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
//				Log.i("蓝牙是否打开了",mBluetoothAdapter.isEnabled()+"");
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
//						invalidateOptionsMenu();
					}
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mLeDeviceRecyclerAdapter.clear();
			startScan();
			Log.i("DeviceScan", "开始扫描，蓝牙设备");
		} else {
			mScanning = false;
			stopScan();
		}

//		//对Menu进行重新绘制
//		invalidateOptionsMenu();
	}

	private void startScan() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
		} else {
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		}
	}

	private void stopScan() {
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
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mLeDeviceRecyclerAdapter.addDevice(result.getDevice());
									mHandler.sendEmptyMessage(1);
								}
							});
						}
					};
		}else{
			mLeScanCallback =
					new BluetoothAdapter.LeScanCallback() {

						@Override
						public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
							Log.i("DeviceScan", "onLeScan");
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mLeDeviceRecyclerAdapter.addDevice(device);
									mHandler.sendEmptyMessage(1);
								}
							});
						}
					};
		}
	}
}
