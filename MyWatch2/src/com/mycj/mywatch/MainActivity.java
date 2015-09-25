package com.mycj.mywatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import com.mycj.mywatch.activity.CameraActivity;
import com.mycj.mywatch.activity.ClockActivity;
import com.mycj.mywatch.activity.DeviceActivity;
import com.mycj.mywatch.activity.HeartRateActivity;
import com.mycj.mywatch.activity.MoreActivity;
import com.mycj.mywatch.activity.PedometerActivity;
import com.mycj.mywatch.activity.SleepActivity;
import com.mycj.mywatch.activity.WeatherActivity;
import com.mycj.mywatch.bean.CodeDB;
import com.mycj.mywatch.bean.ConditionWeather;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.bean.PedoData;
import com.mycj.mywatch.business.LoadWeatherJsonTask;
import com.mycj.mywatch.business.LoadWeatherJsonTask.OnProgressChangeListener;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.DataUtil;
import com.mycj.mywatch.util.DateUtil;
import com.mycj.mywatch.util.MessageUtil;
import com.mycj.mywatch.util.MyFileUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.util.SoundPlay;
import com.mycj.mywatch.util.YahooUtil;
import com.mycj.mywatch.view.ActionSheetDialog;
import com.mycj.mywatch.view.ActionSheetDialog.OnSheetItemClickListener;
import com.mycj.mywatch.view.ActionSheetDialog.SheetItemColor;
import com.mycj.mywatch.view.AlertDialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends BaseActivity implements OnClickListener, OnProgressChangeListener {

	private ImageView imgPedo;
	private ImageView imgHeartRate;
	private ImageView imgClock;
	private ImageView imgCamera;
	private ImageView imgSleep;
	private ImageView imgDevice;
	private ImageView imgWeather;
	private ImageView imgMore;
	private FrameLayout frPedo;
	private FrameLayout frHeartRate;
	private FrameLayout frClock;
	private FrameLayout frCamera;
	private FrameLayout frSleep;
	private FrameLayout frDevice;
	private FrameLayout frWeather;
	private FrameLayout frMore;
	private boolean isLocked;
	private TextView tvWeatherText;
	private TextView tvWeatherTemp;
	private TextView tvWeatherAddress;
	private final static int MSG_PLACE = 0x100004;
	private final static int MSG_FORECAST = 0x10005;
	private final static int MSG_HEART_RATE = 0x10006;
	private final static int MSG_RSSI = 0x10007;
	private final static int MSG_RUN = 0x10008;
	private TextView tvClock;
	private TextView tvClockOnOff;
	private final String UNIT_C = "℃";
	private final String UNIT_F = "℉";
	private ProgressDialog showProgressDialog;

	private int cal;
	private int distance;
	private int hour;
	private int minute;
	private int second;
	// private SimpleBlueService mSimpleBlueService;
	private int step;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SimpleBlueService.ACTION_CONNECTION_STATE)) {
				final int state = intent.getExtras().getInt(AbstractSimpleBlueService.EXTRA_CONNECT_STATE);
				runOnUiThread(new Runnable() {
					public void run() {
						if (state == BluetoothProfile.STATE_DISCONNECTED || (mSimpleBlueService != null && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_DISCONNECTED)) {
							tvRssi.setText("--");
							mHandler.removeCallbacks(runRssi);
							tvRssi.setText(String.valueOf(0));
						}

					}
				});
			} else if (action.equals(SimpleBlueService.ACTION_SERVICE_DISCOVERED_WRITE_DEVICE)) {
				mHandler.removeCallbacks(runRssi);
				mHandler.post(runRssi);
//				mHandler.removeCallbacks(syncSetting);
//				mHandler.postDelayed(syncSetting, 1500);
				if (null != mSimpleBlueService && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
					mSimpleBlueService.scanDevice(false);
				}
				


				// 当天的睡眠
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						byte[] data = ProtocolForWrite.instance().getByteForSleepQualityOfToday(0);
						Log.e("", "data : " + data + "_____________________________");
						if (null != mSimpleBlueService && mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
							mSimpleBlueService.writeCharacteristic(data);
						}

					}
				});

			} else if (action.equals(SimpleBlueService.ACTION_DATA_STEP)) {
				int[] datas = intent.getIntArrayExtra(SimpleBlueService.EXTRA_STEP);
				if (datas != null) {
					step = datas[0];
					cal = datas[1];
					distance = datas[2];
					hour = datas[3];
					minute = datas[4];
					second = datas[5];

				}

				runOnUiThread(new Runnable() {
					public void run() {
						tvStep.setText("" + step);
					}
				});
			} else if (action.equals(SimpleBlueService.ACTION_REMOTE_RSSI)) {
				final int rssi = intent.getExtras().getInt(SimpleBlueService.EXTRA_RSSI);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tvRssi.setText(String.valueOf(rssi));
					}
				});

			} else if (action.equals(SimpleBlueService.ACTION_DATA_HEART_RATE)) {
				final int hr = intent.getExtras().getInt(SimpleBlueService.EXTRA_HEART_RATE);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tvHeartRate.setText(String.valueOf(hr));
					}
				});

			} else if (action.equals(SimpleBlueService.ACTION_DATA_CAMERA)) {
				int notifyForCamera = intent.getExtras().getInt(SimpleBlueService.EXTRA_CAMERA);
				switch (notifyForCamera) {
				case 0:
					boolean foreground = isForeground(MainActivity.this, CameraActivity.class.getName());
					boolean foreground1 = isForeground(MainActivity.this, CameraActivity.class.getSimpleName());
					Log.v("", "-------------------foreground : " + foreground + foreground1);
					if (!isIncameraing) {
						Intent iCamera = new Intent(MainActivity.this, CameraActivity.class);
						startActivity(iCamera);
						isIncameraing = true;
						if (null != mSimpleBlueService && mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
							mSimpleBlueService.writeCharacteristic(DataUtil.hexStringToByte("F502"));
						}
					}

					break;
				case 1:

					break;

				default:
					break;
				}
			} else if (action.equals(SimpleBlueService.ACTION_DATA_HISTORY_SLEEP_FOR_TODAY)) {
				final int[] sleeps = intent.getIntArrayExtra(SimpleBlueService.EXTRA_SLEEP);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						tvSleep.setText(parseSleeps(sleeps) + "");
					}
				});
			}
		}
	};

