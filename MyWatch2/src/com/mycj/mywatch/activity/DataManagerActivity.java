package com.mycj.mywatch.activity;

import org.litepal.crud.DataSupport;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.R.layout;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.bean.HeartRateData;
import com.mycj.mywatch.bean.SleepData;
import com.mycj.mywatch.bean.PedoData;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.SharedPreferenceUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class DataManagerActivity extends BaseActivity implements OnClickListener {

	private RelativeLayout rlSyncAll;
	private RelativeLayout rlClearAll;
	private FrameLayout frMore;
	private AbstractSimpleBlueService mSimpleBlueService;
	private Handler mHandler = new Handler() {

	};
	private RelativeLayout rlMore;
	private ProgressDialog clearDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_manager);
		initViews();
		setListener();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();

	}

	@Override
	public void initViews() {
		rlSyncAll = (RelativeLayout) findViewById(R.id.rl_sync_all);
		rlClearAll = (RelativeLayout) findViewById(R.id.rl_clear_all);
		rlMore = (RelativeLayout) findViewById(R.id.rl_setting);
	}

	@Override
	public void setListener() {
		rlSyncAll.setOnClickListener(this);
		rlClearAll.setOnClickListener(this);
		rlMore.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_sync_all:
			if (null != mSimpleBlueService && mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
				mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForSyncHistoryData());
				final ProgressDialog syncDialog = showProgressDialog(getString(R.string.sync_all_ing));
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						syncDialog.dismiss();
					}
				}, 15 * 1000);
			} else {
//				showAlertDialog("", getStringFromResource(R.string.device_is_not_connected));
				showIosDialog(this, getStringFromResource(R.string.device_is_not_connected));
			}
			break;
		case R.id.rl_clear_all:
			// 提示窗口
			clearDialog = showProgressDialog(getStringFromResource(R.string.is_deleteing));
			// 删除数据库
			DataSupport.deleteAll(PedoData.class);
			DataSupport.deleteAll(SleepData.class);
			DataSupport.deleteAll(HeartRateData.class);
			// 清空第一次同步时间
			SharedPreferenceUtil.put(this, Constant.SHARE_UPDATE_TIME,	0L);
			// 延迟1.5秒 关闭窗口
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					clearDialog.dismiss();
				}
			}, 1500);
			break;
		case R.id.rl_setting:
			finish();
			break;

		default:
			break;
		}
	}
}
