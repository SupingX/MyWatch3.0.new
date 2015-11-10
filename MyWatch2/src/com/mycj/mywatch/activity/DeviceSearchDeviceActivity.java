package com.mycj.mywatch.activity;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.view.AlertDialog;
import com.mycj.mywatch.view.RadarView;

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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeviceSearchDeviceActivity extends BaseActivity implements OnClickListener {

	private RadarView radar;
	private AbstractSimpleBlueService mSimpleBlueService;
	private Runnable runRssi;
	private TextView tvRssi;
	private TextView tvDisconnect;
	private ImageView imgRssi;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AbstractSimpleBlueService.ACTION_REMOTE_RSSI)) {
				final int rssi = intent.getExtras().getInt(AbstractSimpleBlueService.EXTRA_RSSI);
				runOnUiThread(new Runnable() {
					public void run() {
						tvRssi.setText(String.valueOf(rssi));
						imgRssi.setImageResource(getRssiImg(rssi));
						tvDisconnect.setText("");
					}
				});
			} else if (action.equals(AbstractSimpleBlueService.ACTION_CONNECTION_STATE)) {
				final Integer state = intent.getExtras().getInt(AbstractSimpleBlueService.EXTRA_CONNECT_STATE);
				runOnUiThread( new Runnable() {
					public void run() {
						setDeviceConnectState(state);
					}
				});
				switch (state) {
				case BluetoothProfile.STATE_CONNECTED:
					mHandler.removeCallbacks(runRssi);
					mHandler.post(runRssi);
					break;
				case BluetoothProfile.STATE_DISCONNECTING:
					break;
				case BluetoothProfile.STATE_CONNECTING:
					break;
				case BluetoothProfile.STATE_DISCONNECTED:
					mHandler.removeCallbacks(runRssi);
					break;
				default:
					break;
				}
			} else if (action.equals(AbstractSimpleBlueService.ACTION_CONNECTION_STATE)) {
				final Integer rssi = intent.getExtras().getInt(AbstractSimpleBlueService.EXTRA_RSSI);
				runOnUiThread( new Runnable() {
					public void run() {
						tvRssi.setText(rssi);
						imgRssi.setImageResource(getRssiImg(rssi));
					}
				});
			}

		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				mHandler.removeCallbacks(runRssi);
				mHandler.postDelayed(runRssi, 6000);
				break;

			default:
				break;
			}
		};
	};

	private RelativeLayout rlDevice;
	private ProgressDialog startDialog;
	private ProgressDialog exitProgressDialog;
	private com.mycj.mywatch.view.AlertDialog exitDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_search_device);

		initViews();
		setListener();

	}

	@Override
	protected void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();
		Log.e("", "mSimpleBlueService : " + mSimpleBlueService);
		registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());
		runRssi = new Runnable() {
			@Override
			public void run() {
				Log.v("DeviceSearchDeviceActivity", "______请求rssi_______");
				if (mSimpleBlueService!=null) {
					mSimpleBlueService.readRemoteRssi();
					mHandler.sendEmptyMessage(0);
				}
			}
		};
		startDialog = showProgressDialog(getString(R.string.in_searching),false);
	}

	@Override
	protected void onResume() {
		radar.start();
		
		if (mSimpleBlueService!=null) {
			final int state = mSimpleBlueService.getConnectState();
			setDeviceConnectState(state);
		}
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mSimpleBlueService!=null&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
					mHandler.post(runRssi);
					mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForAvoidLose(0x01));
				}
				startDialog.dismiss();
			}
		}, 3000);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(runRssi);
		radar.stop();
		if (startDialog!=null&&startDialog.isShowing()) {
			startDialog.dismiss();
		}
		
		super.onPause();
	}

	private void setDeviceConnectState(int state) {
		switch (state) {
		case BluetoothProfile.STATE_CONNECTED:
			if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded()) {
				tvDisconnect.setText("");
			}
			break;
		case BluetoothProfile.STATE_DISCONNECTING:
			// tvDeviceConnectState.setText("断开连接中");
			break;
		case BluetoothProfile.STATE_CONNECTING:
			tvDisconnect.setText(R.string.is_connecting);
			break;
		case BluetoothProfile.STATE_DISCONNECTED:
			tvDisconnect.setText(R.string.disconnected);
			tvRssi.setText("--");
			imgRssi.setImageResource(getRssiImg(0));
			break;
		default:
			// tvDeviceConnectState.setText("未知");
			break;
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
//		beforeFinish();
		unregisterReceiver(mReceiver);

	
	}

	@Override
	protected void onDestroy() {
		if (startDialog!=null&&startDialog.isShowing()) {
			startDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void initViews() {
		radar = (RadarView) findViewById(R.id.radar);
		tvRssi = (TextView) findViewById(R.id.tv_search_rssi);
		tvDisconnect = (TextView) findViewById(R.id.tv_search_disconnect);
		imgRssi = (ImageView) findViewById(R.id.img_search_rssi);
		rlDevice = (RelativeLayout) findViewById(R.id.rl_device);

	}

	@Override
	public void setListener() {
		rlDevice.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_device:
			mHandler.post(new Runnable() {
				@Override
				public void run() {
				
				}
			});
			
//			if (state == BluetoothProfile.STATE_CONNECTED) {
	//	
//				
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForAvoidLose(0xA1));
//					}
//				}).start();;
//				mHandler.removeCallbacks(runRssi);
//			}
			
			showIOS();
//			beforeFinish();
	
			break;

		default:
			break;
		}
	}
	
	public void showIOS(){
	exitDialog = new com.mycj.mywatch.view.AlertDialog(this).builder()
				.setMsg(getString(R.string.Exit))
				.setPositiveButton(getString(R.string.positive), new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (mSimpleBlueService!=null&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
							mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForAvoidLose(0xA1));
						}
						exitDialog.dismiss();
						finish();
					}
				});
		exitDialog.show();
	}
	
	private void beforeFinish(){
		if (exitProgressDialog==null) {
			exitProgressDialog = showProgressDialog(getResources().getString(R.string.stop_searching),true);
		}else{
			exitProgressDialog.show();
		}
		
		
		if (mSimpleBlueService!=null&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
			mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForAvoidLose(0xA1));
//			if (exitProgressDialog!=null && !exitProgressDialog.isShowing()) {
//				exitProgressDialog.show();
//			}
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					finish();
				}
			},2000);
		}else{
			finish();
		}
	}
	
	@Override
	public void onBackPressed() {
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				if (mSimpleBlueService!=null&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
//					mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForAvoidLose(0xA1));
//				}
//			}
//		});
//		beforeFinish();
//		super.onBackPressed();
		showIOS();
	}
	
}