//	private Runnable syncSetting = new Runnable() {
//		@Override
//		public void run() {
//			// 第一次进入App 同步数据
//			if (null != mSimpleBlueService && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
//				// 1。同步时间
//				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForSyncTime(new Date()));
//				// 3。同步睡眠开始 结束时间
//				int start = (int) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_SLEEP_START_HOUR, 0);
//				int end = (int) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_SLEEP_END_HOUR, 0);
//				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForSleepTime(start, end));
//				// 4。最大最小心率
//				int maxHr = (int) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_HEART_RATE_MAX, 240);
//				int minHr = (int) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_HEART_RATE_MIN, 40);
//				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForHeartRate(maxHr, minHr));
//				// 5。闹钟时间
//				int hourClock = (int) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_CLOCK_HOUR_1, 0);
//				int minClock = (int) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_CLOCK_MIN_1, 0);
//				boolean isColckOpen = (boolean) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_CHECK_BOX_CLOCK_1, false);
//				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForAlarmClock(hourClock, minClock, isColckOpen));
//				// 6。请求今天的睡眠数据
//				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForSleepQualityOfToday(0));
//				// 7。同步所有数据
//				long timeSave = (long) SharedPreferenceUtil.get(MainActivity.this, Constant.SHARE_UPDATE_TIME, 0L);
//				long timeToday = System.currentTimeMillis();// 记录第一次同步的时间
//				if (!DateUtil.isSameDayOfMillis(timeSave, timeToday)) {// 当不是同一天时，更新今天的时间，然后再发送同步今天所有的数据
//					Log.e("MainActivity", "今天第一次进入App，同步所有的历史数据");
//					SharedPreferenceUtil.put(MainActivity.this, Constant.SHARE_UPDATE_TIME, timeToday);
//					mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForSyncHistoryData());
//				}
//
//				if (null != mSimpleBlueService && mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
//					if (weatherCode != null) {
//						mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForWeather(weatherCode.getProtol(), unitInt, tempInt));
//					}
//				}
//			}
//		}
//	};

	/**
	 * 根据每天的数据得到每天的睡眠时间
	 * 
	 * @param sleeps
	 * @return
	 */
	private float parseSleeps(int[] sleeps) {
		float total = 0f;
		for (int i = 0; i < sleeps.length; i++) {
			switch (sleeps[i]) {
			case 0:
				break;
			case 1:
				total += 0.25f;// 获得总的睡眠时间
				break;
			case 2:
				total += 0.75f;// 获得总的睡眠时间
				break;
			case 3:
				total += 1f;// 获得总的睡眠时间
				break;
			case 4:
				total += 1f;// 获得总的睡眠时间
				break;
			case 5:
				total += 1f;// 获得总的睡眠时间
				break;
			default:
				break;
			}

		}
		return total;
	}

	private boolean isIncameraing;

	private TimerTask taskState = new TimerTask() {
		@Override
		public void run() {
			if (mSimpleBlueService != null) {
				// ConnectState currentState =
				// mLiteBlueService.getCurrentState();
				// if (currentState==null) {
				// Log.e("",
				// "========================================mLiteBlueService.getCurrentState().getMessage() : "+"null");
				// }else{
				// Log.e("",
				// "========================================mLiteBlueService.getCurrentState().getMessage() : "+currentState.getMessage());
				//
				// }

			}
			mHandler.sendEmptyMessage(2);
		}
	};
	private CodeDB weatherCode;
	private int unitInt;
	private int tempInt;
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 2:
				mHandler.postDelayed(taskState, 4000);
				break;
			case MSG_RUN:
				mHandler.removeCallbacks(runRssi);
				mHandler.postDelayed(runRssi, 5000);
				break;
			case MSG_FORECAST:
				Log.v("", "<!-- handler处理  之" + MSG_FORECAST);
				String json = msg.getData().getString("json");
				parseForecastJsonAndUpdateView(json);
				break;
			case LoadWeatherJsonTask.MSG_CONDITION:
				Log.v("", "<!-- handler处理  之 MSG_CONDITION ");
				String json5 = msg.getData().getString("json");
				Log.v("", "<!--json5" + json5);

				try {
					ConditionWeather parseConditionWeatherFromJson = YahooUtil.parseConditionWeatherFromJson(json5);
					String temp = parseConditionWeatherFromJson.getTemp();
					String code = parseConditionWeatherFromJson.getCode();
					tvWeatherTemp.setText(temp);
					weatherCode = YahooUtil.getWeatherCode(Integer.valueOf(code), getApplicationContext());
					tvWeatherText.setText(weatherCode.getText());
					unitInt = 0;
					tempInt = Integer.valueOf(temp);

					Log.e("", "tempInt : " + tempInt);
					if (tempInt < 0) {// 零度以下
						if (unit.equals(UNIT_C)) {
							unitInt = 0x40;
						} else {
							unitInt = 0x00;
						}
					} else {// 零上
						if (unit.equals(UNIT_C)) {
							unitInt = 0xC0;
						} else {
							unitInt = 0x80;
						}
					}

					if (null != mSimpleBlueService && mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
						mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForWeather(weatherCode.getProtol(), unitInt, tempInt));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	private TextView tvStep;;
	private Runnable runRssi;
	private TextView tvRssi;
	private TextView tvHeartRate;
	private HashMap<Integer, Integer> soundPoolMap;
	private SoundPool soundPool;
	private int sampleId;
	private TextView tvSleep;
	private TextView tvWeatherUnit;
	private String unit;
	private AbstractSimpleBlueService mSimpleBlueService;
	private SoundPlay play;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// StrictMode.setThreadPolicy(new
		// StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
		// StrictMode.setVmPolicy(new
		// StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		initViews();
		setListener();
		// mHandler.postDelayed(taskState,2000);

	}

	@Override
	protected void onStart() {
		super.onStart();
		runRssi = new Runnable() {
			@Override
			public void run() {
				Log.v("MainActivity", "______请求rssi_______");
				if (mSimpleBlueService != null) {
					mSimpleBlueService.readRemoteRssi();
				}
				mHandler.sendEmptyMessage(MSG_RUN);
			}
		};
		mSimpleBlueService = getSimpleBlueService();
		Log.e("", "mSimpleBlueService : " + mSimpleBlueService);
		registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());

	}

	@Override
	protected void onResume() {
		isIncameraing = false;
		
		// soundPool.play(soundPoolMap.get(1), 1, 1, 0, 3, 1);
		checkBlue();
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				loadWeather(); // 加载天气
			}
		});
		if (null != mSimpleBlueService && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
			mSimpleBlueService.readRemoteRssi();
		
		}

		setClockTime();
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		switch (requestCode) {
		case 1:

			if (resultCode == RESULT_OK) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mSimpleBlueService.isEnable()) {
							// 蓝牙打开
							mSimpleBlueService.scanDevice(true);
						} else {
							// 未打开
						}
					}
				}, 5000);
			}

			break;

		default:
			break;
		}

		super.onActivityResult(requestCode, resultCode, arg2);
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(runRssi);
		isOnceEnter = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		// if (mSimpleBlueService!=null) {
		// mSimpleBlueService.close();
		// }
		super.onDestroy();

	}

	public void initViews() {
		imgPedo = (ImageView) findViewById(R.id.img_main_pedo);
		imgHeartRate = (ImageView) findViewById(R.id.img_main_hr);
		imgClock = (ImageView) findViewById(R.id.img_main_clock);
		imgCamera = (ImageView) findViewById(R.id.img_main_camera);
		imgSleep = (ImageView) findViewById(R.id.img_main_sleep);
		imgDevice = (ImageView) findViewById(R.id.img_main_device);
		imgWeather = (ImageView) findViewById(R.id.img_main_weather);
		imgMore = (ImageView) findViewById(R.id.img_main_more);
		frPedo = (FrameLayout) findViewById(R.id.fr_pedo);
		frHeartRate = (FrameLayout) findViewById(R.id.fr_hr);
		frClock = (FrameLayout) findViewById(R.id.fr_clock);
		frCamera = (FrameLayout) findViewById(R.id.fr_camera);
		frSleep = (FrameLayout) findViewById(R.id.fr_sleep);
		frDevice = (FrameLayout) findViewById(R.id.fr_device);
		frWeather = (FrameLayout) findViewById(R.id.fr_weather);
		frMore = (FrameLayout) findViewById(R.id.fr_more);
		// 计步
		tvStep = (TextView) findViewById(R.id.tv_main_step);
		// 睡眠
		tvSleep = (TextView) findViewById(R.id.tv_main_sleep);
		// 心率
		tvHeartRate = (TextView) findViewById(R.id.tv_main_hr);
		// 蓝牙
		tvRssi = (TextView) findViewById(R.id.tv_main_rssi);
		// 闹钟
		tvClock = (TextView) findViewById(R.id.tv_main_clock);
		tvClockOnOff = (TextView) findViewById(R.id.tv_main_clock_on_off);
		// 天气
		tvWeatherText = (TextView) findViewById(R.id.tv_weather_text);
		tvWeatherTemp = (TextView) findViewById(R.id.tv_weather_temp);
		tvWeatherUnit = (TextView) findViewById(R.id.tv_weather_unit);
		tvWeatherAddress = (TextView) findViewById(R.id.tv_weather_address);
	}

	@Override
	public void setListener() {
		imgPedo.setOnClickListener(this);
		imgHeartRate.setOnClickListener(this);
		imgClock.setOnClickListener(this);
		imgCamera.setOnClickListener(this);
		imgSleep.setOnClickListener(this);
		imgDevice.setOnClickListener(this);
		imgWeather.setOnClickListener(this);
		imgMore.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_main_pedo:
			// ProtocolForWrite.instance().getByteForAlarmClock(23, 23);
			// ProtocolForWrite.instance().getByteForAvoidLose(1);
			// ProtocolForWrite.instance().getByteForCamera(1);
			// ProtocolForWrite.instance().getByteForHeartRate(23, 12);
			// ProtocolForWrite.instance().getByteForMissedCallAndMessage(1, 3);
			// ProtocolForWrite.instance().getByteForRemind(2, "13047618057");
			// ProtocolForWrite.instance().getByteForShutDown();
			// ProtocolForWrite.instance().getByteForSleep(20);
			// ProtocolForWrite.instance().getByteForSleepQualityOfToday(66);
			// ProtocolForWrite.instance().getByteForSleepTime(23, 1);
			// ProtocolForWrite.instance().getByteForStep(2);
			// ProtocolForWrite.instance().getByteForSyncHistoryData();
			// ProtocolForWrite.instance().getByteForSyncTime(new Date());
			// ProtocolForWrite.instance().getByteForWeather(1, 2, 67);
			// byte [] data = DataUtil.hexStringToByte("F60122");
			// ProtocolForNotify.instance().notifyForMusic(data);
			// ProtocolForNotify.instance().notifyForStepData(DataUtil.hexStringToByte("F7223344556677889900112233445566"));
			Intent iPedo = new Intent(this, PedometerActivity.class);
			iPedo.putExtra("step", step);
			iPedo.putExtra("cal", cal);
			iPedo.putExtra("distance", distance);
			iPedo.putExtra("hour", hour);
			iPedo.putExtra("minute", minute);
			iPedo.putExtra("second", second);
			enter(frPedo, iPedo);
			break;
		case R.id.img_main_hr:
			Intent iHr = new Intent(this, HeartRateActivity.class);
			enter(frHeartRate, iHr);
			break;
		case R.id.img_main_clock:
			Intent iClock = new Intent(this, ClockActivity.class);
			enter(frClock, iClock);
			break;
		case R.id.img_main_camera:
			isIncameraing = true;
			Intent iCamera = new Intent(this, CameraActivity.class);
			enter(frCamera, iCamera);
			if (null != mSimpleBlueService && mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
				mSimpleBlueService.writeCharacteristic(DataUtil.hexStringToByte("F502"));
			}
			break;
		case R.id.img_main_sleep:

			Intent iSleep = new Intent(this, SleepActivity.class);
			enter(frSleep, iSleep);
			break;
		case R.id.img_main_device:
			Intent iDevice = new Intent(this, DeviceActivity.class);
			enter(frDevice, iDevice);
			break;
		case R.id.img_main_weather:
			Intent iWeather = new Intent(this, WeatherActivity.class);
			enter(frWeather, iWeather);
			break;
		case R.id.img_main_more:
			Intent iMore = new Intent(this, MoreActivity.class);
			enter(frMore, iMore);
			break;

		default:
			break;
		}
	}

	@Override
	public void onPreExecute(int id) {
	}

	@Override
	public void onPostExecute(int id) {
	}

	@Override
	public void onError(int id) {
		// showShortToast("加载异常，请检查网络设置");
		Log.v("", "加载异常，请检查网络设置");
		loadLastWeather();
	}

	/**
	 * 解析天气并更新视图
	 * 
	 * @param json
	 */
	private void parseForecastJsonAndUpdateView(String json) {
		if (json == null) {
			return;
		}
		if (json.trim().equals("")) {
			return;
		}
		try {
			ConditionWeather weather = YahooUtil.parseConditionWeatherFromJson(json);
			tvWeatherText.setText(weather.getText());
			tvWeatherTemp.setText(weather.getTemp());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载天气
	 */
	private void loadWeather() {
		String city = (String) SharedPreferenceUtil.get(this, Constant.SHARE_PLACE_NAME, "--");
		tvWeatherAddress.setText(city);
		if (!city.equals("--")) {
			unit = (String) SharedPreferenceUtil.get(this, Constant.SHARE_PLACE_UNIT, "℃");
			tvWeatherUnit.setText(unit);
			String woeid = (String) SharedPreferenceUtil.get(this, Constant.SHARE_PLACE_WOEID, "");
			new LoadWeatherJsonTask(LoadWeatherJsonTask.MSG_CONDITION, this, mHandler).execute(YahooUtil.getConditionUrl(woeid, unit));
		}
	}

	private boolean isOnceEnter = true;

	/**
	 * 检查蓝牙
	 */
	private void checkBlue() {
		Log.e("", "-----检查蓝牙-----");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mSimpleBlueService != null) {
					// 确认蓝牙
					if (!mSimpleBlueService.isEnable()) {
//						if (isOnceEnter) {
//							Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//							enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
////							startActivityForResult(enableBtIntent, 1);
//							startActivity(enableBtIntent);
//							isOnceEnter = false;
//						}
						// showIosDialog();
					} else {
						if ((null != mSimpleBlueService && mSimpleBlueService.isBinded())) {
							Log.v("", "首次进入，发觉蓝牙绑定，尝试搜索链接");
							 if (mSimpleBlueService.isScanning()) {
							 mSimpleBlueService.scanDevice(false);
							 }
							mSimpleBlueService.scanDevice(true);
//							mHandler.postDelayed(new Runnable() {
//								
//								@Override
//								public void run() {
//									 if (mSimpleBlueService.isScanning()) {
//										 mSimpleBlueService.scanDevice(false);
//										 }
//								}
//							}, 30*1000);
						}
					}
				} else {
					Log.e("", "-----检查蓝牙-----service为空");
				}

			}
		});
	}

	private void enter(View img, Intent intent) {
		startAnimation(img, intent);
	}

	/**
	 * 记载上次信息
	 */
	private void loadLastWeather() {
		String lastJson = (String) SharedPreferenceUtil.get(this, Constant.SHARE_JSON_FORECAST, "");
		parseForecastJsonAndUpdateView(lastJson);
	}

	/**
	 * 设置闹钟
	 */
	private void setClockTime() {
		int hour = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_1,12);
		int min = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_1, 0);
		boolean isOpen = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_1, false);
		tvClock.setText(parseText(hour) + " : " + parseText(min));
		tvClockOnOff.setText(isOpen ? getStringFromResource(R.string.on) : getStringFromResource(R.string.off));
	}

	private void showIosDialog() {
		ActionSheetDialog dialog = new ActionSheetDialog(this).builder();
		dialog.setTitle(getString(R.string.enable_blue));
		dialog.addSheetItem(getString(R.string.open), SheetItemColor.Red, new OnSheetItemClickListener() {
			@Override
			public void onClick(int which) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// if (mLiteBlueService.isEnable()) {
						// // 蓝牙打开
						// if (mLiteBlueService.isBinded()) {// 当本地绑定时，开启搜索。
						// mLiteBlueService.startScanUsePeriodScanCallback();
						// }
						// } else {
						// // 未打开
						// }
					}
				}, 5000);
				// mLiteBlueService.enable();
			}
		}).show();
	}

	private void startAnimation(View img, final Intent intent) {
		// ObjectAnimator oaTranslationX = ObjectAnimator.ofFloat(img,
		// "translationX", 0, mScreenWidth / 2);
		// ObjectAnimator oaTranslationY = ObjectAnimator.ofFloat(img,
		// "translationY", 0, mScreenHeight / 2);
		ObjectAnimator oaScaleX = ObjectAnimator.ofFloat(img, "scaleX", 0.5f, 1.2f, 1f);
		ObjectAnimator oaScaleY = ObjectAnimator.ofFloat(img, "scaleY", 0.5f, 1.2f, 1f);

		// oaTranslationX.setDuration(1000);
		// oaTranslationY.setDuration(1000);
		// oaScaleX.setDuration(1000);
		// oaScaleY.setDuration(1000);
		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				isLocked = true;
				super.onAnimationStart(animation);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				Log.e("", "动画完成");
				isLocked = false;
				startActivity(intent);
			}
		});
		set.play(oaScaleX).with(oaScaleY);
		set.setDuration(200);
		if (!isLocked) {
			set.start();
		}

	}

	@Override
	public void onBackPressed() {

		ActionSheetDialog exitDialog = new ActionSheetDialog(this).builder();
		exitDialog.setTitle("退出程序？");
		exitDialog.addSheetItem("确定", SheetItemColor.Red, new OnSheetItemClickListener() {
			@Override
			public void onClick(int which) {
				finish();
				System.exit(0);
			}
		}).show();
		// super.onBackPressed();
		int tempInt = Integer.valueOf("-20");
		Log.e("", "________________________tempInt : " + tempInt);

		// for test
		// mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForWeather(0x01,
		// 0x40 , 0x54));
		// showdialog("hahhahah");
		// findStepDateByDate();

	
//		mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForHeartRate(44, 244));

	}

	/**
	 * 根据日期查找stepData
	 * 
	 * @param date
	 * @return
	 */
	private void findStepDateByDate() {
		// List<PedoData> stepdatass =
		// DataSupport.where("year=? and month=? and day=?",
		// sql.substring(0,2),sql.substring(2,6),sql.substring(6,8)).find(PedoData.class);
		// List<PedoData> stepdatas = DataSupport.findAll(PedoData.class);
		// Log.e("", "step : " + stepdatas.size());
		// for (PedoData step : stepdatas) {
		// Log.e("", "step : " + step.toString());
		// }

		// Date date = new Date();
		// Calendar c = Calendar.getInstance();
		// c.setTime(date);
		// c.add(Calendar.DAY_OF_MONTH, 1);
		// Log.e("", "c:" + c.get(Calendar.DAY_OF_MONTH));
		// Log.e("", "date:" + DateUtil.dateToString(c.getTime()));

		// boolean isCallRemind;
		// isCallRemind = (boolean) SharedPreferenceUtil.get(this,
		// Constant.SHARE_CHECK_REMIND_CALL, false);
		// if (null != mSimpleBlueService && isCallRemind &&
		// mSimpleBlueService.getConnectState() ==
		// BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
		// mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForMissedCallAndMessage(0,
		// 0));
		// }

		// if (null != mSimpleBlueService && isCallRemind &&
		// mSimpleBlueService.getConnectState() ==
		// BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
		// mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForRemind(0x00,
		// "00"));
		// }

	}

}
