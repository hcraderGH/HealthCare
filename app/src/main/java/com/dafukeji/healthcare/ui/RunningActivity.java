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
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.fragment.HomeFragment;
import com.dafukeji.healthcare.service.BluetoothLeService;
import com.dafukeji.healthcare.util.ColorArcProgressBar;
import com.dafukeji.healthcare.util.ConvertUtils;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.TimeUtil;
import com.dafukeji.healthcare.util.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
	private ImageView mIvAgain, mIvSpace, mIvOver;
	private ImageView mIvBack;
	private TextView mTvCurrentTemp, mTvReminderTime, mTvToolbarTitle;
	private ColorArcProgressBar mColorArcProgressBar;

	private boolean isOver = false;
	private Handler mHandler;
	private static int delay = 1000;//延迟一秒执行
	private static int period = 1000;

	private byte[] data;

	/*=========== 配置命令相关 ===========*/
	private LinearLayout llCauterize;
	private TextView tvCauterizeGrade,tvNeedleGrade,tvNeedleRequency;

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
	private List<PointValue> mPointValueList = new ArrayList<>();
	private List<Line> mLinesList = new ArrayList<>();
	private List<AxisValue> mAxisValues = new ArrayList<>();
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


	private byte[] frontData;
	private byte[] wholeData;
	private byte[] configSetting;

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
		configSetting=getIntent().getByteArrayExtra(Constants.SETTING);

		//获取运行时的时间
		mStartTime = System.currentTimeMillis();

		mBluetoothLeService=HomeFragment.getBluetoothLeService();

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
						if(remindTime[0].equals("00")){
							mTvReminderTime.setText(remindTime[1] + "′" + remindTime[2] + "″");
						}else{
							mTvReminderTime.setText(remindTime[0] + "′" + remindTime[1] + "′" + remindTime[2] + "″");
						}

						break;
					case 1:
						mColorArcProgressBar.setCurrentValues(100);
