package com.mycj.mywatch.activity;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.MainActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.adapter.NumberWheelAdapter;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.DisplayUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.view.NumberDialog;
import com.mycj.mywatch.view.OnWheelChangedListener;
import com.mycj.mywatch.view.OnWheelScrollListener;
import com.mycj.mywatch.view.WheelView;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ClockActivity extends BaseActivity implements OnClickListener {

//	private RelativeLayout frClock;
	private View timepickerview1;
	private CheckBox cbClock1;
	private TextView tvSave;
	private boolean isChecked_1;
	private WheelView hourWV;
	private WheelView minWV;
	private AbstractSimpleBlueService mSimpleBlueService;
	private TextView tvClock1;
	private FrameLayout frHome;
	private int hour_1;
	private int min_1;
	private Handler mHandler = new Handler() {

	};
	private TextView tvClock2;
	private TextView tvClock3;
	private TextView tvClock4;
	private TextView tvClock5;
	private RelativeLayout rlClock1;
	private RelativeLayout rlClock2;
	private RelativeLayout rlClock3;
	private RelativeLayout rlClock4;
	private RelativeLayout rlClock5;
	private CheckBox cbClock2;
	private CheckBox cbClock3;
	private CheckBox cbClock4;
	private CheckBox cbClock5;
	private int hour_2;
	private int min_2;
	private int hour_3;
	private int min_3;
	private int hour_4;
	private int min_4;
	private int hour_5;
	private int min_5;
	private boolean isChecked_2;
	private boolean isChecked_3;
	private boolean isChecked_4;
	private boolean isChecked_5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clock);
		// mLiteBlueService = getLiteBlueService();
//		SharedPreferenceUtil.put(this, Constant.SHARE_CHECK_BOX_CLOCK, true);
		initViews();
		setListener();

	}

	@Override
	protected void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();

	}

	@Override
	protected void onResume() {
		initClockTime();
		super.onResume();
	}

	@Override
	public void initViews() {
//		frClock = (RelativeLayout) findViewById(R.id.rl_clock);
		// 	标题
		tvSave = (TextView) findViewById(R.id.tv_save);
		frHome = (FrameLayout) findViewById(R.id.fl_home);
		
		//	闹钟 RelativeLayout
		rlClock1 = (RelativeLayout) findViewById(R.id.rl_clock_1);
		rlClock2 = (RelativeLayout) findViewById(R.id.rl_clock_2);
		rlClock3 = (RelativeLayout) findViewById(R.id.rl_clock_3);
		rlClock4 = (RelativeLayout) findViewById(R.id.rl_clock_4);
		rlClock5 = (RelativeLayout) findViewById(R.id.rl_clock_5);
		
		//	闹钟 TextView
		 tvClock1 = (TextView) findViewById(R.id.tv_clock_1);
		 tvClock2 = (TextView) findViewById(R.id.tv_clock_2);
		 tvClock3 = (TextView) findViewById(R.id.tv_clock_3);
		 tvClock4 = (TextView) findViewById(R.id.tv_clock_4);
		 tvClock5 = (TextView) findViewById(R.id.tv_clock_5);
		
		 //	闹钟 CheckBox
		cbClock1 = (CheckBox) findViewById(R.id.cb_clock_1);
		cbClock2 = (CheckBox) findViewById(R.id.cb_clock_2);
		cbClock3 = (CheckBox) findViewById(R.id.cb_clock_3);
		cbClock4 = (CheckBox) findViewById(R.id.cb_clock_4);
		cbClock5 = (CheckBox) findViewById(R.id.cb_clock_5);
		
	
		
		
//		initNumberPicker();
	}

