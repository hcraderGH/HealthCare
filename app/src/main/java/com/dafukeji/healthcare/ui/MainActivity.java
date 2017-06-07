package com.dafukeji.healthcare.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.MyApplication;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.fragment.RecordFragment;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.ToastUtil;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.OnBoomListener;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements View.OnClickListener {

	private HomeFragment homeFragment;
	private RecordFragment recordFragment;
	private List<Fragment> mFragments;
	private int mIndex = 0;

	private RadioButton rbHome, rbRecord;
	private RadioGroup rgTab;
	private Toolbar mToolbar;
	private TextView mTvTitle;

	private BoomMenuButton mBmbController;
	private String[] content = new String[]{"连接设备", "断开连接"};
	private Bitmap mBitmap;
	private boolean isBoomShowing = false;

	private BluetoothAdapter mBluetoothLEAdapter;
	private boolean isGATTConnected = false;
//	private BlueToothBroadCast mBlueToothBroadCast;

	private DrawerLayout mDrawer;
	private ActionBarDrawerToggle mDrawerToggle;

	private MaterialRippleLayout mrlExit;
	private MaterialRippleLayout mrlShare;
	private MaterialRippleLayout mrlSetting;
	private MaterialRippleLayout mrlHome;
	private static String TAG = "测试MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		setListeners();
		initFragment();

//		registerGATTReceiver();

		final BluetoothManager bluetoothManager =
				(BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothLEAdapter = bluetoothManager.getAdapter();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mDrawerToggle.onOptionsItemSelected(item) ||
				super.onOptionsItemSelected(item);
	}

//	private void registerGATTReceiver() {
//		//注册接受蓝牙信息的广播
//		mBlueToothBroadCast=new BlueToothBroadCast();
//		IntentFilter filter=new IntentFilter();
//		filter.addAction(Constants.RECEIVE_BLUETOOTH_INFO);
//		registerReceiver(mBlueToothBroadCast,filter);
//	}
//
//	class BlueToothBroadCast extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			//得到蓝牙的服务连接
//			isGATTConnected= intent.getBooleanExtra(Constants.EXTRAS_GATT_STATUS,false);
////			ReinitializeBoom();
//		}
//	}

	private void initViews() {

		rbHome = (RadioButton) findViewById(R.id.rb_home);
		rbRecord = (RadioButton) findViewById(R.id.rb_record);
		mTvTitle = (TextView) findViewById(R.id.tv_toolbar_title);

		rgTab = (RadioGroup) findViewById(R.id.rg_tab);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);


		mDrawer = (DrawerLayout) findViewById(R.id.draw_layout);
		//设置左上角导航抽屉图标
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar
				, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {//如果实现的方式中没有ToolBar参数，那么在ToolBar中就不会显示图标
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				super.onDrawerOpened(drawerView);
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				super.onDrawerStateChanged(newState);
			}
		};
		mDrawer.setDrawerListener(mDrawerToggle);

		mBmbController = (BoomMenuButton) findViewById(R.id.boom);
		mBmbController.setButtonBottomMargin(400);
		mBmbController.setDotRadius(0);//BoomMenuButton上的点大小设置为0，则表示不显示
		mBmbController.setNormalColor(Color.parseColor("#00000000"));//设置BoomMenuButton的背景颜色，设置为全透明则不显示
		mBmbController.setBackgroundEffect(false);//设置是否显示BoomMenuButton的背景阴影
		mBmbController.setDimColor(getResources().getColor(R.color.cover_color));
		mBmbController.setShowDuration(400);
		mBmbController.setShowDelay(100);//设置Boom按钮的展示的延迟
		mBmbController.setHideDuration(400);

		mBmbController.setPiecePlaceEnum(PiecePlaceEnum.DOT_1);
		mBmbController.setButtonPlaceEnum(ButtonPlaceEnum.SC_1);

		ReinitializeBoom();

		mrlExit = (MaterialRippleLayout) mDrawer.findViewById(R.id.mrl_exit);
		mrlHome = (MaterialRippleLayout) mDrawer.findViewById(R.id.mrl_home);
		mrlShare = (MaterialRippleLayout) mDrawer.findViewById(R.id.mrl_share);
		mrlSetting = (MaterialRippleLayout) mDrawer.findViewById(R.id.mrl_settings);
	}

	private void setListeners() {
		rbHome.setOnClickListener(this);
		rbRecord.setOnClickListener(this);

		mrlExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawer.closeDrawers();
//				new MaterialDialog.Builder(MainActivity.this)
//						.content("确定退出本程序吗？")
//						.positiveText(R.string.dialog_ok)
//						.negativeText(R.string.dialog_cancel)
//						.onPositive(new MaterialDialog.SingleButtonCallback() {
//							@Override
//							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//								if (isGATTConnected){
//									int stimulate=3;//关机标志
//									int stimulateGrade=0;
//									int stimulateFrequency=0;
//									int cauterizeGrade=0;
//									int cauterizeTime=0;
//									int needleType=0;
//									int needleGrade=0;
//									int needleFrequency=0;
//									int medicineTime=0;
//									int crc=stimulate+stimulateGrade+stimulateFrequency+cauterizeGrade+cauterizeTime
//											+needleType+needleGrade+needleFrequency+medicineTime;
//
//									byte[] setting=new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) stimulate, (byte) stimulateGrade
//											, (byte) stimulateFrequency, (byte) cauterizeGrade, (byte) cauterizeTime, (byte)needleType, (byte) needleGrade
//											,(byte)needleFrequency,(byte)medicineTime,(byte)crc};
//									Log.i(TAG, "onClick: off"+ Arrays.toString(setting));
//									DeviceScanActivity.getBluetoothLeService().WriteValue(setting);
//								}
//								MyApplication.getInstance().exit();
//							}
//						}).show();

				if (isGATTConnected) {
					int stimulate = 3;//关机标志
					int stimulateGrade = 0;
					int stimulateFrequency = 0;
					int cauterizeGrade = 0;
					int cauterizeTime = 0;
					int needleType = 0;
					int needleGrade = 0;
					int needleFrequency = 0;
					int medicineTime = 0;
					int crc = stimulate + stimulateGrade + stimulateFrequency + cauterizeGrade + cauterizeTime
							+ needleType + needleGrade + needleFrequency + medicineTime;

					byte[] setting = new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) stimulate, (byte) stimulateGrade
							, (byte) stimulateFrequency, (byte) cauterizeGrade, (byte) cauterizeTime, (byte) needleType, (byte) needleGrade
							, (byte) needleFrequency, (byte) medicineTime, (byte) crc};
					Log.i(TAG, "onClick: off" + Arrays.toString(setting));
					DeviceScanActivity.getBluetoothLeService().WriteValue(setting);
				}
				MyApplication.getInstance().exit();
			}
		});
		mrlHome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawer.closeDrawers();
				setIndexSelected(0);
			}
		});
		mrlSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawer.closeDrawers();
				startActivity(new Intent(MainActivity.this, SettingActivity.class));
			}
		});
		mrlShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrawer.closeDrawers();
				startActivity(new Intent(MainActivity.this, ShareActivity.class));
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rb_home:
				setIndexSelected(0);
				mTvTitle.setText(rbHome.getText().toString());
				break;
			case R.id.rb_record:
				setIndexSelected(1);
				mTvTitle.setText(rbRecord.getText().toString());
				break;
		}
	}


	private void ReinitializeBoom() {

//		if (isGATTConnected){
//			mBmbController.setPiecePlaceEnum(PiecePlaceEnum.DOT_2_1);
//			mBmbController.setButtonPlaceEnum(ButtonPlaceEnum.SC_2_1);
//		}else{
//			mBmbController.setPiecePlaceEnum(PiecePlaceEnum.DOT_1);
//			mBmbController.setButtonPlaceEnum(ButtonPlaceEnum.SC_1);
//		}

//		mBmbController.setPiecePlaceEnum(PiecePlaceEnum.DOT_2_1);
//		mBmbController.setButtonPlaceEnum(ButtonPlaceEnum.SC_2_1);

		int[] normalColor = new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#ff0000")};//设置子控件的颜色
		int[] highLightedColor = new int[]{Color.parseColor("#70bf73"), Color.parseColor("#ff5c5c")};//设置子控件点击后的背景颜色
		int[] res = new int[]{R.mipmap.ic_controller_connect, R.mipmap.ic_controller_disconnect};//设置子控件的背景的图片


		//Boom事件响应的顺序onBoomWillShow，onBoomDidShow，onClicked[onBackgroundClick],onBoomWillHide,onBoomDidHide
		mBmbController.setOnBoomListener(new OnBoomListener() {
			@Override
			public void onClicked(int index, BoomButton boomButton) {
				//子控件的点击响应事件
//				Toast.makeText(MainActivity.this,"onClicked",Toast.LENGTH_LONG).show();
			}

			@Override
			public void onBackgroundClick() {
				//子控件之外的背景的点击响应事件
//				Toast.makeText(MainActivity.this,"onBackgroundClick",Toast.LENGTH_LONG).show();
			}

			@Override
			public void onBoomWillHide() {
//				Toast.makeText(MainActivity.this,"onBoomWillHide",Toast.LENGTH_LONG).show();
			}

			@Override
			public void onBoomDidHide() {
				isBoomShowing = false;
//				Toast.makeText(MainActivity.this,"onBoomDidHide",Toast.LENGTH_LONG).show();
			}

			@Override
			public void onBoomWillShow() {
//				Toast.makeText(MainActivity.this,"onBoomWillShow",Toast.LENGTH_LONG).show();
			}

			@Override
			public void onBoomDidShow() {
				isBoomShowing = true;
//				Toast.makeText(MainActivity.this,"onBoomDidShow",Toast.LENGTH_LONG).show();
			}
		});

		for (int i = 0; i < mBmbController.getPiecePlaceEnum().pieceNumber(); i++) {

			mBitmap = BitmapFactory.decodeResource(getResources(), res[i]);
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			int left = (int) (Math.max(width, height) - Math.floor(width / 2));
			int top = (int) (Math.max(width, height) - Math.floor(height / 2));
			int right = (int) (Math.max(width, height) + Math.floor(width / 2));
			int bottom = (int) (Math.max(width, height) + Math.floor(height / 2));
			TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
					.listener(new OnBMClickListener() {
						@Override
						public void onBoomButtonClick(int index) {
							switch (index) {
								case 0:
									startActivity(new Intent(MainActivity.this, DeviceScanActivity.class));
									break;
								case 1:
									//TODO 断开蓝牙的连接

									break;
							}
						}
					})
					.normalImageRes(res[i])
					.textSize(15)
					.buttonRadius(Math.max(width, height))
					.normalColor(normalColor[i])
					.highlightedColor(highLightedColor[i])
					.imageRect(new Rect(left, top, right, bottom))//通过此方法来设置图片的位置
					.textHeight(100)
					.normalTextColor(Color.WHITE)
					.normalText(content[i]);
			mBmbController.addBuilder(builder);
		}
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	private void initFragment() {
		if (homeFragment == null) {
			homeFragment = new HomeFragment();
		}
		if (recordFragment == null) {
			recordFragment = new RecordFragment();
		}
		mFragments = new ArrayList<>();
		mFragments.add(homeFragment);
		mFragments.add(recordFragment);

		//开始事务
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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

		if (!isBoomShowing) {
			if (!isExit) {
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


				//当退出程序的时候，关闭蓝牙服务
				Intent intent = new Intent(this, BluetoothLeService.class);
				stopService(intent);

				//关闭蓝牙
				if (mBluetoothLEAdapter != null && mBluetoothLEAdapter.isEnabled()) {
					mBluetoothLEAdapter.disable();
					mBluetoothLEAdapter = null;
				}

				MyApplication.getInstance().exit();
			}
		}

		if (mDrawer.isDrawerOpen(GravityCompat.START)) {
			mDrawer.closeDrawers();
		}

//		super.onBackPressed();//此处不能调用父类的方法，否则直接退出程序
	}

	public void setIndexSelected(int index) {//TODO 重新优化方法

		switch (index) {
			case 0:
				rbHome.setChecked(true);
				break;
			case 1:
				rbRecord.setChecked(true);
				break;
		}

		if (mIndex == index) {
			return;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		if (index == 1) {
			recordFragment = null;
			recordFragment = new RecordFragment();
			mFragments.set(index, recordFragment);
		}

		//隐藏
		ft.hide(mFragments.get(mIndex));
		//判断是否添加
		if (!mFragments.get(index).isAdded()) {
			ft.add(R.id.fl_content, mFragments.get(index)).show(mFragments.get(index));
		} else {
			ft.show(mFragments.get(index));
		}
		ft.commit();
		//再次赋值
		mIndex = index;
	}


	@Override
	protected void onDestroy() {
//		unregisterReceiver(mBlueToothBroadCast);
		super.onDestroy();
	}
}