//						mTvReminderTime.setText("00′00′00″");
						mTvReminderTime.setText("00′00″");
						isOver = true;
						stopTimer();
						sendStopSettingData();//为了防止与设备之间疗程时间的不同步，在此直接结束疗程
						saveData();//保存数据
						mIvSpace.setVisibility(View.INVISIBLE);
						mIvAgain.setVisibility(View.VISIBLE);
						break;

					case 2://接受温度
						mTvCurrentTemp.setText(msg.arg1 + "℃");
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
	}

	private void initViews() {



		/*=========配置命令相关=========*/
		llCauterize= (LinearLayout) findViewById(R.id.ll_cauterize);
		tvCauterizeGrade=(TextView)findViewById(R.id.tv_running_cauterize_grade);
		tvNeedleGrade=(TextView)findViewById(R.id.tv_running_needle_grade);
		tvNeedleRequency=(TextView)findViewById(R.id.tv_running_needle_frequency);
		if (mCureType==1){
			llCauterize.setVisibility(View.VISIBLE);
			tvCauterizeGrade.setText(getCauterizeGrade(configSetting[5]));
		}else{
			llCauterize.setVisibility(View.GONE);
		}
		tvNeedleGrade.setText((configSetting[8]+1)+"档");//档位是从0开始的，显示是从1开始的
		tvNeedleRequency.setText((configSetting[9]+1)+"档");


		mTvCurrentTemp = (TextView) findViewById(R.id.tv_current_temp);
		mTvReminderTime = (TextView) findViewById(R.id.tv_reminder_time);

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


		LogUtil.i(TAG, "接受到的温度：" + getIntent().getIntExtra(Constants.CURRENT_TEMP, 0));
		dynamicDataDisplay(getIntent().getLongExtra(Constants.CURRENT_TIME, 0), getIntent().getIntExtra(Constants.CURRENT_TEMP, 0));
	}


	/**
	 * 根据setting中的灸的温度判断档位
	 */
	private String getCauterizeGrade(int temp){
		String grade = null;
		switch (temp){
			case 39:
				grade="微热";
				break;
			case 42:
				grade="热";
				break;
			case 45:
				grade="很热";
				break;
			case 48:
				grade="灼热";
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
						mCure.setStartTime(mStopTime-mRunningTime);//防止不准确采取的方法
						mCure.setStopTime(mStopTime);
						mCureId = mCureDao.insert(mCure);

						LogUtil.i(TAG, "当点击again时mCureID会改变" + mCureId);
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
				while (true){
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
	private boolean isConfigReceived =false;
	private long mSendSensorTime;
	private void startConfigTimer(){
		if (mTimer==null){
			mTimer=new Timer();
		}
		if (mTimerTask==null){
			mTimerTask=new TimerTask() {
				@Override
				public void run() {
					if (!isConfigReceived){
						if (retrySensorCount>=6) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									AlertDialog.Builder builder = new AlertDialog.Builder(RunningActivity.this)
											.setMessage("已断开连接，请重新连接")
											.setPositiveButton("确定", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
//													saveData();//需要在此保存数据 TODO PRIMARY KEY must be unique (code 19)
													dialog.dismiss();

													sendEle();
													finish();
												}
											});
									builder.create().show();
								}
							});
							stopTimer();

						}else{
							retrySensorCount++;
							mSendSensorTime =System.currentTimeMillis();
							isConfigReceived =false;
							mBluetoothLeService.WriteValue(configSetting);
						}
					}else{
						retrySensorCount=0;
						mSendSensorTime =System.currentTimeMillis();
						isConfigReceived =false;
						mBluetoothLeService.WriteValue(configSetting);
					}

				}
			};
		}
		if (mTimer!=null&&mTimerTask!=null){
			mTimer.schedule(mTimerTask,0,400);
		}
	}


	private int retryStopCount;
	private boolean isStopReceived;
	private void startStopTimer(){
		if (mTimer==null){
			mTimer=new Timer();
		}
		if (mTimerTask==null){
			mTimerTask=new TimerTask() {
				@Override
				public void run() {
					if (!isStopReceived){

						if (retryStopCount>=2) {//TODO 发2底层没有应答

							sendEle();
							finish();
							stopTimer();

						}else{
							retryStopCount++;
							isStopReceived =false;
							if (mSendStopCmdFlag){
								sendStopSettingData();
							}

							if (mSendAgainCmdFlag){
								sendAgainSettingData();
							}
						}
					}
				}
			};
		}
		if (mTimer!=null&&mTimerTask!=null){
			mTimer.schedule(mTimerTask,0,400);
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

					//TODO 接收数据处理
					//当校验码前面的数据相加不等于校验码时表示数据错误
					boolean crcIsRight = ConvertUtils.CommonUtils.IsCRCRight(data);
					if (!crcIsRight) {
						//误码纠正
						if (data.length > 13) {
							frontData = new byte[data.length - 13];
							System.arraycopy(data, 13, frontData, 0, frontData.length);
							LogUtil.i(TAG, "截取的frontData:" + Arrays.toString(frontData));
							data = Arrays.copyOfRange(data, 0, 13);
							if (!ConvertUtils.CommonUtils.IsCRCRight(data)) {
								return;
							}
							LogUtil.i(TAG, "截取的data:" + Arrays.toString(data));
						} else if (data.length < 13) {
							wholeData = new byte[13];
							if (frontData != null) {
								System.arraycopy(frontData, 0, wholeData, 0, frontData.length);
								System.arraycopy(data, 0, wholeData, frontData.length, data.length);
								data = wholeData;
								LogUtil.i(TAG, "拼接的data：" + Arrays.toString(data));
								if (!ConvertUtils.CommonUtils.IsCRCRight(data)) {
									return;
								}
								wholeData = null;
								frontData = null;
							} else {
								return;
							}
						} else {//data.length==11

						}
					}

					if (mSendAgainCmdFlag) {
						if (data[2]==configSetting[2]){
							stopTimer();
							startConfigTimer();
							mSendAgainCmdFlag=false;
						}
					}

					if (mSendStopCmdFlag){
						if (data[2]==2){
							retryStopCount=0;
							isStopReceived=true;
							stopTimer();
							mSendStopCmdFlag=false;
							sendEle();
							finish();
						}
					}

					if (data[2]==configSetting[2]) {

						mReceiveDataCount++;

						//目前只计算正常运行时使用的电量
						if (mReceiveDataCount==1){
							mPreDataTime=System.currentTimeMillis();
						}else {
							mCurDataTime=System.currentTimeMillis();
							mEleSum+=(mCurDataTime-mPreDataTime)*getEle(data[8],data[9]);
							mPreDataTime=mCurDataTime;
							LogUtil.i(TAG,"运行时消耗的电量："+mEleSum);
						}

						isConfigReceived =true;
						int temp;
						mSum = mSum + ConvertUtils.byte2unsignedInt(data[3]);//11个数据的平均值作为一个显示数据

						if (mReceiveDataCount % 11 == 0 || mReceiveDataCount == 1) {
							mCurrentTime = System.currentTimeMillis();
							if (mReceiveDataCount == 1) {
								insertPoint(mCurrentTime, (int) Math.floor(mSum));
								temp = (int) Math.floor(mSum);
							} else {
								insertPoint(mCurrentTime, (int) Math.floor(mSum / 11));
								temp = (int) Math.floor(mSum / 11);//无符号位转换

								mSum = 0;
							}

							Message msg = Message.obtain();
							msg.what = 2;
							msg.arg1 = temp;
							mHandler.sendMessage(msg);

							Message msgTemp = Message.obtain();
							msgTemp.what = 3;
							msgTemp.arg1 = temp;
							mHandler.sendMessage(msgTemp);
						}
					}

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


	//通过接收到的数据计算电流的大小
	private int getEle(byte high,byte low){
		return ConvertUtils.byte2unsignedInt(high)*256+ConvertUtils.byte2unsignedInt(low);
	}


	private void dynamicDataDisplay(long currentTime, int temp) {
		if (!isOver) {
			mLineChartView.setInteractive(false);
			PointValue value = new PointValue(dataCount, temp);
			mPointValueList.add(value);
			mAxisValues.add(new AxisValue(dataCount).setLabel(TimeUtil.date2String(currentTime, "mm:ss")));
			dataCount++;
			float x = value.getX();
			LogUtil.i(TAG, "x的值：" + x);
			LogUtil.i(TAG, "点的个数：dataCount" + dataCount);

			Line line;
			line = new Line(mPointValueList);
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
			LogUtil.i(TAG, "X的值：" + x);
			if (x > mXDisplayCount) {
				port = initViewPort(x - mXDisplayCount, x);
			} else {
				port = initViewPort(0, mXDisplayCount);
			}
			mLineChartView.setCurrentViewport(port);//当前窗口

			Viewport maxPort = initMaxViewPort(x);
			mLineChartView.setMaximumViewport(maxPort);//最大窗口

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
		port.top = 60;
		port.bottom = 20;//y轴显示的最低值
		port.left = left;
		port.right = right;
		return port;
	}

	private Viewport initMaxViewPort(float right) {
		Viewport port = new Viewport();
		port.top = 60;
		port.bottom = 20;
		port.left = 0;
		port.right = right + mXDisplayCount;
		return port;
	}


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

		LogUtil.i(TAG, "We are in destroy");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_again:
				mSendAgainCmdFlag=true;
				mStartTime = System.currentTimeMillis();
				isAgain();
				break;
			case R.id.iv_over://实现结束功能(当结束后则直接返回，如果疗程没有进行完则提示)
				mSendStopCmdFlag=true;
				if (mRunningTime < mOriginalTime) {
					stopTimer();
					if (mConnected) {
						startStopTimer();//发送结束疗程的配置数据
					}
					saveData();//此处保存的数据为未做完的
				} else {

					sendEle();
					finish();
				}
				break;
			case R.id.iv_back:
				mSendStopCmdFlag=true;
				isOver();
				break;
		}
	}


	private void sendEle(){
		Intent intent=new Intent();
		intent.putExtra("ele",mEleSum);
		setResult(RESULT_OK,intent);
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
						startStopTimer();

						getDb();//如果要将重新开始的保存为另一份；
						mCure=null;//设置为null,将会重新新建cure对象
						points=null;
						points=new ArrayList<>();
						dialog.dismiss();

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

	private void sendAgainSettingData() {

		if (CureSPUtil.isSaved(Constants.SP_MEDICAL_STIMULATE, this)) {
			mStimulate = CureSPUtil.getSP(Constants.SP_MEDICAL_STIMULATE, this);
		}

		if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_GRADE, this)) {
			mCauterizeGrade = CureSPUtil.getTempByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_GRADE, this));
		}
		if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_TIME_GRADE, this)) {
			mCauterizeTime = CureSPUtil.getCauterizeTimeByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_TIME_GRADE, this));
		}
		if (CureSPUtil.isSaved(Constants.SP_NEEDLE_TYPE, this)) {
			mNeedleType = CureSPUtil.getSP(Constants.SP_NEEDLE_TYPE, this);
		}
		if (CureSPUtil.isSaved(Constants.SP_NEEDLE_GRADE, this)) {
			mNeedleGrade = CureSPUtil.getSP(Constants.SP_NEEDLE_GRADE, this);
		}
		if (CureSPUtil.isSaved(Constants.SP_NEEDLE_FREQUENCY, this)) {
			mNeedleFrequency = CureSPUtil.getSP(Constants.SP_NEEDLE_FREQUENCY, this);
		}
		if (CureSPUtil.isSaved(Constants.SP_MEDICINE_TIME_GRADE, this)) {
			mMedicineTime = CureSPUtil.getMedicineTimeByPosition(CureSPUtil.getSP(Constants.SP_MEDICINE_TIME_GRADE, this));
		}

		LogUtil.i(TAG, "发送的数据Settings:" + Arrays.toString(CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
				, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime)));

		if (HomeFragment.getBluetoothLeService() == null) {
			return;
		}

		HomeFragment.getBluetoothLeService().WriteValue(CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
				, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime));
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
		mSendStopCmdFlag=true;
		isOver();
//		super.onBackPressed();//调用父类的方法，使得点击返回键时直接返回上一个Activity，去掉此方法则不会直接退出
	}
}
