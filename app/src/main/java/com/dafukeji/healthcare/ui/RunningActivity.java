package com.dafukeji.healthcare.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dafukeji.daogenerator.Cure;
import com.dafukeji.daogenerator.CureDao;
import com.dafukeji.daogenerator.DaoMaster;
import com.dafukeji.daogenerator.DaoSession;
import com.dafukeji.daogenerator.Point;
import com.dafukeji.daogenerator.PointDao;
import com.dafukeji.healthcare.BaseActivity;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.bean.Battery;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.ColorArcProgressBar;
import com.dafukeji.healthcare.util.CommonUtils;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.TimeUtil;
import com.dafukeji.healthcare.util.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.dao.InternalUnitTestDaoAccess;
import lecho.lib.hellocharts.formatter.AxisValueFormatter;
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
	private boolean mConnected = false;

	private long mRunningTime = 0;
	private long mOriginalTime;

	private Toolbar mToolbar;
	private ImageView mIvAgain, mIvSpace, mIvOver;
	private ImageView mIvBack;
	private TextView mTvCurrentTemp, mTvReminderTime, mTvToolbarTitle, mTvReminderEle;
	private ColorArcProgressBar mColorArcProgressBar;

	private boolean isOver = false;
	private Handler mHandler;
	private static int delay = 1000;//延迟一秒执行
	private static int period = 1000;

	private byte[] data;

	/*=========== 配置命令相关 ===========*/
	private LinearLayout llCauterize;
	private TextView tvCauterizeGrade, tvNeedleGrade, tvNeedleFrequency;

	/*=========== 控件相关 ==========*/
	private LineChartView mLineChartView;               //线性图表控件

	/*=========== 数据相关 ==========*/
	private LineChartData mLineChartData;               //图表数据
	private int numberOfLines = 1;                      //图上折线/曲线的显示条数
	private int maxNumberOfLines = 4;                   //图上折线/曲线的最多条数
	private int numberOfPoints = 16;                    //图上的节点数

	private int mXDisplayCount =18;//X轴显示的适配个数，适合6个

	/*=========== 其他相关 ==========*/
	private ValueShape pointsShape = ValueShape.CIRCLE; //点的形状(圆/方/菱形)
	float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints]; //将线上的点放在一个数组中


	private int dataCount = 0;//接受到的数据的个数
	private int intervalCount;
	private List<PointValue> mPointValueList = new ArrayList<>();
	private List<PointValue> mCurPointValueList=new ArrayList<>();
	private List<Line> mLinesList = new ArrayList<>();
	private List<AxisValue> mAxisValues = new ArrayList<>();
	private List<AxisValue> mCurAxisValues=new ArrayList<>();
	private static String TAG = "测试RunningActivity";

	private List<Point> points = new ArrayList<>();
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

	private int mReceiveDataCount = 0;
	private int mSum = 0;

	private int mStimulate;//强刺激的类型，3表示关机
	private int mCauterizeGrade = 40;//初始的一档对应的温度
	private int mCauterizeTime = 10;//初始的加热时间
	private int mNeedleType = 1;//默认为按功能
	private int mNeedleGrade = 1;//默认为1档
	private int mNeedleFrequency = 1;//默认为1档
	private int mMedicineTime = 20;//默认为3档20分钟

	private boolean mSendStopCmdFlag;
	private boolean mSendAgainCmdFlag;
	private byte[] configSetting;
	private byte[] configSettingWithoutStimulate;

	//电量相关
	private long mEleSum;
	private long mPreDataTime;
	private long mCurDataTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_running_2);

		mCureType = getIntent().getIntExtra(Constants.CURE_TYPE, 0);
		getDb();

		mOriginalTime = (long) (getIntent().getIntExtra(Constants.ORIGINAL_TIME, 0) * 60 * 1000);//获取的是int类型的分钟数，则需要强转
		LogUtil.i(TAG, "onCreate: mOriginalTime" + mOriginalTime);
		configSetting = getIntent().getByteArrayExtra(Constants.SETTING);

		//获取运行时的时间
		mStartTime = System.currentTimeMillis();

		insertPoint(mStartTime, ConvertUtils.byte2unsignedInt(configSetting[3]));//开始时的温度点

		mBluetoothLeService = HomeFragment.getBluetoothLeService();

		initViews();

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case 0:
						if (mOriginalTime != 0) {
							mColorArcProgressBar.setCurrentValues((float) Math.floor((mRunningTime * 100 / mOriginalTime)));
						}
						String[] remindTime = TimeUtil.getSubtractedString(mOriginalTime, mRunningTime);
						if (remindTime[0].equals("00")) {
							mTvReminderTime.setText(remindTime[1] + "′" + remindTime[2] + "″");
						} else {
							mTvReminderTime.setText(remindTime[0] + "′" + remindTime[1] + "′" + remindTime[2] + "″");
						}

						break;
					case 1:
						mColorArcProgressBar.setCurrentValues(100);
