package com.dafukeji.healthcare.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.MyApplication;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.fragment.RecordFragment;
import com.dafukeji.healthcare.util.StatusBar;
import com.dafukeji.healthcare.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements View.OnClickListener{

	private HomeFragment homeFragment;
	private RecordFragment recordFragment;
	private List<Fragment> mFragments;
	private int mIndex=0;

	private RadioButton rbHome,rbRecord;
	private RadioGroup rgTab;
	private Toolbar mToolbar;
	private TextView mTvTitle;
	private ImageView ivController;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		StatusBar.setImmersiveStatusBar(this,mToolbar,R.color.app_bar_color);
		initFragment();
	}

	private void initViews() {
		rbHome= (RadioButton) findViewById(R.id.rb_home);
		rbRecord= (RadioButton) findViewById(R.id.rb_record);
		mTvTitle= (TextView) findViewById(R.id.tv_toolbar_title);

		rgTab= (RadioGroup) findViewById(R.id.rg_tab);

		mToolbar= (Toolbar) findViewById(R.id.toolbar);
		ivController= (ImageView) findViewById(R.id.iv_controller);

		rbHome.setOnClickListener(this);
		rbRecord.setOnClickListener(this);
		ivController.setOnClickListener(this);
	}

	private void initFragment() {
		if (homeFragment==null){
			homeFragment = new HomeFragment();
		}
		if (recordFragment==null){
			recordFragment=new RecordFragment();
		}
		mFragments=new ArrayList<>();
		mFragments.add(homeFragment);
		mFragments.add(recordFragment);
		
		//开始事务
		FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.fl_content, homeFragment).commit();
		//默认设置为第0个
		setIndexSelected(0);
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
//			homeFragment.unBindService();
		}
	}

	public void setIndexSelected(int index) {
		if (mIndex==index){
			return;
		}
		FragmentManager fragmentManager=getSupportFragmentManager();
		FragmentTransaction ft=fragmentManager.beginTransaction();
		//隐藏
		ft.hide(mFragments.get(mIndex));
		//判断是否添加
		if (!mFragments.get(index).isAdded()){
			ft.add(R.id.fl_content,mFragments.get(index)).show(mFragments.get(index));
		}else{
			ft.show(mFragments.get(index));
		}
		ft.commit();
		//再次赋值
		mIndex=index;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.rb_home:
				setIndexSelected(0);
				mTvTitle.setText(rbHome.getText().toString());
				break;
			case R.id.rb_record:
				setIndexSelected(1);
				mTvTitle.setText(rbRecord.getText().toString());
				break;
			case R.id.iv_controller:
				Intent controlIntent=new Intent(this, DeviceScanActivity.class);
				startActivity(controlIntent);
				break;

		}
	}
	
}
