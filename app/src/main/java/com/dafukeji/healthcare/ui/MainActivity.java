package com.dafukeji.healthcare.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.MyApplication;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.fragment.RecordFragment;
import com.dafukeji.healthcare.util.StatusBar;
import com.dafukeji.healthcare.util.ToastUtil;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

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

	private BoomMenuButton mBmbController;
	private String[] content=new String[]{"连接设备","断开连接"};
	private Bitmap mBitmap;

	private BluetoothAdapter mBluetoothLEAdapter;
	private boolean isGATTConnected;
	private BlueToothBroadCast mBlueToothBroadCast;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		StatusBar.setImmersiveStatusBar(this,mToolbar,R.color.app_bar_color);
		initFragment();

		registerGATTReceiver();

		final BluetoothManager bluetoothManager =
				(BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothLEAdapter = bluetoothManager.getAdapter();
	}

	private void registerGATTReceiver() {
		//注册接受蓝牙信息的广播
		mBlueToothBroadCast=new BlueToothBroadCast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constants.RECEIVE_GATT_STATUS);
		registerReceiver(mBlueToothBroadCast,filter);
	}

	class BlueToothBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//得到蓝牙的服务连接
			isGATTConnected= intent.getBooleanExtra(Constants.RECEIVE_GATT_STATUS,false);
			ReinitializeBoom();
		}
	}

	private void initViews() {
		rbHome= (RadioButton) findViewById(R.id.rb_home);
		rbRecord= (RadioButton) findViewById(R.id.rb_record);
		mTvTitle= (TextView) findViewById(R.id.tv_toolbar_title);

		rgTab= (RadioGroup) findViewById(R.id.rg_tab);

		mToolbar= (Toolbar) findViewById(R.id.toolbar);

		rbHome.setOnClickListener(this);
		rbRecord.setOnClickListener(this);


		mBmbController = (BoomMenuButton) findViewById(R.id.boom);
		mBmbController.setButtonBottomMargin(200);
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
	}

	private void ReinitializeBoom() {

		if (isGATTConnected){
			mBmbController.setPiecePlaceEnum(PiecePlaceEnum.DOT_2_1);
			mBmbController.setButtonPlaceEnum(ButtonPlaceEnum.SC_2_1);
		}else{
			mBmbController.setPiecePlaceEnum(PiecePlaceEnum.DOT_1);
			mBmbController.setButtonPlaceEnum(ButtonPlaceEnum.SC_1);
		}

		int[] normalColor=new int[]{Color.parseColor("#4CAF50"),Color.parseColor("#ff0000")};//设置子控件的颜色
		int[] highLightedColor=new int[]{Color.parseColor("#70bf73"),Color.parseColor("#ff5c5c")};//设置子控件点击后的背景颜色
		int[] res=new int[]{R.mipmap.ic_controller_connect,R.mipmap.ic_controller_disconnect};//设置子控件的背景的图片


		for (int i = 0; i < mBmbController.getPiecePlaceEnum().pieceNumber(); i++) {

			mBitmap = BitmapFactory.decodeResource(getResources(), res[i]);
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			int left = (int) (mBmbController.getButtonRadius() - Math.floor(width / 2) + 4);
			int top = (int) (mBmbController.getButtonRadius() - Math.floor(height / 2) + 4);
			int right = (int) (mBmbController.getButtonRadius() + Math.floor(width / 2) + 4);
			int bottom = (int) (mBmbController.getButtonRadius() + Math.floor(height / 2 + 4));
			TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
					.listener(new OnBMClickListener() {
						@Override
						public void onBoomButtonClick(int index) {
							switch (index){
								case 0:
									startActivity(new Intent(MainActivity.this,DeviceScanActivity.class));
									break;
								case 1:
									//断开蓝牙的连接
									if (homeFragment!=null){
										homeFragment.disConnect();
									}
									break;
							}
						}
					})
					.normalImageRes(res[i])
					.textSize(15)
					.buttonRadius(60)
					.normalColor(normalColor[i])
					.highlightedColor(highLightedColor[i])
					.imageRect(new Rect(left, top, right, bottom))//通过此方法来设置图片的位置
					.textHeight(40)
					.normalTextColor(Color.WHITE)
					.normalText(content[i]);

			mBmbController.addBuilder(builder);
		}
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

		}
	}
	
}
