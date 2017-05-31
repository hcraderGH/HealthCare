package com.dafukeji.healthcare.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dafukeji.daogenerator.Cure;
import com.dafukeji.daogenerator.CureDao;
import com.dafukeji.daogenerator.DaoMaster;
import com.dafukeji.daogenerator.DaoSession;
import com.dafukeji.daogenerator.Point;
import com.dafukeji.daogenerator.PointDao;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.ColorArcProgressBar;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.StatusBar;
import com.dafukeji.healthcare.util.TimeUtil;
import com.dafukeji.healthcare.util.ToastUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by DevCheng on 2017/4/21.
 */

public class RunningActivity extends BaseActivity implements View.OnClickListener {

	private BluetoothAdapter mBluetoothLEAdapter;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
	private boolean mConnected = false;

	private long mRunningTime = 0;
	private long mOriginalTime;

	private Toolbar mToolbar;
	private ImageView mIvPause, mIvContinue, mIvOver;
	private ImageView mIvBack;
	private TextView mTvCurrentTemp, mTvReminderTime, mTvToolbarTitle;
	private ColorArcProgressBar mColorArcProgressBar;

	private boolean isOver = false;
	private TimerTask mTimerTask;
	private Timer mTimer;
	private Handler mHandler;
	private static int delay = 1000;//延迟一秒执行
	private static int period = 1000;

	private byte[] mData;

	/*=========== 控件相关 ==========*/
	private LineChartView mLineChartView;               //线性图表控件

	/*=========== 数据相关 ==========*/
	private LineChartData mLineChartData;               //图表数据
	private int numberOfLines = 1;                      //图上折线/曲线的显示条数
	private int maxNumberOfLines = 4;                   //图上折线/曲线的最多条数
	private int numberOfPoints = 12;                    //图上的节点数

	private int mXDisplayCount = 6;//X轴显示的适配个数

	/*=========== 其他相关 ==========*/
	private ValueShape pointsShape = ValueShape.CIRCLE; //点的形状(圆/方/菱形)
	float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints]; //将线上的点放在一个数组中

	private int dataCount = 0;//接受到的数据的个数
	private boolean mIsFinish = false;
	private List<PointValue> mPointValueList = new ArrayList<>();
	private List<Line> mLinesList = new ArrayList<>();
	private List<AxisValue> mAxisValues = new ArrayList<>();
	private static String TAG="测试RunningActivity";

	private DaoMaster mMaster;
	private DaoSession mSession;
	private DaoMaster.DevOpenHelper mHelper;
	private PointDao mPointDao;
	private CureDao mCureDao;
	private SQLiteDatabase mDb;
	private int mCureType;
	private Cure mCure;
	private long mStartTime;
	private long mStopTime;
	private long mCureId;

	private long mCurrentTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_running);

		mCureType = getIntent().getIntExtra(Constants.CURE_TYPE, 0);
		getDb();

		mOriginalTime = (long)(getIntent().getIntExtra(Constants.ORIGINAL_TIME, 0)*60*1000);//获取的是int类型的分钟数，则需要强转
		Logger.i("onCreate: mOriginalTime"+mOriginalTime);

		//获取运行时的时间
		mStartTime = System.currentTimeMillis();

		initViews();
//		StatusBar.setImmersiveStatusBar(this, mToolbar, R.color.app_bar_color);
//		dynamicDataDisplay();//TODO 测试使用
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case 0:
						Logger.d("handleMessage: mRunningTime" + mRunningTime);
						Logger.d("handleMessage: pre" + (float) Math.floor((mRunningTime * 100 / mOriginalTime)));
						if (mOriginalTime != 0) {
							mColorArcProgressBar.setCurrentValues((float) Math.floor((mRunningTime * 100 / mOriginalTime)));
						}
						String[] remindTime = TimeUtil.getSubtractedString(mOriginalTime, mRunningTime);
						mTvReminderTime.setText(remindTime[0] + "′" + remindTime[1] + "′" + remindTime[2] + "″");
						break;
					case 1:
						isOver = true;
						mIvPause.setVisibility(View.INVISIBLE);
						mIvContinue.setVisibility(View.VISIBLE);
						mIvOver.setVisibility(View.VISIBLE);
						stopTimer();
						break;

					case 2://接受温度
						mTvCurrentTemp.setText(msg.arg1+"℃");
						break;

					case 3://显示图表
						dynamicDataDisplay(mCurrentTime, msg.arg1);
						break;


				}
			}
		};

		startTimer();

		// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothLEAdapter = bluetoothManager.getAdapter();


		// 若蓝牙没打开
		if (!mBluetoothLEAdapter.isEnabled()) {
			mBluetoothLEAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
		}

