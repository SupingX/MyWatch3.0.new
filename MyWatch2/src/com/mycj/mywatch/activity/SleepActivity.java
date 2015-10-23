package com.mycj.mywatch.activity;

import java.util.ArrayList;
import java.util.List;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.fragment.SleepFragment;
import com.mycj.mywatch.fragment.SleepHistoryFragment;
import com.mycj.mywatch.fragment.SleepSettingFragment;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.util.FileUtil;
import com.mycj.mywatch.util.FileUtils;
import com.mycj.mywatch.util.ScreenShot;
import com.mycj.mywatch.util.ShareUtil;
import com.mycj.mywatch.view.NoScrollViewPager;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothProfile;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class SleepActivity extends BaseActivity implements OnClickListener {
	private TextView tvSleep;
	private TextView tvHistory;
	private TextView tvSetting;
	private SleepFragment sleepFragment;
	private SleepHistoryFragment sleepHistoryFragment;
	private SleepSettingFragment sleepSettingFragment;
	private NoScrollViewPager sleepViewPager;
	private List<Fragment> fragments;
	
	private FrameLayout flBack;
	// private ImageView imgSync;s
	// private FrameLayout flSync;
	private ObjectAnimator startAnimation;
	private TextView tvTitle;
	private AbstractSimpleBlueService mSimpleBlueService;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			String path = (String) msg.obj;
			ShareUtil.shareImage(path, SleepActivity.this);
		};
	};
	private ImageView imgShare;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sleep);
		initViews();
		setListener();
		// 初始化tab
		updateTab(0);
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//获取今天的睡眠记录
		if (null != mSimpleBlueService && mSimpleBlueService.isBinded() && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED) {
			byte[] data = ProtocolForWrite.instance().getByteForSleepQualityOfToday(0);
			mSimpleBlueService.writeCharacteristic(data);
		}
	}

	@Override
	protected void onDestroy() {
		if (startAnimation != null) {
			startAnimation.cancel();
		}
		super.onDestroy();
	}

	@Override
	public void initViews() {
		tvSleep = (TextView) findViewById(R.id.tv_sleep_bottom);
		tvHistory = (TextView) findViewById(R.id.tv_sleep_history_bottom);
		tvSetting = (TextView) findViewById(R.id.tv_sleep_setting_bottom);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		flBack = (FrameLayout) findViewById(R.id.fl_home);
		// flSync = (FrameLayout) findViewById(R.id.fl_sync);
		// imgSync = (ImageView) findViewById(R.id.img_sync_sleep);
		imgShare = (ImageView) findViewById(R.id.img_share);
		
		//加载ViewPager
		sleepViewPager = (NoScrollViewPager) findViewById(R.id.vp_sleep);
		fragments = new ArrayList<>();
		sleepFragment = new SleepFragment();
		sleepHistoryFragment = new SleepHistoryFragment();
		sleepSettingFragment = new SleepSettingFragment();
		fragments.add(sleepFragment);
		fragments.add(sleepHistoryFragment);
		fragments.add(sleepSettingFragment);
		
		sleepViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return fragments.size();
			}
			
			@Override
			public Fragment getItem(int pos) {
				return fragments.get(pos);
			}
		});
	}

	@Override
	public void setListener() {
		tvSleep.setOnClickListener(this);
		tvHistory.setOnClickListener(this);
		tvSetting.setOnClickListener(this);
		flBack.setOnClickListener(this);
		// flSync.setOnClickListener(this);
		imgShare.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tv_sleep_bottom:
			updateTab(0);
			tvTitle.setText(R.string.sleep);
			break;
		case R.id.tv_sleep_history_bottom:
			updateTab(1);
			tvTitle.setText(R.string.history);
			break;
		case R.id.tv_sleep_setting_bottom:
			updateTab(2);
			tvTitle.setText(R.string.setting);
			break;
		case R.id.fl_home:
			finish();
			break;
		case R.id.img_share:
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = ScreenShot.takeScreenShot(SleepActivity.this);
					FileUtil.saveBitmap(bitmap);
					String path = FileUtil.getandSaveCurrentImage(SleepActivity.this, bitmap);
					if (path != null) {
						Message msg = new Message();
						msg.obj = path;
						mHandler.sendMessage(msg);
					}
				}
			});
			break;
		case R.id.fl_sync:
			// if (!isSyncing) {
			// isSyncing = true;
			// startAnimation = startAnimation(imgSync);
			// startAnimation.start();
			// //发送跟新请求
			// //请求成功，1.关闭动画 2.isSyncing = false;
			// if
			// (null!=mSimpleBlueService&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED)
			// {
			// byte[] data =
			// ProtocolForWrite.instance().getByteForSleepQualityOfToday(0);
			// mSimpleBlueService.writeCharacteristic(data);
			// }
			// mHandler.postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// startAnimation.cancel();
			// }
			// }, 2000);
			// }
			break;

		default:
			break;
		}
	}

	private void updateTab(int i) {
		clearTab();
		switch (i) {
		case 0:
			sleepViewPager.setCurrentItem(0);
			tvSleep.setTextColor(getResources().getColor(R.color.color_top_blue));
			setDrawable(tvSleep, R.drawable.ic_sleep_icon);
			imgShare.setVisibility(View.VISIBLE);
			break;
		case 1:
			sleepViewPager.setCurrentItem(1);
			tvHistory.setTextColor(getResources().getColor(R.color.color_top_blue));
			setDrawable(tvHistory, R.drawable.ic_pedo_tab_history);
			imgShare.setVisibility(View.GONE);
			break;
		case 2:
			sleepViewPager.setCurrentItem(2);
			tvSetting.setTextColor(getResources().getColor(R.color.color_top_blue));
			setDrawable(tvSetting, R.drawable.ic_pedo_tab_setting);
			imgShare.setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}

	/**
	 * 设置TextView 图片
	 * 
	 * @param tv
	 * @param resourceid
	 */
	private void setDrawable(TextView tv, int resourceid) {
		Resources res = getResources();
		Drawable img = res.getDrawable(resourceid);
		// 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
		img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
		tv.setCompoundDrawables(null, img, null, null); //
	}

	/**
	 * 更新底部选中状态
	 * 
	 * @param id
	 */
//	private void updateTab(int id) {
//		clearTab();
//		FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
//		switch (id) {
//		case 0:
//			tvSleep.setTextColor(getResources().getColor(R.color.color_top_blue));
//			setDrawable(tvSleep, R.drawable.ic_sleep_icon);
//			if (sleepFragment == null) {
//				sleepFragment = new SleepFragment();
//			}
//			imgShare.setVisibility(View.VISIBLE);
//			// imgSync.setVisibility(View.GONE);
//			beginTransaction.replace(R.id.frame_sleep, sleepFragment);
//			break;
//		case 1:
//			if (sleepHistoryFragment == null) {
//				sleepHistoryFragment = new SleepHistoryFragment();
//			}
//			beginTransaction.replace(R.id.frame_sleep, sleepHistoryFragment);
//			tvHistory.setTextColor(getResources().getColor(R.color.color_top_blue));
//			setDrawable(tvHistory, R.drawable.ic_pedo_tab_history);
//			imgShare.setVisibility(View.GONE);
//			// imgSync.setVisibility(View.VISIBLE);
//			break;
//		case 2:
//			if (sleepSettingFragment == null) {
//				sleepSettingFragment = new SleepSettingFragment();
//			}
//			beginTransaction.replace(R.id.frame_sleep, sleepSettingFragment);
//			tvSetting.setTextColor(getResources().getColor(R.color.color_top_blue));
//			setDrawable(tvSetting, R.drawable.ic_pedo_tab_setting);
//			imgShare.setVisibility(View.GONE);
//			// imgSync.setVisibility(View.GONE);
//			break;
//
//		default:
//			break;
//		}
//		 beginTransaction.addToBackStack(null);
//		beginTransaction.commitAllowingStateLoss();
//		// beginTransaction.commit();
//	}

	/**
	 * 清楚底部选中状态
	 */
	private void clearTab() {
		tvSleep.setTextColor(getResources().getColor(R.color.grey));
		tvHistory.setTextColor(getResources().getColor(R.color.grey));
		tvSetting.setTextColor(getResources().getColor(R.color.grey));
		setDrawable(tvSleep, R.drawable.ic_sleep_icon_unpressed);
		setDrawable(tvHistory, R.drawable.ic_pedo_tab_history_unpress);
		setDrawable(tvSetting, R.drawable.ic_pedo_tab_setting_unpress);
	}


}
