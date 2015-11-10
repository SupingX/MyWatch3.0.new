package com.mycj.mywatch;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mycj.mywatch.activity.DeviceBindOtherActivity;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.util.MessageUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SpalishActivity extends BaseActivity {
	private Handler mHandler = new Handler(){};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spalish);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				

				// byte[] byteForWeather = null;
				// //7.同步天气 ？
				// String wieid = (String)
				// SharedPreferenceUtil.get(getApplicationContext(),
				// Constant.SHARE_PLACE_WOEID, "");
				// // if (wieid != null) {
				// // String weather= (String)
				// SharedPreferenceUtil.get(getApplicationContext(),
				// Constant.SHARE_PLACE_WEATHER, "");
				// // String unit= (String)
				// SharedPreferenceUtil.get(getApplicationContext(),
				// Constant.SHARE_PLACE_UNIT, "");
				// // String temp = (String)
				// SharedPreferenceUtil.get(getApplicationContext(),
				// Constant.SHARE_PLACE_TEMP, "");
				// // byteForWeather =
				// ProtocolForWrite.instance().getByteForWeather(weather,
				// unit,temp);
				// // }
				
//				List<byte[]> values = new ArrayList<>();
//				values.add(byteForSyncTime);
//				values.add(byteForSleepTime);
//				values.add(byteForHeartRate);
//				values.add(byteForAlarmClock);
//				values.add(byteForSleepQualityOfToday);
				
//				intent.putExtra("byteForSyncTime", byteForSyncTime);
//				intent.putExtra("byteForSleepTime", byteForSleepTime);
//				intent.putExtra("byteForHeartRate", byteForHeartRate);
//				intent.putExtra("byteForAlarmClock", byteForAlarmClock);
//				intent.putExtra("byteForSleepQualityOfToday", byteForSleepQualityOfToday);
				BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
				BluetoothAdapter adapter = mBluetoothManager.getAdapter();
				if (adapter!=null ) {
					if (adapter.isEnabled()) {
						enter();
					}else{
						//检查是否打开蓝牙
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, 1);
					}
				}
			
			}
		}, 2000);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		if (requestCode==1) {
			if (resultCode == RESULT_OK) {
				openBlueDialog = showProgressDialog("正在打开蓝牙...",false);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						openBlueDialog.dismiss();
						openBlueDialog=null;
					}
				}, 5000);
			}
		}
		super.onActivityResult(requestCode, resultCode, arg2);
	}
	
	private void enter(){
		Intent intent = new Intent(SpalishActivity.this, MainActivity.class);
		Bundle b = intent.getExtras();
		// 第一次进入App 同步数据
		int mmsCount = MessageUtil.getNewMmsCount(getApplicationContext());
		int msmCount = MessageUtil.getNewSmsCount(getApplicationContext());
		int phoneCount = MessageUtil.readMissCall(getApplicationContext());
		// 1。同步时间
		byte[] byteForSyncTime = ProtocolForWrite.instance().getByteForSyncTime(new Date());
		// 2。同步睡眠开始 结束时间
		int start = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_SLEEP_START_HOUR, 0);
		int end = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_SLEEP_END_HOUR, 0);
		byte[] byteForSleepTime = ProtocolForWrite.instance().getByteForSleepTime(start, end);
		// 3。最大最小心率
		int maxHr = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_HEART_RATE_MAX, 240);
		int minHr = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_HEART_RATE_MIN, 40);
		byte[] byteForHeartRate = ProtocolForWrite.instance().getByteForHeartRate(maxHr, minHr);
		// 4。闹钟时间
		// 获取初始值
		int hour_1 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_HOUR_1, 12);
		int min_1 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_MIN_1, 00);
		int hour_2 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_HOUR_2, 12);
		int min_2 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_MIN_2, 00);
		int hour_3 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_HOUR_3, 12);
		int min_3 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_MIN_3, 00);
		int hour_4 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_HOUR_4, 12);
		int min_4 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_MIN_4, 00);
		int hour_5 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_HOUR_5, 12);
		int min_5 = (int) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CLOCK_MIN_5, 00);
		boolean isChecked_1 = (boolean) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_CHECK_BOX_CLOCK_1, false);
		boolean isChecked_2 = (boolean) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CHECK_BOX_CLOCK_2, false);
		boolean isChecked_3 = (boolean) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CHECK_BOX_CLOCK_3, false);
		boolean isChecked_4 = (boolean) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CHECK_BOX_CLOCK_4, false);
		boolean isChecked_5 = (boolean) SharedPreferenceUtil.get(SpalishActivity.this, Constant.SHARE_CHECK_BOX_CLOCK_5, false);
		byte[] byteForAlarmClock = ProtocolForWrite.instance().getByteForAlarmClock(new int[] { hour_1, min_1, hour_2, min_2, hour_3, min_3, hour_4, min_4, hour_5, min_5 },
				new boolean[] { isChecked_1, isChecked_2, isChecked_3, isChecked_4, isChecked_5 });
		// 5。请求今天的睡眠数据
		byte[] byteForSleepQualityOfToday = ProtocolForWrite.instance().getByteForSleepQualityOfToday(0);
		startActivity(intent);
		finish();
	}
	
	
	private AbstractBluetoothStateBroadcastReceiver mBlueStateReceiver = new AbstractBluetoothStateBroadcastReceiver(){

		@Override
		public void onBluetoothChange(int state, int previousState) {
			switch (state) {
			case BluetoothAdapter.STATE_ON:
				Log.i("", "蓝牙已打开");
				if (openBlueDialog!=null && openBlueDialog.isShowing()) {
					openBlueDialog.dismiss();
					openBlueDialog=null;
				}
				startActivity(MainActivity.class);
				
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				Log.i("", "蓝牙关闭中。。。");
				break;
			case BluetoothAdapter.STATE_TURNING_ON:
				Log.i("", "蓝牙打开中。。。");
				break;
			case BluetoothAdapter.STATE_OFF:
				Log.i("", "蓝牙已关闭");
//				if (openBlueDialog!=null && openBlueDialog.isShowing()) {
//					openBlueDialog.dismiss();
//				}
				break;
			default:
				break;
			}
		}
	};
	private ProgressDialog openBlueDialog;
	
	
	@Override
	protected void onStart() {
		super.onStart();
		registerBoradcastReceiverForCheckBlueToothState(mBlueStateReceiver);
		
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mBlueStateReceiver);
		super.onDestroy();
		
	}
	
	/**
	 * 监听蓝牙变化情况
	 */
	private void registerBoradcastReceiverForCheckBlueToothState(BroadcastReceiver stateChangeReceiver) {
		IntentFilter stateChangeFilter = new IntentFilter();
		stateChangeFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		stateChangeFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		registerReceiver(stateChangeReceiver, stateChangeFilter);
	}

	@Override
	public void initViews() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setListener() {
		// TODO Auto-generated method stub
		
	}
}
