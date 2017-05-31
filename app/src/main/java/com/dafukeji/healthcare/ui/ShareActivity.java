package com.dafukeji.healthcare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;

/**
 * Created by DevCheng on 2017/5/28.
 */

public class ShareActivity extends BaseActivity implements View.OnClickListener,View.OnLongClickListener{

	private ImageView mIvBack;
	private ImageView ivShareQR;
	@Override
	protected void onCreate( Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		initViews();
	}

	private void initViews() {
		mIvBack= (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);

		ivShareQR=(ImageView)findViewById(R.id.iv_qr_code);
		ivShareQR.setOnLongClickListener(this);
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
	public boolean onLongClick(View v) {
		switch (v.getId()){
			case R.id.iv_qr_code:
				//TODO 弹出选择框选择程序发送二维码

				break;
		}

		return true;
	}
}