//		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//		Logger.d( "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));//已经在HomeFragment中进行绑定服务了
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	private void initViews() {
		mTvCurrentTemp = (TextView) findViewById(R.id.tv_current_temp);
		mTvReminderTime = (TextView) findViewById(R.id.tv_reminder_time);

		mTvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
		mTvToolbarTitle.setText(getCureTypeString(mCureType));

		mIvPause = (ImageView) findViewById(R.id.iv_pause);
		mIvContinue = (ImageView) findViewById(R.id.iv_continue);
		mIvOver = (ImageView) findViewById(R.id.iv_over);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		mIvPause.setOnClickListener(this);
		mIvContinue.setOnClickListener(this);
		mIvOver.setOnClickListener(this);
		mIvBack.setOnClickListener(this);

		mColorArcProgressBar = (ColorArcProgressBar) findViewById(R.id.cpb_reminder_pre);

		mLineChartView = (LineChartView) findViewById(R.id.line_chart);
		/**
		 * 禁用视图重新计算 主要用于图表在变化时动态更改，不是重新计算
		 * 类似于ListView中数据变化时，只需notifyDataSetChanged()，而不用重新setAdapter()
		 */
		mLineChartView.setViewportCalculationEnabled(false);
		mLineChartView.setOnValueTouchListener(new ValueTouchListener());

		dynamicDataDisplay(System.currentTimeMillis(),0);
	}


	/**
	 * 获取治疗的类型
	 *
	 * @return 治疗类型的String
	 */
	private String getCureTypeString(int cureType) {
		String type = null;
		switch (cureType) {
			case Constants.CURE_CAUTERIZE:
				type = getString(R.string.cure_cauterize);
				break;
			case Constants.CURE_NEEDLE:
				type = getString(R.string.cure_needle);
				break;
			case Constants.CURE_MEDICINE:
				type = getString(R.string.cure_medicine);
				break;
		}
		return type;
	}


	/**
	 * 获取greenDao创建的数据库
	 */
	private void getDb() {
		mHelper = new DaoMaster.DevOpenHelper(this, "treat.db", null);
		mDb = mHelper.getWritableDatabase();
		mMaster = new DaoMaster(mDb);
		mSession = mMaster.newSession();

		mPointDao = mSession.getPointDao();
		mCureDao = mSession.getCureDao();
	}

	private void insertPoint(long currentTime, float temp) {

		if (mCure == null) {
			mCure = new Cure();
			mCure.setCureType(mCureType);
			mCure.setStartTime(mStartTime);
			mCureId = mCureDao.insert(mCure);
		}

		Point point = new Point();
		point.setCurrentTime(currentTime);
		point.setTemperature(temp);
		point.setCureId(mCureId);
		mPointDao.insert(point);
	}

	private void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {

					Message msg = Message.obtain();
					if (mRunningTime == mOriginalTime) {
						msg.what = 1;
					} else {
						msg.what = 0;
					}
					mRunningTime = mRunningTime + 1000;
					mHandler.sendMessage(msg);
				}
			};
		}

		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, delay, period);
		}
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	//注册接收的事件
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothDevice.ACTION_UUID);
		return intentFilter;
	}


	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
				LogUtil.i(TAG,"Only gatt, just wait");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				mConnected = false;
				//TODO 断开连接处理
				Toasty.error(RunningActivity.this, "与设备断开连接", Toast.LENGTH_SHORT).show();

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //可以开始干活了
				mConnected = true;
				LogUtil.i(TAG, "In what we need");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
				LogUtil.i(TAG, "DATA");
				mData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				if (mData != null) {
					//TODO 接收数据处理

					LogUtil.i(TAG,"RunningActivity onReceive: mData" + Arrays.toString(mData));//TODO 将接受到的数据显示在图表中

					//当校验码前面的数据相加不等于校验码时表示数据错误
					if (!(mData[0] + mData[1] + mData[2] + mData[3] + mData[4] + mData[5] + mData[6] + mData[7]== mData[8])) {
						return;
					}

					mCurrentTime= System.currentTimeMillis();
					insertPoint(mCurrentTime, mData[4]);


					int temp= ConvertUtils.byte2unsignedInt(mData[3]);//无符号位转换
					Message msg=Message.obtain();
					msg.what=2;
					msg.arg1=temp;
					mHandler.sendMessage(msg);

					Message msgTemp=Message.obtain();
					msgTemp.what=3;
					msgTemp.arg1=temp;
					mHandler.sendMessage(msgTemp);

//					//TODO 测试添加到数据库中的数据
//					List<Point> points=mCureDao.queryBuilder().list().get((int) mCureDao.queryBuilder().count()-1).getPoints();
//					Logger.v( "onReceive: point的个数"+points.size());
//					for (Point point : points) {
//						Logger.v( "onReceive: point时间："+point.getCurrentTime()+"温度："+point.getTemperature());
//					}
//					Logger.v( "onReceive: cure的个数"+mCureDao.queryBuilder().count());

//					dynamicDataDisplay();//TODO 目前测试不了
//					tvCurrentTemp.setText(ConvertUtils.bytes2HexString(data)+"℃");//TODO 注意此处获取的数据
				}
			}
		}
	};

