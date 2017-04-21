package com.dafukeji.healthcare;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dafukeji.healthcare.util.ToastUtil;

public class DeviceScanActivity extends AppCompatActivity {

	private LeRecyclerAdapter mLeDeviceRecyclerAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private RecyclerView mRecyclerView;
	private boolean mScanning=false;

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 3000;
	public static final int REQUEST_ENABLE_BT=1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();//必须放在setContentView方法前面
		setContentView(R.layout.activity_device_scan);
		setTitle("查找设备");
		//设定默认返回值为取消
		setResult(RESULT_CANCELED);

		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		initWidgets();

	}

	private void initWidgets() {
		mRecyclerView= (RecyclerView) findViewById(R.id.rlv_scan_devices);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mLeDeviceRecyclerAdapter =new LeRecyclerAdapter(this);
		mLeDeviceRecyclerAdapter.setOnItemClickListener(new LeRecyclerAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				final BluetoothDevice device=mLeDeviceRecyclerAdapter.getDevice(position);
				if (device==null) return;
				//设置返回数据
				Intent intent=new Intent();
				intent.putExtra(Constants.EXTRAS_DEVICE_NAME,device.getName());
				intent.putExtra(Constants.EXTRAS_DEVICE_ADDRESS,device.getAddress());
				//设置返回值并结束程序
				setResult(RESULT_OK,intent);
				finish();
				if (mScanning){
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mScanning=false;
				}

			}
		});
		mRecyclerView.setAdapter(mLeDeviceRecyclerAdapter);
	}


	private void setupActionBar(){
		ActionBar actionBar=getSupportActionBar();
		if (actionBar!=null){
			actionBar.setDisplayHomeAsUpEnabled(true);//为ActionBar的左边添加返回图标并执行返回功能
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.scan_menu,menu);
		if (!mScanning){//没有进行扫描时
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		}else{
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()){

			case R.id.menu_scan:
				openBlueTooth();
				Log.i("蓝牙是否打开了",mBluetoothAdapter.isEnabled()+"");
				mLeDeviceRecyclerAdapter.clear();
				scanLeDevice(true);
				break;
			case R.id.menu_stop:
				scanLeDevice(false);
				break;

			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				break;
		}
		return true;
	}

	private void openBlueTooth() {
		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			ToastUtil.showToast(this,"您的设备不支持低功耗蓝牙",1000);
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

	// Hander
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
					if (mScanning){
						mScanning = false;
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						invalidateOptionsMenu();
					}
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mLeDeviceRecyclerAdapter.clear();
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}

		//对Menu进行重新绘制
		invalidateOptionsMenu();
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {

				@Override
				public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
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
