package com.dafukeji.healthcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.andexert.library.RippleView;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;

/**
 * Created by DevCheng on 2017/5/31.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener{


	private ImageView ivBack;
	private RippleView rvAboutSoftware;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.iv_back:
				finish();
				break;
		}
	}
}