//	private Timer tTimer=new Timer();
//	private Random random=new Random();
//	private int position=0;
//	/**
//	 * 视差使数据看起来为动态加载（心电图效果）
//	 */
//	private void dynamicDataDisplay() {
//		mLineChartView.setInteractive(false);
//		tTimer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				if(!mIsFinish){
////					PointValue value=new PointValue(dataCount,mData[0]);
//
//					PointValue value = new PointValue(position, 40 + random.nextInt(20));
//					mPointValueList.add(value);
//					mAxisValues.add(new AxisValue(dataCount).setLabel(TimeUtil.date2String(System.currentTimeMillis(),"MM:ss")));
//					dataCount++;
//					float x=value.getX();
//					Line line = new Line(mPointValueList);
//					line.setColor(Color.RED);//设置线的颜色
//					line.setStrokeWidth(2);//设置线的粗细
//					line.setShape(pointsShape);                 //设置点的形状
//					line.setPointRadius(3);
//					line.setPointColor(Color.GREEN);
//					line.setHasLines(true);               //设置是否显示线
//					line.setHasPoints(true);             //设置是否显示节点
//					line.setCubic(true);                     //设置线是否立体或其他效果
//					line.setFilled(true);                   //设置是否填充线下方区域
//					line.setHasLabels(false);       //设置是否显示节点标签
//					//设置节点点击的效果
//					line.setHasLabelsOnlyForSelected(true);
//
//					mLinesList.clear();
//					mLinesList.add(line);
//					mLineChartData = initDatas(mLinesList);
//					mLineChartView.setLineChartData(mLineChartData);
//					//根据点的横坐标实时变换坐标的视图范围
//					Viewport port;
//					if (x > mXDisplayCount) {
//						port = initViewPort(x - mXDisplayCount, x);
//					} else {
//						port = initViewPort(0, mXDisplayCount);
//					}
//					mLineChartView.setCurrentViewport(port);//当前窗口
//
//					Viewport maPort = initMaxViewPort(x);
//					mLineChartView.setMaximumViewport(maPort);//最大窗口
//
//					//TODO 测试
//					position++;
//					if (position > 50- 1) {
//						mIsFinish = true;
//						mLineChartView.setInteractive(true);
//					}
//				}
//			}
//		},300,300);
//	}


	private void dynamicDataDisplay(long currentTime, int temp) {
		if (!isOver) {
			mLineChartView.setInteractive(false);
			PointValue value = new PointValue(dataCount, temp);
			mPointValueList.add(value);
			mAxisValues.add(new AxisValue(dataCount).setLabel(TimeUtil.date2String(currentTime, "MM:ss")));
			dataCount++;
			float x = value.getX();
			Line line = new Line(mPointValueList);
			line.setColor(Color.RED);//设置线的颜色
			line.setStrokeWidth(2);//设置线的粗细
			line.setShape(pointsShape);                 //设置点的形状
			line.setPointRadius(3);
			line.setPointColor(Color.GREEN);
			line.setHasLines(true);               //设置是否显示线
			line.setHasPoints(true);             //设置是否显示节点
			line.setCubic(true);                     //设置线是否立体或其他效果
			line.setFilled(true);                   //设置是否填充线下方区域
			line.setHasLabels(false);       //设置是否显示节点标签
			//设置节点点击的效果
			line.setHasLabelsOnlyForSelected(true);

			mLinesList.clear();
			mLinesList.add(line);
			mLineChartData = initDatas(mLinesList);
			mLineChartView.setLineChartData(mLineChartData);
			//根据点的横坐标实时变换坐标的视图范围
			Viewport port;
			if (x > mXDisplayCount) {
				port = initViewPort(x - mXDisplayCount, x);
			} else {
				port = initViewPort(0, mXDisplayCount);
			}
			mLineChartView.setCurrentViewport(port);//当前窗口

			Viewport maPort = initMaxViewPort(x);
			mLineChartView.setMaximumViewport(maPort);//最大窗口

		} else {
			mLineChartView.setInteractive(true);
		}
	}


	private LineChartData initDatas(List<Line> lines) {
		LineChartData lineData = new LineChartData(lines);
		Axis axisX = new Axis();
		Axis axisY = new Axis().setHasLines(true);
		axisX.setTextColor(Color.GRAY);
		axisX.setValues(mAxisValues);
		axisX.setMaxLabelChars(8);
		axisY.setTextColor(Color.GRAY);
		axisX.setName("时间");
		axisY.setName("温度/℃");//设置名称
		lineData.setAxisXBottom(axisX);//设置X轴位置 下方
		lineData.setAxisYLeft(axisY);//设置Y轴位置 左边
		return lineData;
	}

	private Viewport initViewPort(float left, float right) {
		Viewport port = new Viewport();
		port.top = 200;
		port.bottom = 20;//y轴显示的最低值
		port.left = left;
		port.right = right;
		return port;
	}

	private Viewport initMaxViewPort(float right) {
		Viewport port = new Viewport();
		port.top = 200;
		port.bottom = 0;
		port.left = 0;
		port.right = right + 50;
		return port;
	}

	// Code to manage Service lifecycle.
