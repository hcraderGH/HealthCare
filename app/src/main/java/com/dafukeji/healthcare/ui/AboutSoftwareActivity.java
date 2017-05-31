package com.dafukeji.healthcare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;

/**
 * Created by DevCheng on 2017/5/31.
 */

public class AboutSoftwareActivity extends BaseActivity implements View.OnClickListener {

	private ImageView ivBack;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_software);

		initViews();
	}

	private void initViews() {
		ivBack= (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
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