//	private void initNumberPicker() {
//		LayoutInflater inflater1 = LayoutInflater.from(ClockActivity.this);
//		timepickerview1 = inflater1.inflate(R.layout.timepicker, null);
//		// 设置时间
//		hourWV = (WheelView) timepickerview1.findViewById(R.id.hour);
//		hourWV.setAdapter(new NumberWheelAdapter(0, 23));
//		hourWV.setCyclic(true);// 可循环滚动
//		hourWV.setLabel(getStringFromResource(R.string.hours));// 文字
//		hourWV.setCurrentItem(hour);
//		// 设置分钟
//		minWV = (WheelView) timepickerview1.findViewById(R.id.min);
//		minWV.setAdapter(new NumberWheelAdapter(0, 59));
//		minWV.setCyclic(true);// 可循环滚动
//		minWV.setLabel(getStringFromResource(R.string.min));
//		minWV.setCurrentItem(min);
//		// 根据屏幕密度来指定选择器字体的大小(不同屏幕可能不同)
//		int textSize = 0;
//		int screenheight = DisplayUtil.getScreenMetrics(this).y;
//		textSize = (screenheight / 100) * 3;
//		hourWV.TEXT_SIZE = textSize;
//		minWV.TEXT_SIZE = textSize;
//		//
//		OnWheelChangedListener hourWheelListener;
//		OnWheelScrollListener hourOnWheelScrollListener;
//
//		// 动态添加
//		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		param.addRule(RelativeLayout.CENTER_IN_PARENT);
//		timepickerview1.setLayoutParams(param);
//		frClock.addView(timepickerview1);
//	}

	@Override
	public void setListener() {
//		frClock.setOnClickListener(this);
		tvSave.setOnClickListener(this);
		frHome.setOnClickListener(this);

		cbClock1.setOnClickListener(this);
		cbClock2.setOnClickListener(this);
		cbClock3.setOnClickListener(this);
		cbClock4.setOnClickListener(this);
		cbClock5.setOnClickListener(this);
		rlClock1.setOnClickListener(this);
		rlClock2.setOnClickListener(this);
		rlClock3.setOnClickListener(this);
		rlClock4.setOnClickListener(this);
		rlClock5.setOnClickListener(this);
	}

	private void initClockTime() {
		//获取初始值
		hour_1 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_1, 12);
		min_1 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_1, 00);
		hour_2 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_2, 12);
		min_2 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_2, 00);
		hour_3 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_3, 12);
		min_3 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_3, 00);
		hour_4 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_4, 12);
		min_4 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_4, 00);
		hour_5 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_5, 12);
		min_5 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_5, 00);
		isChecked_1 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_1, false);
		isChecked_2 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_2, false);
		isChecked_3 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_3, false);
		isChecked_4 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_4, false);
		isChecked_5 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_5, false);
		//设置
		cbClock1.setChecked(isChecked_1);
		cbClock2.setChecked(isChecked_2);
		cbClock3.setChecked(isChecked_3);
		cbClock4.setChecked(isChecked_4);
		cbClock5.setChecked(isChecked_5);
		
		setClock(tvClock1, hour_1, min_1);
		setClock(tvClock2, hour_2, min_2);
		setClock(tvClock3, hour_3, min_3);
		setClock(tvClock4, hour_4, min_4);
		setClock(tvClock5, hour_5, min_5);
	}
	
	/**
	 * 设置闹钟 TextView
	 * @param tv
	 * @param hour
	 * @param min
	 */
	private void setClock(TextView tv ,int hour,int min){
		tv.setText(parseText(hour) + ":" +parseText(min));
	}
	

	
	private NumberDialog showNumberDilog(int hour,int min,String msg){
		return  new NumberDialog(this, hour, min).builder().setTitle(msg);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_home:
			finish();

			break;
		case R.id.cb_clock_1:
			isChecked_1 = !isChecked_1;
			cbClock1.setChecked(isChecked_1);
			Log.v("", "isChecked : " + isChecked_1);
			SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CHECK_BOX_CLOCK_1, isChecked_1);
			break;
		case R.id.cb_clock_2:
			isChecked_2 = !isChecked_2;
			cbClock2.setChecked(isChecked_2);
			Log.v("", "isChecked : " + isChecked_2);
			SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CHECK_BOX_CLOCK_2, isChecked_2);
			break;
		case R.id.cb_clock_3:
			isChecked_3 = !isChecked_3;
			cbClock3.setChecked(isChecked_3);
			Log.v("", "isChecked : " + isChecked_3);
			SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CHECK_BOX_CLOCK_3, isChecked_3);
			break;
		case R.id.cb_clock_4:
			isChecked_4 = !isChecked_4;
			cbClock4.setChecked(isChecked_4);
			Log.v("", "isChecked : " + isChecked_1);
			SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CHECK_BOX_CLOCK_4, isChecked_4);
			break;
		case R.id.cb_clock_5:
			isChecked_5 = !isChecked_5;
			cbClock5.setChecked(isChecked_5);
			Log.v("", "isChecked : " + isChecked_1);
			SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CHECK_BOX_CLOCK_5, isChecked_5);
			break;
		case R.id.rl_clock_1:
			final NumberDialog showNumberDilog_1= showNumberDilog(hour_1, min_1, "闹钟 1");
			showNumberDilog_1.setPositiveButton(getResources().getString(R.string.positive), new OnClickListener() {
				@Override
				public void onClick(View v) {
					int hour = showNumberDilog_1.getHour();
					int minute = showNumberDilog_1.getMinute();
					Log.v("", "hour : min - - - >" + hour + " : " + minute);
					hour_1 = hour;
					min_1 = minute;
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_HOUR_1, hour);
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_MIN_1, minute);
					setClock(tvClock1, hour, minute);
					showNumberDilog_1.dismiss();
				}
			});
			showNumberDilog_1.show();
			break;
		case R.id.rl_clock_2:
			final NumberDialog showNumberDilog_2= showNumberDilog(hour_2, min_2, "闹钟 2");
			showNumberDilog_2.setPositiveButton(getResources().getString(R.string.positive), new OnClickListener() {
				@Override
				public void onClick(View v) {
					int hour = showNumberDilog_2.getHour();
					int minute = showNumberDilog_2.getMinute();
					Log.v("", "hour : min - - - >" + hour + " : " + minute);
					hour_1 = hour;
					min_1 = minute;
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_HOUR_2, hour);
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_MIN_2, minute);
					setClock(tvClock2, hour, minute);
					showNumberDilog_2.dismiss();
				}
			});
			showNumberDilog_2.show();
			break;
		case R.id.rl_clock_3:
			final NumberDialog showNumberDilog_3= showNumberDilog(hour_3, min_3, "闹钟 3");
			showNumberDilog_3.setPositiveButton(getResources().getString(R.string.positive), new OnClickListener() {
				@Override
				public void onClick(View v) {
					int hour = showNumberDilog_3.getHour();
					int minute = showNumberDilog_3.getMinute();
					Log.v("", "hour : min - - - >" + hour + " : " + minute);
					hour_3 = hour;
					min_3 = minute;
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_HOUR_3, hour);
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_MIN_3, minute);
					setClock(tvClock3, hour, minute);
					showNumberDilog_3.dismiss();
				}
			});
			showNumberDilog_3.show();
			break;
		case R.id.rl_clock_4:
			final NumberDialog showNumberDilog_4= showNumberDilog(hour_4, min_4, "闹钟 4");
			showNumberDilog_4.setPositiveButton(getResources().getString(R.string.positive), new OnClickListener() {
				@Override
				public void onClick(View v) {
					int hour = showNumberDilog_4.getHour();
					int minute = showNumberDilog_4.getMinute();
					Log.v("", "hour : min - - - >" + hour + " : " + minute);
					hour_4 = hour;
					min_4 = minute;
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_HOUR_4, hour);
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_MIN_4, minute);
					setClock(tvClock4, hour, minute);
					showNumberDilog_4.dismiss();
				}
			});
			showNumberDilog_4.show();
			break;
		case R.id.rl_clock_5:
			final NumberDialog showNumberDilog_5= showNumberDilog(hour_5, min_5, "闹钟 5");
			showNumberDilog_5.setPositiveButton(getResources().getString(R.string.positive), new OnClickListener() {
				@Override
				public void onClick(View v) {
					int hour = showNumberDilog_5.getHour();
					int minute = showNumberDilog_5.getMinute();
					Log.v("", "hour : min - - - >" + hour + " : " + minute);
					hour_5 = hour;
					min_5 = minute;
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_HOUR_5, hour);
					SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_CLOCK_MIN_5, minute);
					setClock(tvClock5, hour, minute);
					showNumberDilog_5.dismiss();
				}
			});
			showNumberDilog_5.show();
			break;
			
			
		case R.id.tv_save:
			if (null != mSimpleBlueService && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForAlarmClock(new int[]{
						hour_1,min_1,hour_2,min_2,hour_3,min_3,hour_4,min_4,hour_5,min_5
				}, new boolean[]{
						isChecked_1,isChecked_2,isChecked_3,isChecked_4,isChecked_5
				}));
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				}, 1000);

			} else {
//				showShortToast(getStringFromResource(R.string.device_is_not_connected));
				showIosDialog(this, getStringFromResource(R.string.device_is_not_connected));
			}

			break;

		default:
			break;
		}

	}
}
