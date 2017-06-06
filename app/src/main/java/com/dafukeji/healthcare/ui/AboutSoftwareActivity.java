package com.dafukeji.healthcare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.util.SettingManager;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;

/**
 * Created by DevCheng on 2017/5/31.
 */

public class AboutSoftwareActivity extends BaseActivity implements View.OnClickListener {

	private ImageView ivBack;
	private TextView tvNewVersion,tvCurrentVersion;
	private String currentVersion;
	private String newVersion;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_software);

		initViews();

	}

	private void initViews() {
		ivBack= (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		tvNewVersion= (TextView) findViewById(R.id.tv_new_version);
		tvCurrentVersion= (TextView) findViewById(R.id.tv_current_version);

		currentVersion=AppUtils.getAppVersionName()+"."+AppUtils.getAppVersionCode();
		tvCurrentVersion.setText(currentVersion);

		if (NetworkUtils.isConnected()){
			UpgradeInfo upgradeInfo=Beta.getUpgradeInfo();
			if (upgradeInfo!=null){//如果Bugly中没有发布新的版本，在此需要进行判断
				newVersion=upgradeInfo.versionName+"."+upgradeInfo.versionCode;
				SettingManager.getInstance().setNEW_VERSION(newVersion);
			}else{//用本地版本代替最新版本号
				newVersion=currentVersion;

			}
		}else {
			if (SettingManager.getInstance().getNEW_VERSION(currentVersion).equals(currentVersion)){
				newVersion=currentVersion;
			}else{
				newVersion=SettingManager.getInstance().getNEW_VERSION(currentVersion);
			}

		}
		tvNewVersion.setText(newVersion);
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
