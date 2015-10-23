package com.mycj.mywatch;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.util.MessageUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SpalishActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spalish);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
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
				
				intent.putExtra("byteForSyncTime", byteForSyncTime);
				intent.putExtra("byteForSleepTime", byteForSleepTime);
				intent.putExtra("byteForHeartRate", byteForHeartRate);
				intent.putExtra("byteForAlarmClock", byteForAlarmClock);
				intent.putExtra("byteForSleepQualityOfToday", byteForSleepQualityOfToday);
				startActivity(intent);
				finish();
			}
		}, 2000);
	}
	
}
