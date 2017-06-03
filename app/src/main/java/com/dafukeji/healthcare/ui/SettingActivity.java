package com.dafukeji.healthcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.andexert.library.RippleView;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.SPUtils;
import com.rey.material.widget.Switch;
import com.tencent.bugly.beta.Beta;

/**
 * Created by DevCheng on 2017/5/31.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{


	private ImageView ivBack;
	private RippleView rvAboutSoftware;
	private SwitchCompat scAutoUpdate,scNotification;
	private SPUtils spUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		spUtils=new SPUtils(Constants.SP_SETTING,this);
		initViews();
	}

	private void initViews() {

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

		scAutoUpdate= (SwitchCompat) findViewById(R.id.sc_auto_update);
		scAutoUpdate.setOnCheckedChangeListener(this);
		scAutoUpdate.setChecked(spUtils.getBoolean(Constants.SP_SETTING_AUTO_UPDATE,true));

		scNotification= (SwitchCompat) findViewById(R.id.sc_notification);
		scNotification.setOnCheckedChangeListener(this);
		scNotification.setChecked(spUtils.getBoolean(Constants.SP_SETTING_NOTIFICATION,true));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.iv_back:
				finish();
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
				spUtils.put(Constants.SP_SETTING_AUTO_UPDATE,isChecked);
				break;

			case R.id.sc_notification:
				if (isChecked){
					//TODO 接受推送消息
				}else{

				}
				spUtils.put(Constants.SP_SETTING_NOTIFICATION,isChecked);
				break;
		}
	}
}
