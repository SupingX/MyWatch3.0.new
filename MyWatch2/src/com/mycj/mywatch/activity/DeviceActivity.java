package com.mycj.mywatch.activity;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.MusicService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.view.ActionSheetDialog;
import com.mycj.mywatch.view.ActionSheetDialog.OnSheetItemClickListener;
import com.mycj.mywatch.view.ActionSheetDialog.SheetItemColor;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeviceActivity extends BaseActivity implements OnClickListener {

	private FrameLayout flHome;
	private RelativeLayout rlSearch;
	private RelativeLayout rlBindOther;
	private TextView tvDeviceName;
	private TextView tvDeviceAddress;
	private TextView tvDeviceConnectState;
	private ImageView imgDeviceExit;
	private CheckBox cbRemind;
	private AbstractSimpleBlueService mSimpleBlueService;

	private boolean isRemindOpen;

	private Handler mHandler = new Handler() {
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SimpleBlueService.ACTION_SERVICE_DISCOVERED_WRITE_DEVICE)) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setCurrentDevice();
					}
				});
		
			} else if (action.equals(SimpleBlueService.ACTION_CONNECTION_STATE)) {
				final int state = intent.getExtras().getInt(SimpleBlueService.EXTRA_CONNECT_STATE);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setDeviceConnectState(state);
					}
				});
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device);
		initViews();
		setListener();

	}

	@Override
	protected void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();
		Log.e("", "mSimpleBlueService : " + mSimpleBlueService);
		registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());
	}

	@Override
	protected void onResume() {
		isRemindOpen = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_REMIND, false);//
		setCurrentDevice();
		setDeviceConnectState(mSimpleBlueService.getConnectState());
		setCheckBoxRemind();
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mReceiver);

	}

	@Override
	protected void onDestroy() {
	
		super.onDestroy();
	}

	@Override
	public void initViews() {
		flHome = (FrameLayout) findViewById(R.id.fl_home);
		rlSearch = (RelativeLayout) findViewById(R.id.rl_search);
		rlBindOther = (RelativeLayout) findViewById(R.id.rl_bind_other);
		cbRemind = (CheckBox) findViewById(R.id.cb_remind);
		imgDeviceExit = (ImageView) findViewById(R.id.img_current_device_exit);

		tvDeviceName = (TextView) findViewById(R.id.tv_current_device_name);
		tvDeviceAddress = (TextView) findViewById(R.id.tv_current_device_address);
		tvDeviceConnectState = (TextView) findViewById(R.id.tv_current_device_connect_state);

	}

	@Override
	public void setListener() {
		flHome.setOnClickListener(this);
		rlSearch.setOnClickListener(this);
		rlBindOther.setOnClickListener(this);
		imgDeviceExit.setOnClickListener(this);
		cbRemind.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_home:
			finish();
			break;
		case R.id.rl_search:
			startActivity(DeviceSearchDeviceActivity.class);
			break;
		case R.id.rl_bind_other:
			startActivity(DeviceBindOtherActivity.class);
			break;
		case R.id.cb_remind:
			isRemindOpen = !isRemindOpen;
			cbRemind.setChecked(isRemindOpen);
			SharedPreferenceUtil.put(this, Constant.SHARE_CHECK_REMIND, isRemindOpen);
			final ProgressDialog syncDialog = showProgressDialog(getString(R.string.is_setting));
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					syncDialog.dismiss();
				}
			}, 500);

			break;
		case R.id.img_current_device_exit:
			exitBinded();
			break;

		default:
			break;
		}
	}

	/**
	 * 设置本地绑定设备
	 */
	private void setCurrentDevice() {
		if (mSimpleBlueService!=null) {
			String name = mSimpleBlueService.getBindedDeviceName();
			String address = mSimpleBlueService.getBindedDeviceAddress();
			tvDeviceName.setText(name.equals("--") ? "" : name);
			tvDeviceAddress.setText(address.equals("--") ? getString(R.string.no_binding) : address);
		}else{
			tvDeviceName.setText("--");
			tvDeviceAddress.setText("--");
		}
	}

	/**
	 * 设置蓝牙连接状态
	 * 
	 * @param state
	 */
	private void setDeviceConnectState(int state) {
		// if (mLiteBlueService.isConnected()) {
		// tvDeviceConnectState.setText(R.string.is_connected);
		// } else if (mLiteBlueService.isServiceDiscovering()) {
		// tvDeviceConnectState.setText(R.string.is_connecting);
		// } else if
		// (mLiteBlueService.isServiceDiscovered()&&mLiteBlueService.isBinded())
		// {
		// tvDeviceConnectState.setText(R.string.binded);
		// } else {
		// tvDeviceConnectState.setText(R.string.disconnected);
		// }
		switch (state) {
		case BluetoothProfile.STATE_CONNECTED:
			if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED) {
				tvDeviceConnectState.setText(R.string.is_connected);
			}
			break;
		case BluetoothProfile.STATE_DISCONNECTING:
			// tvDeviceConnectState.setText("断开连接中");
			break;
		case BluetoothProfile.STATE_CONNECTING:
			tvDeviceConnectState.setText(R.string.is_connecting);
			break;
		case BluetoothProfile.STATE_DISCONNECTED:
			tvDeviceConnectState.setText(R.string.disconnected);
			break;
		default:
			// tvDeviceConnectState.setText("未知");
			break;
		}

	}

	/**
	 * 设置是否打开提醒
	 */
	private void setCheckBoxRemind() {
		isRemindOpen = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_REMIND, false);
		cbRemind.setChecked(isRemindOpen);
	}

	/**
	 * 弹出进度 （旧）
	 */
	private void showIosDialog() {
		ActionSheetDialog dialog = new ActionSheetDialog(this).builder();
		dialog.setTitle(getString(R.string.enable_blue));
		dialog.addSheetItem(getString(R.string.open), SheetItemColor.Red, new OnSheetItemClickListener() {
			@Override
			public void onClick(int which) {

				// mLiteBlueService.enable();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// if (mLiteBlueService.isEnable()) {
						// // 蓝牙打开
						// if
						// (mLiteBlueService.isBinded()&&!mLiteBlueService.isServiceDiscovered())
						// {// 当本地绑定时，开启搜索。
						// mLiteBlueService.startScanUsePeriodScanCallback();
						// }
						// } else {
						// // 未打开
						// }
					}
				}, 3000);

			}
		}).show();
	}

	/**
	 * 解除绑定设备
	 */
	private void exitBinded() {
		if (mSimpleBlueService!=null&&mSimpleBlueService.isBinded()) {
			final ActionSheetDialog dialog = new ActionSheetDialog(this).builder();
			dialog.setTitle(getResources().getString(R.string.contact_binding));
			dialog.addSheetItem(getResources().getString(R.string.confirm), SheetItemColor.Red, new OnSheetItemClickListener() {
				@Override
				public void onClick(int which) {

					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							// 1.先清除本地设备
							mSimpleBlueService.unSaveDevice();
							// 2.关闭连接（当时连接状态时）
							// if (mSimpleBlueService.getConnectState() ==
							// BluetoothProfile.STATE_CONNECTED) {
							mSimpleBlueService.close();
							mSimpleBlueService.setConnectState(BluetoothProfile.STATE_DISCONNECTED);
							Intent intent = new Intent(AbstractSimpleBlueService.ACTION_CONNECTION_STATE);
							intent.putExtra(AbstractSimpleBlueService.EXTRA_CONNECT_STATE, BluetoothProfile.STATE_DISCONNECTED);
							sendBroadcast(intent);
							MusicService ms = getMusicService();
							if (ms!=null&&ms.isPlaying()) {
								ms.stop();
							}
							
							// }
							// 3.更新UI
							setCurrentDevice();
//							setDeviceConnectState(BluetoothProfile.STATE_DISCONNECTED);
						}
					});
				}
			}).show();
		} else {
			showShortToast(R.string.no_device_binding);
		}
	}

}
