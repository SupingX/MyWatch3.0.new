package com.mycj.mywatch.activity;


import java.util.ArrayList;
import java.util.List;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.fragment.HeartRateFragment;
import com.mycj.mywatch.fragment.HeartRateHistoryFragment;
import com.mycj.mywatch.fragment.HeartRateSettingFragment;
import com.mycj.mywatch.util.FileUtil;
import com.mycj.mywatch.util.ScreenShot;
import com.mycj.mywatch.util.ShareUtil;
import com.mycj.mywatch.view.NoScrollViewPager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class HeartRateActivity extends BaseActivity implements OnClickListener{

	private TextView tvHreatRate;
	private TextView tvHistory;
	private TextView tvSetting;
	private FrameLayout rlBack;
	private TextView tvTitle;
	private HeartRateFragment hrFragment;
	private HeartRateHistoryFragment hrHistoryFragment;
	private HeartRateSettingFragment hrSettingFragment;
	private NoScrollViewPager hrViewPager;
	private List<Fragment> fragments;
//	private TextView tvSave;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			String path  = (String) msg.obj;
			ShareUtil.shareImage(path, HeartRateActivity.this);
		};
	};
	private ImageView imgShare;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heart_rate);

		initViews();
		setListener();
		//初始化tab
		updateTab(0);
	}
	
	@Override
	public void initViews() {
		tvHreatRate = (TextView) findViewById(R.id.tv_hr_bottom);
		tvHistory = (TextView) findViewById(R.id.tv_history_bottom);
		tvSetting = (TextView) findViewById(R.id.tv_setting_bottom);
//		tvSave = (TextView) findViewById(R.id.tv_save);
		tvTitle = (TextView) findViewById(R.id.tv_pedo_title);
		rlBack = (FrameLayout) findViewById(R.id.fl_home);
		imgShare = (ImageView) findViewById(R.id.img_share);
		// 加载ViewPager
				hrViewPager = (NoScrollViewPager) findViewById(R.id.vp_hr);
				fragments = new ArrayList<>();
				hrFragment = new HeartRateFragment();
				hrHistoryFragment = new HeartRateHistoryFragment();
				hrSettingFragment = new HeartRateSettingFragment();
				fragments.add(hrFragment);
				fragments.add(hrHistoryFragment);
				fragments.add(hrSettingFragment);
				hrViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
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
		tvHreatRate.setOnClickListener(this);
		tvHistory.setOnClickListener(this);
		tvSetting.setOnClickListener(this);
		rlBack.setOnClickListener(this);
//		tvSave.setOnClickListener(this);
		imgShare.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tv_hr_bottom:
			updateTab(0);
			break;
		case R.id.tv_history_bottom:
			updateTab(1);
			break;
		case R.id.tv_setting_bottom:
			updateTab(2);
			break;
		case R.id.fl_home:
			finish();
			break;
		case R.id.img_share:
			mHandler.post(new Runnable() {
				@Override
				public void run() {
				Bitmap bitmap = ScreenShot.takeScreenShot(HeartRateActivity.this);
				String path = FileUtil.getandSaveCurrentImage(HeartRateActivity.this,bitmap);
				if (path!=null) {
					Message msg = new Message();
					msg.obj = path;
					mHandler.sendMessage(msg);
				}
				}
			});
			
		default:
			break;
		}
	}
	
	private void updateTab(int i) {
		clearTab();
		switch (i) {
		case 0:
			hrViewPager.setCurrentItem(0);
			tvHreatRate.setTextColor(getResources().getColor(R.color.color_top_blue));
			tvTitle.setText(getResources().getString(R.string.heart_rate));
			setDrawable(tvHreatRate, R.drawable.ic_tab_hr);
			imgShare.setVisibility(View.VISIBLE);
			break;
		case 1:
			hrViewPager.setCurrentItem(1);
			imgShare.setVisibility(View.GONE);
			tvTitle.setText(getResources().getString(R.string.history));
			tvHistory.setTextColor(getResources().getColor(R.color.color_top_blue));
			setDrawable(tvHistory, R.drawable.ic_pedo_tab_history);
			break;
		case 2:
			hrViewPager.setCurrentItem(2);
			imgShare.setVisibility(View.GONE);
			tvTitle.setText(getResources().getString(R.string.setting));
			tvSetting.setTextColor(getResources().getColor(R.color.color_top_blue));
			setDrawable(tvSetting, R.drawable.ic_pedo_tab_setting);
			break;

		default:
			break;
		}
	}
	/**
		 * 更新底部选中状态
		 * @param id
		 */
//		private void updateTab(int id) {
//			clearTab();
//			FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
//			switch (id) {
//			case 0:
//				tvHreatRate.setTextColor(getResources().getColor(R.color.color_top_blue));
//				tvTitle.setText(getResources().getString(R.string.heart_rate));
//				setDrawable(tvHreatRate, R.drawable.ic_tab_hr);
//				imgShare.setVisibility(View.VISIBLE);
//				if(hrFragment==null){
//					hrFragment = new HeartRateFragment();
//				}
//				beginTransaction.replace(R.id.frame_heart_rate, hrFragment);
//				break;
//			case 1:
//				if(hrHistoryFragment==null){
//					hrHistoryFragment = new HeartRateHistoryFragment();
//				}
//				imgShare.setVisibility(View.GONE);
//				tvTitle.setText(getResources().getString(R.string.history));
//				beginTransaction.replace(R.id.frame_heart_rate, hrHistoryFragment);
//				tvHistory.setTextColor(getResources().getColor(R.color.color_top_blue));
//				setDrawable(tvHistory, R.drawable.ic_pedo_tab_history);
//				break;
//			case 2:
//				if(hrSettingFragment==null){
//					hrSettingFragment = new HeartRateSettingFragment();
//				}
//				imgShare.setVisibility(View.GONE);
//				tvTitle.setText(getResources().getString(R.string.setting));
//				beginTransaction.replace(R.id.frame_heart_rate, hrSettingFragment);
//				tvSetting.setTextColor(getResources().getColor(R.color.color_top_blue));
//				setDrawable(tvSetting, R.drawable.ic_pedo_tab_setting);
//				break;
//	
//			default:
//				break;
//			}
//			beginTransaction.addToBackStack(null);
//			beginTransaction.commitAllowingStateLoss();
//	//		beginTransaction.commit();
//		}



	/**
	 * 清楚底部选中状态
	 */
	private void clearTab() {
		tvHreatRate.setTextColor(getResources().getColor(R.color.grey));
		tvHistory.setTextColor(getResources().getColor(R.color.grey));
		tvSetting.setTextColor(getResources().getColor(R.color.grey));
		setDrawable(tvHreatRate, R.drawable.ic_tab_hr_unpressed);
		setDrawable(tvHistory, R.drawable.ic_pedo_tab_history_unpress);
		setDrawable(tvSetting, R.drawable.ic_pedo_tab_setting_unpress);
	}

	/**
	 * 设置TextView 图片
	 * @param tv
	 * @param resourceid
	 */
	private void setDrawable(TextView tv , int resourceid){
		 Resources res = getResources();
		 Drawable img = res.getDrawable(resourceid);
		 // 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
		 img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
		 tv.setCompoundDrawables(null, img, null, null); //
	}
	
	 public boolean onTouchEvent(MotionEvent event) {
	        if(null != this.getCurrentFocus()){
	            /**
	             * 点击空白位置 隐藏软键盘
	             */
	            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	        }
	        return super .onTouchEvent(event);
	 }
	 
	 @Override
	public void onBackPressed() {
		 finish();
		super.onBackPressed();
	}
}