//						mTvReminderTime.setText("00′00′00″");
						mTvReminderTime.setText("00′00″");
						isOver = true;
//						dynamicDataDisplay(System.currentTimeMillis(),0);//结束时能够显示完整波形变动

						mLineChartView.setInteractive(true);

						stopTimer();
						sendStopSettingData();//为了防止与设备之间疗程时间的不同步，在此直接结束疗程
						saveData();//保存数据
						mIvSpace.setVisibility(View.INVISIBLE);
						mIvAgain.setVisibility(View.VISIBLE);
						break;

					case 2://接受温度和电量
						mTvCurrentTemp.setText(msg.arg1 + "℃");
						mTvReminderEle.setText(CommonUtils.getOptimizePerEle(msg.arg2) + "%");
						break;

					case 3://显示图表
						dynamicDataDisplay(mCurrentTime, msg.arg1);
						break;
				}
			}
		};

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

		startConfigTimer();
		new Thread(new MyThread()).start();

		setResult(RESULT_OK);
	}

	private void initViews() {

		/*=========配置命令相关=========*/
		llCauterize = (LinearLayout) findViewById(R.id.ll_cauterize);
		tvCauterizeGrade = (TextView) findViewById(R.id.tv_running_cauterize_grade);
		tvNeedleGrade = (TextView) findViewById(R.id.tv_running_needle_grade);
		tvNeedleFrequency = (TextView) findViewById(R.id.tv_running_needle_frequency);
		if (mCureType == 1) {
			llCauterize.setVisibility(View.VISIBLE);
			tvCauterizeGrade.setText(getCauterizeGrade(configSetting[5]));
		} else {
			llCauterize.setVisibility(View.GONE);
		}

		//根据是否设置了强刺激来对档位进行处理
		if (configSetting[3]!=0||configSetting[4]!=0){
			tvNeedleGrade.setText((configSetting[8]-2) + "档");//档位是从1开始的
			tvNeedleFrequency.setText((configSetting[9]-2) + "档");
		}else{
			tvNeedleGrade.setText((configSetting[8]) + "档");//档位是从1开始的
			tvNeedleFrequency.setText((configSetting[9]) + "档");
		}


		mTvCurrentTemp = (TextView) findViewById(R.id.tv_current_temp);
		mTvCurrentTemp.setText(getIntent().getIntExtra(Constants.CURRENT_TEMP, 0) + "℃");

		mTvReminderTime = (TextView) findViewById(R.id.tv_reminder_time);
		mTvReminderTime.setText(TimeUtil.getTimeString(configSetting[6]+configSetting[10]));

		mTvReminderEle = (TextView) findViewById(R.id.tv_reminder_ele);
		mTvReminderEle.setText(Battery.REMINDER_PER_ELE+"%");

		mTvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
		mTvToolbarTitle.setText(getCureTypeString(mCureType));

		mIvAgain = (ImageView) findViewById(R.id.iv_again);
		mIvSpace = (ImageView) findViewById(R.id.iv_space);
		mIvOver = (ImageView) findViewById(R.id.iv_over);

		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		mIvAgain.setOnClickListener(this);
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

		dynamicDataDisplay(getIntent().getLongExtra(Constants.CURRENT_TIME, 0),0);
	}


	/**
	 * 根据setting中的灸的温度判断档位
	 */
	private String getCauterizeGrade(int temp) {
		String grade = null;
		switch (temp) {
			case 39+2:
				grade = "微热";
				break;
			case 42+2:
				grade = "热";
				break;
			case 45+2:
				grade = "很热";
				break;
			case 48+2:
				grade = "灼热";
				break;
		}
		return grade;
	}


	/**
	 * 获取治疗的类型
	 *
	 * @return 治疗类型的String
	 */
	private String getCureTypeString(int cureType) {
		String type = null;
		switch (cureType) {
			case Constants.CURE_MEDICAL:
				type = getString(R.string.cure_medical);
				break;
			case Constants.CURE_PHYSICAL:
				type = getString(R.string.cure_physical);
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

		Point point = new Point();
		point.setCurrentTime(currentTime);
		point.setTemperature(temp);
		points.add(point);
	}

	private void saveData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (points.size() > 0) {
					mStopTime = System.currentTimeMillis();
					if (mCure == null) {
						mCure = new Cure();
						mCure.setCureType(mCureType);
//						mCure.setStartTime(mStartTime);
						mCure.setStartTime(mStopTime - mRunningTime);//防止不准确采取的方法
						mCure.setStopTime(mStopTime);
						mCureId = mCureDao.insert(mCure);

					}

					for (int i = 0; i < points.size(); i++) {
						points.get(i).setCureId(mCureId);
						mPointDao.insert(points.get(i));
					}
				}
			}
		}).start();
	}


	private class MyThread implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(1000);
					mRunningTime = mRunningTime + 1000;
					Message msg = Message.obtain();
					if (mRunningTime >= mOriginalTime) {//应该==就可以了
						msg.what = 1;
						mHandler.sendMessage(msg);
						break;
					} else {
						msg.what = 0;
						mHandler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//发送传感命令
	private static Timer mTimer;
	private static TimerTask mTimerTask;
	private int retrySensorCount;
	private int mSendConfigCount;
	private boolean isConfigReceived = false;
	private long mSendSensorTime;

	private void startConfigTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					if (!isConfigReceived) {
						if (retrySensorCount >= 6) {
							disconnected();
							return;
						} else {
							retrySensorCount++;
						}
					} else {
						retrySensorCount = 0;
					}

					//根据是否设置了强刺激来进行发送不同的配置领命
					mSendSensorTime = System.currentTimeMillis();
					isConfigReceived = false;
					mSendConfigCount++;
					if (configSetting[3]!=0||configSetting[4]!=0) {
						if (mSendConfigCount<=450){
							if (mBluetoothLeService!=null) {
								mBluetoothLeService.WriteValue(configSetting);
							}
						}else{

							configSettingWithoutStimulate =configSetting;
							configSettingWithoutStimulate[11]= (byte) (configSettingWithoutStimulate[11]
									- configSettingWithoutStimulate[3]- configSettingWithoutStimulate[4]-2-2);//记得校验核改变
							configSettingWithoutStimulate[3]=0;
							configSettingWithoutStimulate[4]=0;
							configSettingWithoutStimulate[8]-=2;
							configSettingWithoutStimulate[9]-=2;

							mBluetoothLeService.WriteValue(configSettingWithoutStimulate);
						}
					}else{
						mBluetoothLeService.WriteValue(configSetting);
					}
				}
			};
		}
		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, 0, 400);
		}
	}


	/**
	 * 设备意外断开时，处于断开状态
	 */
	private void disconnected(){
		stopTimer();
		Intent intent = new Intent();
		intent.putExtra(Constants.EXTRAS_GATT_STATUS, false);
		intent.setAction(Constants.RECEIVE_GATT_STATUS);
		sendBroadcast(intent);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(RunningActivity.this)
						.setMessage("已断开连接，请重新连接")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
//													saveData();//需要在此保存数据 TODO PRIMARY KEY must be unique (code 19)

								if (mBluetoothLeService!=null){
									mBluetoothLeService.disconnect();
								}
								dialog.dismiss();
								stopTimer();
								finish();
							}
						});
				builder.setCancelable(false);
				builder.create().show();
			}
		});
	}

	private int retryStopCount;
	private boolean isStopReceived;

	private void startStopTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					if (!isStopReceived) {
						if (retryStopCount >= 2) {//TODO 发2底层没有应答
//							disconnected();//没有应答则不能在此处使用此方法
							finish();
						} else {
							retryStopCount++;
							if (mSendStopCmdFlag) {
								sendStopSettingData();
							}
						}
					}else{
						retryStopCount=0;
					}
				}
			};
		}
		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, 0, 400);
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
				LogUtil.i(TAG, "Only gatt, just wait");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
				mConnected = false;

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //可以开始干活了
				mConnected = true;
				LogUtil.i(TAG, "In what we need");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据

				mConnected = true;
				data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				LogUtil.i(TAG, "onReceive: " + (data == null ? "data为null" : Arrays.toString(data)));
				if (data != null) {

					if (mSendStopCmdFlag) {
						if (data[2] == 2) {
							retryStopCount = 0;
							isStopReceived = true;
							stopTimer();
							mSendStopCmdFlag = false;
							finish();
						}
					}

					if (data[2] == configSetting[2]) {

						mReceiveDataCount++;

						//目前只计算正常运行时使用的电量
						if (mReceiveDataCount == 1) {
							mPreDataTime = System.currentTimeMillis();
						} else {
							mCurDataTime = System.currentTimeMillis();
							mEleSum += (mCurDataTime - mPreDataTime) * getEle(data[8], data[9]);
							mPreDataTime = mCurDataTime;
							LogUtil.i(TAG, "运行时消耗的电量：" + mEleSum);
						}

						isConfigReceived = true;
						int temp;
						mSum = mSum + ConvertUtils.byte2unsignedInt(data[3]);//10个数据的平均值作为一个显示数据

						mCurrentTime = System.currentTimeMillis();
						if (mReceiveDataCount%10==0){
							//实时点温度
							temp=ConvertUtils.byte2unsignedInt(data[3]);

							Message msg = Message.obtain();
							msg.what = 2;
							msg.arg1 = temp;
							msg.arg2 = (int) Math.ceil((CommonUtils.eleFormula(Battery.ORIGINAL_VOLTAGE) * 3600 * 300 * 10
									- intent.getLongExtra(Constants.USED_ELE, 0)) * 100 / (3600 * 300 * 1000));
							mHandler.sendMessage(msg);

						}

						int gatherFrequency=1;
						if (configSetting[3]!=0||configSetting[4]!=0){
							gatherFrequency=12-ConvertUtils.byte2unsignedInt(data[6]);
							LogUtil.i(TAG,"------------------------->强刺激时：gatherFrequency="+gatherFrequency);
						}else{
							gatherFrequency =10-ConvertUtils.byte2unsignedInt(data[6]);
							LogUtil.i(TAG,"------------------------->无强刺激时：gatherFrequency="+gatherFrequency);
						}
						if (mReceiveDataCount % gatherFrequency == 0||mReceiveDataCount==1) {

							intervalCount++;
							Message msgVoltage = Message.obtain();
							msgVoltage.what = 3;
							int realVoltage=CommonUtils.getRunningVoltageByGrade(ConvertUtils.byte2unsignedInt(data[4]));
							if (intervalCount % 2 == 0) {
//								msgVoltage.arg1 = 0;
								msgVoltage.arg1 =(int)(Math.random()*realVoltage/4);
								LogUtil.i(TAG,"产生的最低点的随机数="+msgVoltage.arg1);
							} else {
								msgVoltage.arg1 = realVoltage-(int)(Math.random()*10);
							}
							mHandler.sendMessage(msgVoltage);
						}
					}
				}
			}
		}
	};


	//通过接收到的数据计算电流的大小
	private int getEle(byte high, byte low) {
		return ConvertUtils.byte2unsignedInt(high) * 256 + ConvertUtils.byte2unsignedInt(low);
	}

	private void dynamicDataDisplay(long currentTime, int grade) {
		if (!isOver) {
			mLineChartView.setInteractive(false);
			PointValue value = new PointValue(dataCount, grade);
			if (mCurPointValueList.size()<=mXDisplayCount){
				mCurPointValueList.add(value);
				mCurAxisValues.add(new AxisValue(dataCount).setLabel(TimeUtil.date2String(currentTime, "mm:ss")));
			}else{
				mCurPointValueList.remove(0);
				mCurPointValueList.add(value);
				mCurAxisValues.remove(0);
				mCurAxisValues.add(new AxisValue(dataCount).setLabel(TimeUtil.date2String(currentTime, "mm:ss")));
			}
			mPointValueList.add(value);
			mAxisValues.add(new AxisValue(dataCount).setLabel(TimeUtil.date2String(currentTime, "mm:ss")));
			dataCount++;
			float x = value.getX();
			mLinesList.clear();
			mLinesList.add(setLine(mCurPointValueList));
			mLineChartData = initDatas(mLinesList);
			mLineChartView.setLineChartData(mLineChartData);
			//根据点的横坐标实时变换坐标的视图范围
			Viewport port;
			port = initViewPort(x - mXDisplayCount, x);
			mLineChartView.setCurrentViewport(port);//当前窗口
			mLineChartView.setMaximumViewport(port);//使用此方法则结束后不能显示出全部的波形

		} else {
			mLineChartView.setInteractive(true);
			float x=mPointValueList.get(mPointValueList.size()-1).getX();

			mLinesList.clear();
			mLinesList.add(setLine(mPointValueList));

			LineChartData lineData = new LineChartData(mLinesList);
			Axis axisX = new Axis();
			Axis axisY = new Axis().setHasLines(true);
			axisX.setTextColor(Color.GRAY);
			axisX.setValues(mAxisValues);
			axisX.setMaxLabelChars(6);
			axisX.setHasLines(true);//x轴分割线

			axisY.setTextColor(Color.GRAY);
			axisX.setName("时间");
			axisY.setName("强度/V");//设置名称
			lineData.setAxisXBottom(axisX);//设置X轴位置 下方
			lineData.setAxisYLeft(axisY);//设置Y轴位置 左边
			mLineChartView.setLineChartData(lineData);
			//根据点的横坐标实时变换坐标的视图范围
			Viewport port;
			if (x > mXDisplayCount) {
				port = initViewPort(x - mXDisplayCount, x);
			} else {
				port = initViewPort(0, mXDisplayCount);
			}
			mLineChartView.setCurrentViewport(port);//当前窗口
			Viewport maxPort = null;
			if (mPointValueList.size()<=mXDisplayCount) {
				maxPort = initMaxViewPort(x,mXDisplayCount);
			}else{
				maxPort=initMaxViewPort(x,0);
			}
			mLineChartView.setMaximumViewport(maxPort);//最大窗口
		}
	}

	private Line setLine(List<PointValue> pointValues){
		Line line;
		line = new Line(pointValues);
		line.setColor(Color.parseColor("#ff0033"));//设置线的颜色
		line.setStrokeWidth(2);//设置线的粗细
		line.setShape(pointsShape);                 //设置点的形状
		line.setPointRadius(2);
		line.setPointColor(Color.parseColor("#006600"));
		line.setHasLines(true);               //设置是否显示线
		line.setHasPoints(true);             //设置是否显示节点
		line.setCubic(true);                     //设置线是否立体或其他效果
		line.setFilled(false);                   //设置是否填充线下方区域
		line.setHasLabels(false);       //设置是否显示节点标签
		//设置节点点击的效果
		line.setHasLabelsOnlyForSelected(true);
		return line;
	}

	private LineChartData initDatas(List<Line> lines) {
		LineChartData lineData = new LineChartData(lines);
		Axis axisX = new Axis();
		Axis axisY = new Axis().setHasLines(true);
		axisX.setTextColor(Color.GRAY);
		axisX.setValues(mCurAxisValues);
//		if (mPointValueList.size()<=mXDisplayCount){
//			axisX.setMaxLabelChars(mXDisplayCount);
//		}else {
//			axisX.setMaxLabelChars(6);
//		}
		axisX.setMaxLabelChars(6);
		axisX.setHasLines(true);//x轴分割线

		axisY.setTextColor(Color.GRAY);
		axisX.setName("时间");
		axisY.setName("强度/V");//设置名称
		lineData.setAxisXBottom(axisX);//设置X轴位置 下方
		lineData.setAxisYLeft(axisY);//设置Y轴位置 左边
		return lineData;
	}

	private Viewport initViewPort(float left, float right) {
		Viewport port = new Viewport();
		port.top = 200;
		port.bottom = 0;//y轴显示的最低值
		port.left = left;
		port.right = right;
		return port;
	}


	//TODO 此方法可以删除
	private Viewport initMaxViewPort(float right,float offset) {
		Viewport port = new Viewport();
		port.top = 200;
		port.bottom = 0;
		port.left = 0;
		port.right = right + offset;
		return port;

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopTimer();
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

		LogUtil.i(TAG, "We are in destroy");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_again:
//				mSendAgainCmdFlag = true;
				isAgain();
				break;
			case R.id.iv_over://实现结束功能(当结束后则直接返回，如果疗程没有进行完则提示)
				mSendStopCmdFlag = true;
				if (mRunningTime < mOriginalTime) {
					stopTimer();
					if (mConnected) {
						startStopTimer();//发送结束疗程的配置数据
					}
					saveData();//此处保存的数据为未做完的
				} else {
					stopTimer();
					finish();
				}
				break;
			case R.id.iv_back:
				mSendStopCmdFlag = true;
				isOver();
				break;
		}
	}

	private void sendStopSettingData() {

		mSendStopCmdFlag = true;

		int stimulate = 2;//停止疗程标志
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
		Log.i(TAG, "中途停止疗程" + Arrays.toString(setting));

		if (HomeFragment.getBluetoothLeService() != null) {//处于连接状态
			LogUtil.i(TAG, "HomeFragment.getBluetoothLeService()" + HomeFragment.getBluetoothLeService());
			HomeFragment.getBluetoothLeService().WriteValue(setting);
		} else {
			//TODO 由于突然断开进行的处理
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
						saveData();//此处保存的数据为未做完的
						if (mConnected) {
							stopTimer();
							startStopTimer();
						}
						dialog.dismiss();
//						finish();
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
	 * 当点击再一次时判断是否重新开始治疗
	 */
	private void isAgain() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("温馨提示")
				.setMessage("确定再进行一个疗程吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mColorArcProgressBar.setCurrentValues((float) Math.floor((mRunningTime * 100 / mOriginalTime)));
						isOver = false;
						//重新发送配置的数据
						stopTimer();

						getDb();//如果要将重新开始的保存为另一份；
						mCure = null;//设置为null,将会重新新建cure对象
						points = null;
						points = new ArrayList<>();
						dialog.dismiss();

						//清空图表
						mPointValueList.clear();

						//初始化数据
						mStartTime = System.currentTimeMillis();
						mTvReminderTime.setText(TimeUtil.getTimeString(configSetting[6]+configSetting[10]));
						mRunningTime=0;
						retrySensorCount=0;
						isConfigReceived=false;
						mSendConfigCount=0;
						retryStopCount=0;
						isStopReceived=false;
						startConfigTimer();
						new Thread(new MyThread()).start();

						mRunningTime = 0;//当点击新开始
						mIvAgain.setVisibility(View.GONE);
						mIvSpace.setVisibility(View.GONE);
						mIvOver.setVisibility(View.VISIBLE);
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


	//TODO 无用的方法可以删去
	private void sendAgainSettingData() {

//		if (CureSPUtil.isSaved(Constants.SP_MEDICAL_STIMULATE, this)) {
//			mStimulate = CureSPUtil.getSP(Constants.SP_MEDICAL_STIMULATE, this);
//		}
//
//		if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_GRADE, this)) {
//			mCauterizeGrade = CureSPUtil.getTempByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_GRADE, this));
//		}
//		if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_TIME_GRADE, this)) {
//			mCauterizeTime = CureSPUtil.getCauterizeTimeByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_TIME_GRADE, this));
//		}
//		if (CureSPUtil.isSaved(Constants.SP_NEEDLE_TYPE, this)) {
//			mNeedleType = CureSPUtil.getSP(Constants.SP_NEEDLE_TYPE, this);
//		}
//		if (CureSPUtil.isSaved(Constants.SP_NEEDLE_GRADE, this)) {
//			mNeedleGrade = CureSPUtil.getSP(Constants.SP_NEEDLE_GRADE, this);
//		}
//		if (CureSPUtil.isSaved(Constants.SP_NEEDLE_FREQUENCY, this)) {
//			mNeedleFrequency = CureSPUtil.getSP(Constants.SP_NEEDLE_FREQUENCY, this);
//		}
//		if (CureSPUtil.isSaved(Constants.SP_MEDICINE_TIME_GRADE, this)) {
//			mMedicineTime = CureSPUtil.getMedicineTimeByPosition(CureSPUtil.getSP(Constants.SP_MEDICINE_TIME_GRADE, this));
//		}
//
//		LogUtil.i(TAG, "发送的数据Settings:" + Arrays.toString(CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
//				, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime)));
//
//		if (HomeFragment.getBluetoothLeService() == null) {
//			return;
//		}
//
//		HomeFragment.getBluetoothLeService().WriteValue(CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
//				, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime));

		if (HomeFragment.getBluetoothLeService() == null) {
			return;
		}
		HomeFragment.getBluetoothLeService().WriteValue(configSetting);
	}


	/**
	 * 节点触摸监听
	 */
	private class ValueTouchListener implements LineChartOnValueSelectListener {
		@Override
		public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
			ToastUtil.showToast(RunningActivity.this, value.getY() + "V", 1000);
		}

		@Override
		public void onValueDeselected() {

		}
	}

	@Override
	public void onBackPressed() {
		mSendStopCmdFlag = true;
		isOver();
//		super.onBackPressed();//调用父类的方法，使得点击返回键时直接返回上一个Activity，去掉此方法则不会直接退出
	}

}
