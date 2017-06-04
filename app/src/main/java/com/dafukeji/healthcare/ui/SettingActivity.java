package com.dafukeji.healthcare.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andexert.library.RippleView;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.MyApplication;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.util.SettingManager;
import com.tencent.bugly.beta.Beta;

import java.util.Arrays;

/**
 * Created by DevCheng on 2017/5/31.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{


	private ImageView ivBack;
	private RippleView rvAboutSoftware;
	private SwitchCompat scAutoUpdate,scNotification,scNoDisturbing;
	private LinearLayout llNoDisturbing;
	private Button btnExit;


	private boolean isGATTConnected=false;
	private BlueToothBroadCast mBlueToothBroadCast;

	private static String TAG="测试SettingActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		//注册接受蓝牙信息的广播
		mBlueToothBroadCast=new BlueToothBroadCast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constants.RECEIVE_GATT_STATUS);
		registerReceiver(mBlueToothBroadCast,filter);

		initViews();

	}

	class BlueToothBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//得到蓝牙的服务连接
			isGATTConnected= intent.getBooleanExtra(Constants.EXTRAS_GATT_STATUS,false);
		}
	}

	private void initViews() {

		llNoDisturbing= (LinearLayout) findViewById(R.id.ll_no_disturbing);

		btnExit= (Button) findViewById(R.id.btn_exit);
		btnExit.setOnClickListener(this);

		ivBack= (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		rvAboutSoftware= (RippleView) findViewById(R.id.rv_about_software);

		rvAboutSoftware.setRippleDuration(getResources().getInteger(R.integer.rv_duration));
		rvAboutSoftware.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
			@Override
			public void onComplete(RippleView rippleView) {
				startActivity(new Intent(SettingActivity.this,AboutSoftwareActivity.class));
			}
		});


		//scNoDisturbing需要先初始化，否则当scNotification设置偏好设置保存的值时，后触发监听，而此时scNoDisturbing没有初始化的话，会导致空指针
		scNoDisturbing= (SwitchCompat) findViewById(R.id.sc_no_disturbing);
		scNoDisturbing.setOnCheckedChangeListener(this);
		scNoDisturbing.setChecked(SettingManager.getInstance().isNO_DISTURBING());

		scAutoUpdate= (SwitchCompat) findViewById(R.id.sc_auto_update);
		scAutoUpdate.setOnCheckedChangeListener(this);
		scAutoUpdate.setChecked(SettingManager.getInstance().isAUTO_UPDATE());

		scNotification= (SwitchCompat) findViewById(R.id.sc_notification);
		scNotification.setOnCheckedChangeListener(this);
		scNotification.setChecked(SettingManager.getInstance().isNOTIFICATION());


	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.iv_back:
				finish();
				break;
			case R.id.btn_exit:
				new MaterialDialog.Builder(this)
						.content("确定退出本程序吗？")
						.positiveText(R.string.dialog_ok)
						.negativeText(R.string.dialog_cancel)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								if (isGATTConnected){
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
									Log.i(TAG, "onClick: off"+ Arrays.toString(setting));
									HomeFragment.getBluetoothLeService().WriteValue(setting);
								}
								MyApplication.getInstance().exit();
							}
						}).show();
				break;
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()){
			case R.id.sc_auto_update:
				if (isChecked){
					Beta.autoCheckUpgrade=true;
				}else{
					Beta.autoCheckUpgrade=false;
				}
				SettingManager.getInstance().setAUTO_UPDATE(isChecked);
				break;

			case R.id.sc_notification:
				if (isChecked){
					//TODO 接受推送消息
					llNoDisturbing.setVisibility(View.VISIBLE);
				}else{
					scNoDisturbing.setChecked(false);
					llNoDisturbing.setVisibility(View.GONE);
				}
				SettingManager.getInstance().setNOTIFICATION(isChecked);
				break;
			case R.id.sc_no_disturbing:
				if (isChecked){
					//TODO 开启免打扰模式
				}else{

				}
				SettingManager.getInstance().setNO_DISTURBING(isChecked);
				break;
		}
	}
}
