package com.mycj.mywatch.activity;

import java.util.Date;

import com.mycj.mywatch.AppInfoActivity;
import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.MessageUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.view.ActionSheetDialog;
import com.mycj.mywatch.view.ActionSheetDialog.OnSheetItemClickListener;
import com.mycj.mywatch.view.ActionSheetDialog.SheetItemColor;
import com.mycj.mywatch.view.AlertDialog;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MoreActivity extends BaseActivity implements OnClickListener {

	private FrameLayout flHome;
	private RelativeLayout rlMusic;
	private RelativeLayout rlDataManager;
	private RelativeLayout rlTimeSync;
	private RelativeLayout rlShutdown;
	private RelativeLayout rlAbout;
	private CheckBox cbReminderCall;
	private boolean isChecked;
	private ImageView imgTimeSyncLoading;
	private ImageView imgShutdown;
	private ObjectAnimator timeSyncAnimation;
	private ObjectAnimator shutdownAnimation;
	private Handler mHandler = new Handler() {
	};
	private AbstractSimpleBlueService mSimpleBlueService;
	private RelativeLayout rlHelp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
//		SharedPreferenceUtil.put(this, Constant.SHARE_CHECK_REMIND_CALL, true);
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
		setReminderCallValue();
		super.onResume();
	}

	private void setReminderCallValue() {
		isChecked = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_REMIND_CALL, true);
		cbReminderCall.setChecked(isChecked);
	}

	@Override
	public void initViews() {
		flHome = (FrameLayout) findViewById(R.id.fl_home);
		rlMusic = (RelativeLayout) findViewById(R.id.rl_music);
		rlDataManager = (RelativeLayout) findViewById(R.id.rl_data);
		rlTimeSync = (RelativeLayout) findViewById(R.id.rl_time_sync);
		rlShutdown = (RelativeLayout) findViewById(R.id.rl_shut);
		rlAbout = (RelativeLayout) findViewById(R.id.rl_about);
		rlHelp = (RelativeLayout) findViewById(R.id.rl_help);
		cbReminderCall = (CheckBox) findViewById(R.id.cb_remind_call);
		imgTimeSyncLoading = (ImageView) findViewById(R.id.img_time_sync_loading);
		imgShutdown = (ImageView) findViewById(R.id.img_shut_loading);
	}

	@Override
	public void setListener() {
		flHome.setOnClickListener(this);
		rlMusic.setOnClickListener(this);
		rlDataManager.setOnClickListener(this);
		rlTimeSync.setOnClickListener(this);
		rlShutdown.setOnClickListener(this);
		rlAbout.setOnClickListener(this);
		cbReminderCall.setOnClickListener(this);
		rlHelp.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_home:
			finish();
			break;
		case R.id.rl_music:
			startActivity(MusicActivity.class);
			break;
		case R.id.rl_data:
			startActivity(DataManagerActivity.class);
			break;
		case R.id.rl_time_sync:

			if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED) {
				imgTimeSyncLoading.setVisibility(View.VISIBLE);
				timeSyncAnimation = startAnimation(imgTimeSyncLoading);
				timeSyncAnimation.start();
				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForSyncTime(new Date()));
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						timeSyncAnimation.cancel();
						imgTimeSyncLoading.setVisibility(View.INVISIBLE);
					}
				}, 2000);
			} else {
				showIosDialog(MoreActivity.this, getResources().getString(R.string.device_is_not_connected));
//				showAlertDialog("", "请连接手环");
			}
			break;
		case R.id.rl_shut:
			
			showIosDialog();
		
			break;
		case R.id.rl_about:
			
			showIosDialog(this,  getVersion(), getResources().getString(R.string.app_name));
			
//			startActivity(AppInfoActivity.class);
//			new AlertDialog(this).builder().setTitle("app版本").setMsg().setCancelable(true).show();
			break;
		case R.id.rl_help:
			
//			showIosDialog(this,  getVersion(), getResources().getString(R.string.app_name));
			
			startActivity(AppInfoActivity.class);
//			new AlertDialog(this).builder().setTitle("app版本").setMsg().setCancelable(true).show();
			break;
		case R.id.cb_remind_call:
			isChecked = !isChecked;
			cbReminderCall.setChecked(isChecked);
			SharedPreferenceUtil.put(this, Constant.SHARE_CHECK_REMIND_CALL, isChecked);
			if (isChecked) {
				int mmsCount = MessageUtil.getNewMmsCount(getApplicationContext());
				int msmCount = MessageUtil.getNewSmsCount(getApplicationContext());
				int phoneCount = MessageUtil.readMissCall(getApplicationContext());
				doWriteUnReadPhoneAndSmsToWatch(phoneCount, (mmsCount + msmCount));
			}
			break;

		default:
			break;
		}
	}
	
	
	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return getResources().getString(R.string.app_version_info) + version;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "--";
	}
	/**
	 * 未接来电和未读短信提醒
	 */
	private void doWriteUnReadPhoneAndSmsToWatch(int phone, int sms) {
		Log.e("", "___________doWriteUnReadPhoneAndSmsToWatch" + sms);

		boolean isCallRemind;
		isCallRemind = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_REMIND_CALL, false);
		if (isCallRemind && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
			Log.e("", "___________更新短信来电数量");
			mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForMissedCallAndMessage(phone, sms));

		}
	}
	

	
	private void showIosDialog() {
		ActionSheetDialog dialog = new ActionSheetDialog(this).builder();
		dialog.setTitle(getResources().getString(R.string.close_));
		dialog.addSheetItem(getResources().getString(R.string.Exit), SheetItemColor.Red, new OnSheetItemClickListener() {
			@Override
			public void onClick(int which) {
				if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED) {
					imgShutdown.setVisibility(View.VISIBLE);
					shutdownAnimation = startAnimation(imgShutdown);
					shutdownAnimation.start();
					mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForShutDown());
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							shutdownAnimation.cancel();
							imgShutdown.setVisibility(View.INVISIBLE);
						}
					}, 2000);
				} else {
					showIosDialog(MoreActivity.this, getResources().getString(R.string.device_is_not_connected));
//					showAlertDialog("", "手环已断开连接");
				}
				
			}
		}).show();
	}
	
	
	
}
