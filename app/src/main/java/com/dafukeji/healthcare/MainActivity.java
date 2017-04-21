package com.dafukeji.healthcare;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.ToastUtil;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.TimePickerDialog;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

	@BindView(R.id.tg_zhenjiu_start_or_stop)
	ToggleButton tgZhenjiuStartOrStop;
	@BindView(R.id.tg_zhenjiu_pause_or_continue)
	ToggleButton tgZhenjiuPauseOrContinue;
	@BindView(R.id.tg_warm_pause_or_continue)
	ToggleButton tgWarmPauseOrContinue;
	@BindView(R.id.tg_warm_start_or_stop)
	ToggleButton tgWarmStartOrStop;

	@BindView(R.id.btn_warm_time)
	Button btWarmTime;
	@BindView(R.id.btn_zhenjiu_time)
	Button btZhenjiuTime;
	@BindView(R.id.btn_temperature_pick)
	Button btTemperaturePick;

	@BindView(R.id.tv_current_temperature)
	TextView tvCurrentTemp;

	private NiceSpinner nsGears;

	private String zhenjiuTime = "0分钟";
	private String warmTime = "0分钟";

	private String TAG ="测试";


	private BluetoothAdapter mBluetoothLEAdapter;
	private String mDeviceName;
	private String mDeviceAddress="D8:B0:4C:B1:F5:83";
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
	private boolean mConnected = false;

	private int selectedGrade =1;//档位

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);


		functionZhenjiu = this.getResources().getString(R.string.zhenjiu);
		functionWarm = this.getResources().getString(R.string.warm);

		nsGears = (NiceSpinner) findViewById(R.id.sp_gears_select);
		nsGears.setTextColor(getResources().getColor(R.color.subtitle_text_color));//选中的文字的颜色
		nsGears.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		final LinkedList<String> data = new LinkedList<>(Arrays.asList("一档", "二挡", "三挡", "四挡", "五档"));
		nsGears.attachDataSource(data);
		nsGears.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ToastUtil.showToast(MainActivity.this, "您选择了" + data.get(position), 1500);
				selectedGrade= position+1;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothLEAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothLEAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		// 若蓝牙没打开
		if (!mBluetoothLEAdapter.isEnabled()) {
			mBluetoothLEAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
		}
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
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
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				mConnected = false;
				ToastUtil.showToast(MainActivity.this, "连接成功，现在可以正常通信！",1000);
				//TODO 断开连接处理
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){ //可以开始干活了
				mConnected = true;
				Log.e(TAG, "In what we need");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
				Log.e(TAG, "DATA");
				byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				if (data != null) {
					//TODO 接收数据处理
					tvCurrentTemp.setText(ConvertUtils.bytes2HexString(data)+"℃");//TODO 注意此处获取的数据
				}
			}
		}
	};


	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mConnected) {
			menu.findItem(R.id.action_disconnect).setVisible(false);
		} else {
			menu.findItem(R.id.action_connect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_connect) {
			Intent i = new Intent(this, DeviceScanActivity.class);
			startActivityForResult(i, 0, null);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			//获取连接上的蓝牙设备
			mDeviceAddress= data.getStringExtra(Constants.EXTRAS_DEVICE_ADDRESS);
			mBluetoothLeService.connect(mDeviceAddress);
		}
	}

	private String functionZhenjiu;
	private String functionWarm;
	private String functionNull = "";
	private String function = "";

	@OnCheckedChanged(R.id.tg_zhenjiu_start_or_stop)
	public void SOSZhenjiu(CompoundButton button) {
		if (zhenjiuTime.equals("0分钟")) {
			tgZhenjiuStartOrStop.setChecked(false);
			ToastUtil.showToast(this, "请选定持续时间", 1500);
		} else {
			if (button.isChecked() && function.equals(functionWarm)) {
				tgZhenjiuStartOrStop.setChecked(false);
				showReminderDialog(functionZhenjiu, zhenjiuTime, btWarmTime, tgZhenjiuPauseOrContinue
						, tgWarmPauseOrContinue, tgZhenjiuStartOrStop, tgWarmStartOrStop);
			} else if (button.isChecked() && (!function.equals(functionWarm))) {//正式开始
				function = functionZhenjiu;
				tgZhenjiuPauseOrContinue.setVisibility(View.VISIBLE);
				//通过蓝牙写入数据
				if (!mBluetoothLEAdapter.isEnabled()){
					ToastUtil.showToast(MainActivity.this,"请打开蓝牙",1000);
					return;
				}
				Log.i(TAG, "SOSZhenjiu:selectedGrade "+selectedGrade);
				Log.i(TAG, "SOSZhenjiu:Data "+Arrays.toString(String.valueOf(selectedGrade).getBytes()));
				mBluetoothLeService.WriteValue(String.valueOf(selectedGrade).getBytes());//暂时只写入档位
			} else {
				tgZhenjiuPauseOrContinue.setVisibility(View.GONE);
				function = functionNull;
				zhenjiuTime = getString(R.string.default_time);
				btZhenjiuTime.setText(zhenjiuTime);
			}
		}
	}


	@OnCheckedChanged(R.id.tg_warm_start_or_stop)
	public void SOSWarm(CompoundButton button) {
		if (warmTime.equals("0分钟")) {
			tgWarmStartOrStop.setChecked(false);
			ToastUtil.showToast(this, "请选定持续时间", 1500);
		} else {
			if (button.isChecked() && function.equals(functionZhenjiu)) {
				tgWarmStartOrStop.setChecked(false);
				showReminderDialog(functionWarm, warmTime, btZhenjiuTime, tgWarmPauseOrContinue
						, tgZhenjiuPauseOrContinue, tgWarmStartOrStop, tgZhenjiuStartOrStop);
			} else if (button.isChecked() && (!function.equals(functionZhenjiu))) {
				function = functionWarm;
				tgWarmPauseOrContinue.setVisibility(View.VISIBLE);
			} else {
				function = functionNull;
				tgWarmPauseOrContinue.setVisibility(View.GONE);
				warmTime = getString(R.string.default_time);
				btWarmTime.setText(warmTime);
			}
		}
	}


	@OnClick({R.id.btn_zhenjiu_time, R.id.btn_warm_time, R.id.btn_temperature_pick})
	void TimeSelect(View v) {

		Dialog.Builder builder = null;

		switch (v.getId()) {
			case R.id.btn_warm_time://加热功能

				builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light, 24, 00) {
					@Override
					public void onPositiveActionClicked(DialogFragment fragment) {
						TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();

						int hour = dialog.getHour();
						int minute = dialog.getMinute();
						if (hour == 0) {
							warmTime = minute + "分钟";
						} else if (minute < 10) {
							warmTime = hour + "小时" + minute + "0分钟";
						} else {
							warmTime = hour + "小时" + minute + "分钟";
						}

						btWarmTime.setText(warmTime);
						ToastUtil.showToast(MainActivity.this, "您选择的持续时间是" + warmTime, 1500);
						super.onPositiveActionClicked(fragment);//此代码必须放在下面

					}

					@Override
					public void onNegativeActionClicked(DialogFragment fragment) {
						super.onNegativeActionClicked(fragment);

					}
				};
				builder.positiveAction("确定")
						.negativeAction("取消");

				break;

			case R.id.btn_zhenjiu_time://针灸功能

				builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light, 24, 00) {
					@Override
					public void onPositiveActionClicked(DialogFragment fragment) {
						TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();

						int hour = dialog.getHour();
						int minute = dialog.getMinute();
						if (hour == 0) {
							zhenjiuTime = minute + "分钟";
						} else if (minute < 10) {
							zhenjiuTime = hour + "小时" + minute + "0分钟";
						} else {
							zhenjiuTime = hour + "小时" + minute + "分钟";
						}

						btZhenjiuTime.setText(zhenjiuTime);
						ToastUtil.showToast(MainActivity.this, "您选择的持续时间是" + zhenjiuTime, 1500);
						super.onPositiveActionClicked(fragment);//此代码必须放在下面

					}

					@Override
					public void onNegativeActionClicked(DialogFragment fragment) {
						super.onNegativeActionClicked(fragment);

					}
				};
				builder.positiveAction("确定")
						.negativeAction("取消");
				break;

			case R.id.btn_temperature_pick:
				builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light) {
					@Override
					public void onPositiveActionClicked(DialogFragment fragment) {
						btTemperaturePick.setText(getSelectedValue());
						ToastUtil.showToast(MainActivity.this, "您选择了" + getSelectedValue(), 1500);
						super.onPositiveActionClicked(fragment);
					}

					@Override
					public void onNegativeActionClicked(DialogFragment fragment) {
						super.onNegativeActionClicked(fragment);
					}
				};
				((SimpleDialog.Builder) builder).items(new String[]{"40℃", "45℃", "50℃", "55℃", "60℃"}, 0)
						.title("加热温度选择")
						.positiveAction("确定")
						.negativeAction("取消");
				break;
		}
		DialogFragment fragment = DialogFragment.newInstance(builder);
		fragment.show(getSupportFragmentManager(), null);

	}


	private void showReminderDialog(final String currentFunction,
	                                String remindTime,
	                                final Button ingBt,
	                                final ToggleButton tgPOCVisible,
	                                final ToggleButton tgPOCGone,
	                                final ToggleButton tgSOSChecked, final ToggleButton tgSOSUnchecked) {
		Dialog.Builder builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light) {
			@Override
			public void onPositiveActionClicked(DialogFragment fragment) {
//				if (currentFunction.equals(functionZhenjiu)){
//					warmTime=getString(R.string.default_time);
//				}else if(currentFunction.equals(functionWarm)){
//					zhenjiuTime=getString(R.string.default_time);
//				}else{
//
//				}
				function = currentFunction;
				ingBt.setText(getResources().getString(R.string.default_time));
				tgPOCVisible.setVisibility(View.VISIBLE);
				tgPOCGone.setVisibility(View.GONE);
				tgSOSUnchecked.setChecked(false);
				tgSOSChecked.setChecked(true);

				super.onPositiveActionClicked(fragment);
			}

			@Override
			public void onNegativeActionClicked(DialogFragment fragment) {
				tgSOSChecked.setChecked(false);
				super.onNegativeActionClicked(fragment);
			}
		};

		((SimpleDialog.Builder) builder).message("您正在使用" + function + "功能,剩余" + remindTime + ",是否进行" + currentFunction)
				.title("提示")
				.positiveAction("确定")
				.negativeAction("取消");

		DialogFragment fragment = DialogFragment.newInstance(builder);
		fragment.show(getSupportFragmentManager(), null);

	}

	/**
	 * 按两次back键退出
	 */
	private static Boolean isExit = false;
	private Timer tExit = new Timer();
	private TimerTask task;

	@Override
	public void onBackPressed() {
		if (isExit == false) {
			isExit = true;
			ToastUtil.showToast(this, "再按一次退出程序", 1000);
			task = new TimerTask() {
				@Override
				public void run() {
					isExit = false;
				}
			};
			tExit.schedule(task, 2000);
		} else {
			ToastUtil.cancelToast();
			MyApplication.getInstance().exit();
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		//this.unregisterReceiver(mGattUpdateReceiver);
		//unbindService(mServiceConnection);
		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}
		if (mBluetoothLEAdapter != null) {
			mBluetoothLEAdapter.disable();
		}
		Log.d(TAG, "We are in destroy");
	}


}
