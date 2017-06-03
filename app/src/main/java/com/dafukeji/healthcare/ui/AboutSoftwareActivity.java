package com.dafukeji.healthcare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.NetworkUtils;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;

/**
 * Created by DevCheng on 2017/5/31.
 */

public class AboutSoftwareActivity extends BaseActivity implements View.OnClickListener {

	private ImageView ivBack;
	private TextView tvNewVersion;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_software);

		initViews();

		if (NetworkUtils.isConnected()){
			UpgradeInfo upgradeInfo=Beta.getUpgradeInfo();
			tvNewVersion.setText("V"+upgradeInfo.versionName+"."+upgradeInfo.versionCode);
		}
	}


	private void initViews() {
		ivBack= (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		tvNewVersion= (TextView) findViewById(R.id.tv_new_version);
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
