package com.dafukeji.healthcare.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.LogUtil;

/**
 * Created by DevCheng on 2017/6/1.
 */
public class ScanService extends Service {

	private BluetoothAdapter mBluetoothAdapter;
	private ScanCallback mScanCallback;
	private BluetoothAdapter.LeScanCallback mLeScanCallback;


	private String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED";//判断设备是否连接成功
	private String ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";//蓝牙的开关状态

	private String ACTION_FOUND = "android.bluetooth.device.action.FOUND";//扫描到设备触发的广播;
	private int STATE_OFF = 10; //蓝牙关闭
	private int STATE_ON = 12; //蓝牙打开

	private int STATE_DISCONNECTED = 0; //未连接
	private int STATE_CONNECTING = 1; //连接中
	private int STATE_CONNECTED = 2; //连接成功

	private static String TAG="测试ScanService";

	@Override
	public void onCreate() {

		BlueToothBroadCast blueToothBroadCast=new BlueToothBroadCast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(ACTION_STATE_CHANGED);
		filter.addAction(ACTION_CONNECTION_STATE_CHANGED);
		this.registerReceiver(blueToothBroadCast,filter);

		LogUtil.i(TAG,"ScanService onCreate()");

		initScanCallback();

		final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter!=null&mBluetoothAdapter.isEnabled()){//当启动应用前已经打开了蓝牙，则直接连接
			startScan();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	class BlueToothBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.STATE_ON);
			LogUtil.i(TAG,"state"+state);
			if (state==STATE_ON){
				startScan();
			}

			int connectStatus=intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR); //当前的连接状态
			LogUtil.i(TAG,"connectStatus"+connectStatus);
			if (connectStatus==STATE_CONNECTED){
				stopScan();
			}

//			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//			if (device==null){
//				return;
//			}
//			String name = device.getName();
//			String addr = device.getAddress();
//			if (addr.equals(Constants.MATCH_DEVICE_ADDRESS)){
//				Intent i = new Intent();
//				i.putExtra(Constants.EXTRAS_DEVICE_NAME, name);
//				i.putExtra(Constants.EXTRAS_DEVICE_ADDRESS, addr);
//				i.setAction(Constants.RECEIVE_BLUETOOTH_INFO);
//				sendBroadcast(i);
//			}
		}
	}

	private void startScan() {
		LogUtil.i(TAG,"startScan()");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
		} else {
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		}
	}

	private void stopScan() {
		LogUtil.i(TAG,"stopScan()");
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
							if (result.getDevice().getName().equals(Constants.MATCH_DEVICE_NAME)){
								Intent intent = new Intent();
								intent.putExtra(Constants.EXTRAS_DEVICE_NAME, result.getDevice().getName());
								intent.putExtra(Constants.EXTRAS_DEVICE_ADDRESS, result.getDevice().getAddress());
								intent.setAction(Constants.RECEIVE_BLUETOOTH_INFO);
								sendBroadcast(intent);
							}
						}
					};
		}else{
			mLeScanCallback =
					new BluetoothAdapter.LeScanCallback() {

						@Override
						public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
							if (device.getName().equals(Constants.MATCH_DEVICE_NAME)){
								Intent intent = new Intent();
								intent.putExtra(Constants.EXTRAS_DEVICE_NAME, device.getName());
								intent.putExtra(Constants.EXTRAS_DEVICE_ADDRESS, device.getAddress());
								intent.setAction(Constants.RECEIVE_BLUETOOTH_INFO);
								sendBroadcast(intent);
							}
						}
					};
		}
	}

	@Override
	public void onDestroy() {
		LogUtil.i(TAG,"ScanService停止服务 onDestroy()");
		super.onDestroy();
	}
}