//	private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName componentName, IBinder service) {
//			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//			if (!mBluetoothLeService.initialize()) {
//				Logger.e( "Unable to initialize Bluetooth");
//				finish();
//			}
//
//			Logger.e( "mBluetoothLeService is okay");
//			// Automatically connects to the device upon successful start-up initialization.
//			//mBluetoothLeService.connect(mDeviceAddress);
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName componentName) {
//			mBluetoothLeService = null;
//		}
//	};


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler = null;
		}
		this.unregisterReceiver(mGattUpdateReceiver);

		//TODO 在此处不能够解绑Service否则将会kill掉
//		unbindService(mServiceConnection);
//		if (mBluetoothLeService != null) {
//			mBluetoothLeService.close();
//			mBluetoothLeService = null;
//		}
//		if (mBluetoothLEAdapter != null) {
//			mBluetoothLEAdapter.disable();
//		}

		LogUtil.i(TAG,"We are in destroy");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_pause:
				stopTimer();
				mIvPause.setVisibility(View.INVISIBLE);
				mIvContinue.setVisibility(View.VISIBLE);
				mIvOver.setVisibility(View.VISIBLE);
				break;
			case R.id.iv_continue:
				if (isOver) {
					mRunningTime = 0;//当点击继续时从新开始
					mColorArcProgressBar.setCurrentValues((float) Math.floor((mRunningTime * 100 / mOriginalTime)));
					isOver = false;
				}
				startTimer();
				mIvPause.setVisibility(View.VISIBLE);
				mIvContinue.setVisibility(View.INVISIBLE);
				mIvOver.setVisibility(View.INVISIBLE);
				break;
			case R.id.iv_over://实现结束功能(当结束后则直接返回，如果疗程没有进行完则提示)
				if (mRunningTime == mOriginalTime) {
					finish();
				} else {
					isOver();
				}
				break;
			case R.id.iv_back:
				isOver();
				break;
		}
	}


	/**
	 * 当点击返回时判断是否结束治疗
	 */
	private void isOver() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("温馨提示")
				.setMessage("确定停止疗程，返回主页面吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						stopTimer();
						mCure.setStopTime(System.currentTimeMillis());//当结束的时候
						mCureDao.insertOrReplace(mCure);
						dialog.dismiss();
						finish();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}


	/**
	 * 节点触摸监听
	 */
	private class ValueTouchListener implements LineChartOnValueSelectListener {
		@Override
		public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
			ToastUtil.showToast(RunningActivity.this, value.getY() + "℃", 1000);
		}

		@Override
		public void onValueDeselected() {

		}
	}

	@Override
	public void onBackPressed() {
		isOver();
//		super.onBackPressed();//调用父类的方法，使得点击返回键时直接返回上一个Activity，去掉此方法则不会直接退出
	}
}
