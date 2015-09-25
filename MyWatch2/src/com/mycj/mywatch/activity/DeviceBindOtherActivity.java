package com.mycj.mywatch.activity;

import java.util.ArrayList;
import java.util.List;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.service.AbstractSimpleBlueService;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeviceBindOtherActivity extends BaseActivity implements OnClickListener {

	private ProgressDialog progressDialog;
	private List<BluetoothDevice> listDevices;
	private int[] rssis = new int[100];
	private MyAdapter mAdapter;
	private ListView lvDevice;
	private Vibrator vibrator;
	private AbstractSimpleBlueService mSimpleBlueService;
	private ImageView imgLoading;
	private RelativeLayout rlDevice;
	private ObjectAnimator startAnimation;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				break;
			case 0x0101:

				break;

			default:
				break;
			}
		};
	};
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SimpleBlueService.ACTION_DEVICE_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(SimpleBlueService.EXTRA_DEVICE);
				int rssi = intent.getIntExtra(SimpleBlueService.EXTRA_RSSI, 0);

				if (!listDevices.contains(device)) {
					listDevices.add(device);
					rssis[listDevices.size() - 1] = rssi;
				} else {
					int pos = listDevices.indexOf(device);
					rssis[pos] = rssi;
				}
				mAdapter.notifyDataSetChanged();
			} else if (action.equals(SimpleBlueService.ACTION_SERVICE_DISCOVERED_WRITE_DEVICE)) {
				Log.e("", "------------已连接跳转吧-----------");
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// 控制手机震动2秒
						vibrator.vibrate(2000);
						startActivity(DeviceActivity.class);
						finish();
					}
				});
			
				
		} else if (action.equals(SimpleBlueService.ACTION_SERVICE_DISCOVERED_WONG_DEVICE)) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			Log.e("", "-----------链接失败-----------");
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mSimpleBlueService.close();
					mSimpleBlueService.scanDevice(true);
				}
			});
			
		}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_bind_other);
		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		listDevices = new ArrayList<>();
		mAdapter = new MyAdapter();
		initViews();
		setListener();

	}

	@Override
	protected void onStart() {
		super.onStart();
		startAnimation = startAnimation(imgLoading);
		mSimpleBlueService = getSimpleBlueService();
		Log.e("", "mSimpleBlueService : " + mSimpleBlueService);
		registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());
	}

	@Override
	protected void onResume() {
		checkBlue();
		startAnimation.start();
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mReceiver);
		isOnceEnter = true;
		
	}

	@Override
	protected void onDestroy() {
		if (startAnimation != null) {
			startAnimation.cancel();
		}
		if (null!=mSimpleBlueService&&mSimpleBlueService.isScanning()) {
			mSimpleBlueService.scanDevice(false);
		}
		listDevices.clear();
		super.onDestroy();
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
	boolean isOnceEnter = true;
	/**
	 * 确认蓝牙是否打开
	 */
	private void checkBlue() {
		Log.e("", "-----检查蓝牙-----");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mSimpleBlueService != null) {
					// 确认蓝牙
					if (!mSimpleBlueService.isEnable()) {
						if (isOnceEnter) {
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//						startActivityForResult(enableBtIntent, 1);
						startActivity(enableBtIntent);
						// showIosDialog();
						isOnceEnter = false;
						}
					} else {
						if (mSimpleBlueService.isScanning()) {
							mSimpleBlueService.scanDevice(false);
						}
						mSimpleBlueService.scanDevice(true);
					}
				} else {
					Log.e("", "-----检查蓝牙-----service为空");
				}

			}
		});
	}


	@Override
	public void initViews() {
		lvDevice = (ListView) findViewById(R.id.lv_device);
		lvDevice.setAdapter(mAdapter);
		rlDevice = (RelativeLayout) findViewById(R.id.rl_device);
		imgLoading = (ImageView) findViewById(R.id.ic_loading);
	}

	@Override
	public void setListener() {
		rlDevice.setOnClickListener(this);
		imgLoading.setOnClickListener(this);
		lvDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
//				mSimpleBlueService.scanDevice(false);
				progressDialog = showProgressDialog(getResources().getString(R.string.in_conntecting),true);
				final BluetoothDevice device = listDevices.get(position);
//				if (mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
					mSimpleBlueService.close();
		
					mSimpleBlueService.unSaveDevice();
//				}
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mSimpleBlueService.connect(device);
						mSimpleBlueService.scanDevice(false);
					}
				}, 2 * 1000);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_device:
			startActivity(new Intent(this, DeviceActivity.class));
			finish();
			break;
		case R.id.ic_loading:
			listDevices.clear();
			 if(mSimpleBlueService.isScanning()){
				 mSimpleBlueService.scanDevice(false);
			 }
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mSimpleBlueService.scanDevice(true);
				}
			}, 1000);
			break;

		default:
			break;
		}
	}
	
	/**
	 * 设备
	 * @author Administrator
	 *
	 */
	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listDevices.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.item_device, parent, false);
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
				holder.tvRssi = (TextView) convertView.findViewById(R.id.tv_rssi);
				holder.imgRssi = (ImageView) convertView.findViewById(R.id.img_rssi);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvName.setText(listDevices.get(position).getName());
			holder.tvAddress.setText(listDevices.get(position).getAddress());

			// holder.tvRssi.setText(String.valueOf(listRssis.get(position).intValue()));
			// holder.imgRssi.setImageResource(getRssiImg(listRssis.get(position)));
			holder.tvRssi.setText(String.valueOf(rssis[position]));
			holder.imgRssi.setImageResource(getRssiImg(rssis[position]));
			return convertView;
		}

		class ViewHolder {
			TextView tvName;
			TextView tvAddress;
			TextView tvRssi;
			ImageView imgRssi;
		}
	}

}
