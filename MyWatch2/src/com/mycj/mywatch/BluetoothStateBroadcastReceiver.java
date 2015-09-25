package com.mycj.mywatch;

import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class BluetoothStateBroadcastReceiver extends AbstractBluetoothStateBroadcastReceiver {

	private Context context;
	private AbstractSimpleBlueService mSimpleBlueService;

	public BluetoothStateBroadcastReceiver() {

	}

	public BluetoothStateBroadcastReceiver(Context context,AbstractSimpleBlueService mSimpleBlueService) {
		this.context = context;
		this.mSimpleBlueService = mSimpleBlueService;
	}

	@Override
	public void onBluetoothChange(int state, int previousState) {
		switch (state) {
		case BluetoothAdapter.STATE_ON:
			Log.i("", "蓝牙已打开");
			mSimpleBlueService.scanDevice(true);
			break;
		case BluetoothAdapter.STATE_TURNING_OFF:
			Log.i("", "蓝牙关闭中。。。");
			break;
		case BluetoothAdapter.STATE_TURNING_ON:
			Log.i("", "蓝牙打开中。。。");
			break;
		case BluetoothAdapter.STATE_OFF:
			Log.i("", "蓝牙已关闭");
			if (mSimpleBlueService.isScanning()) {
				mSimpleBlueService.scanDevice(false);
			}
			Toast.makeText(context, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	
	}

}
