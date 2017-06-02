package com.dafukeji.healthcare.ui;

import android.content.Intent;
import android.os.Bundle;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;

/**
 * Created by DevCheng on 2017/6/2.
 */

public class WelcomeActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
		finish();
	}
}
