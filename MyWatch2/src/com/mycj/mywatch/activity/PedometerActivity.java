package com.mycj.mywatch.activity;


import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.fragment.PedoFragment;
import com.mycj.mywatch.fragment.PedoHistoryFragment;
import com.mycj.mywatch.fragment.PedoSettingFragment;
import com.mycj.mywatch.util.FileUtil;
import com.mycj.mywatch.util.ScreenShot;
import com.mycj.mywatch.util.ShareUtil;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("CommitTransaction")
public class PedometerActivity extends BaseActivity implements OnClickListener{

	private TextView tvPedo;
	private TextView tvHistory;
	private TextView tvSetting;
	private PedoFragment pedoFragment;
	private PedoHistoryFragment pedoHistoryFragment;
	private PedoSettingFragment pedoSettingFragment;
	private FrameLayout rlBack;
	private TextView tvTitle;
	private ImageView imgShare;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			String path  = (String) msg.obj;
			ShareUtil.shareImage(path, PedometerActivity.this);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pedometer);
		initViews();
		setListener();
		//初始化tab
		updateTab(0);
	}
	
	@Override
	public void initViews() {
		tvPedo = (TextView) findViewById(R.id.tv_pedo_bottom);
		tvHistory = (TextView) findViewById(R.id.tv_history_bottom);
		tvSetting = (TextView) findViewById(R.id.tv_setting_bottom);
		tvTitle = (TextView) findViewById(R.id.tv_pedo_title);
		rlBack = (FrameLayout) findViewById(R.id.fl_home);
		imgShare = (ImageView) findViewById(R.id.img_share);
		
	
	}


	@Override
	public void setListener() {
		tvPedo.setOnClickListener(this);
		tvHistory.setOnClickListener(this);
		tvSetting.setOnClickListener(this);
		rlBack.setOnClickListener(this);
		imgShare.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tv_pedo_bottom:
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
				Bitmap bitmap = ScreenShot.takeScreenShot(PedometerActivity.this);
				String path = FileUtil.getandSaveCurrentImage(PedometerActivity.this,bitmap);
				if (path!=null) {
					Message msg = new Message();
					msg.obj = path;
					mHandler.sendMessage(msg);
				}
				}
			});
			
			break;
		default:
			break;
		}
	}

	/**
		 * 更新底部选中状态
		 * @param id
		 */
		private void updateTab(int id) {
			clearTab();
			FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
			switch (id) {
			case 0:
				tvPedo.setTextColor(getResources().getColor(R.color.color_top_blue));
				tvTitle.setText(R.string.pedometer);
				setDrawable(tvPedo, R.drawable.ic_pedo_tab_icon);
				imgShare.setVisibility(View.VISIBLE);
				if(pedoFragment==null){
					pedoFragment = new PedoFragment();
				}
				beginTransaction.replace(R.id.frame_pedo, pedoFragment);
				break;
			case 1:
				if(pedoHistoryFragment==null){
					pedoHistoryFragment = new PedoHistoryFragment();
				}
				tvTitle.setText(R.string.history);
				beginTransaction.replace(R.id.frame_pedo, pedoHistoryFragment);
				tvHistory.setTextColor(getResources().getColor(R.color.color_top_blue));
				setDrawable(tvHistory, R.drawable.ic_pedo_tab_history);
				imgShare.setVisibility(View.GONE);
				break;
			case 2:
				if(pedoSettingFragment==null){
					pedoSettingFragment = new PedoSettingFragment();
				}
				tvTitle.setText(R.string.setting);
				beginTransaction.replace(R.id.frame_pedo, pedoSettingFragment);
				tvSetting.setTextColor(getResources().getColor(R.color.color_top_blue));
				setDrawable(tvSetting, R.drawable.ic_pedo_tab_setting);
				imgShare.setVisibility(View.GONE);
				break;
	
			default:
				break;
			}
//			beginTransaction.addToBackStack(null);
			beginTransaction.commitAllowingStateLoss();
	//		beginTransaction.commit();
		}

	/**
	 * 清楚底部选中状态
	 */
	private void clearTab() {
		tvPedo.setTextColor(getResources().getColor(R.color.grey));
		tvHistory.setTextColor(getResources().getColor(R.color.grey));
		tvSetting.setTextColor(getResources().getColor(R.color.grey));
		setDrawable(tvPedo, R.drawable.ic_pedo_tab_icon_unpress);
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
	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}
	
}
