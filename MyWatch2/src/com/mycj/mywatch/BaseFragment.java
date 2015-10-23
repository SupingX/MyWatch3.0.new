package com.mycj.mywatch;

import java.util.Locale;

import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.view.ActionSheetDialog;
import com.mycj.mywatch.view.ActionSheetDialog.OnSheetItemClickListener;
import com.mycj.mywatch.view.ActionSheetDialog.SheetItemColor;

import android.R.anim;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class BaseFragment extends Fragment{
	public final  int RESULT_PEDO_TARGET = 0x01; 
	public final  int RESULT_PEDO_HEIGHT = 0x02; 
	public final  int RESULT_PEDO_WEIGHT = 0x03; 
	public final  int RESULT_PEDO_AGE = 0x04; 
	private AbstractSimpleBlueService mSimpleBlueService;
	public void chooseLau(String language){
		 Resources res = getResources(); 
		 Configuration config = res.getConfiguration(); 
		 DisplayMetrics dm = res.getDisplayMetrics(); 
		 if (language.equals("zh")) {
			 config.locale = Locale.SIMPLIFIED_CHINESE; 
		}else if (language.equals("en")) {
			 config.locale = Locale.ENGLISH; 
		}
		 res.updateConfiguration(config, dm);
		 SharedPreferenceUtil.put(getActivity(),Constant.SHARE_LOCALE , language);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
//		chooseLau((String)SharedPreferenceUtil.get(getActivity(), Constant.SHARE_LOCALE, "zh"));
		super.onCreate(savedInstanceState);
	}
	public void startAnimation(TextView v){
		ObjectAnimator animation = ObjectAnimator.ofFloat(v, "alpha", 1,0.5f,1f);
		animation.setDuration(1000);
		animation.start();
	}
	public int getHeight(){
		int height = (int) SharedPreferenceUtil.get(getActivity(), Constant.SHARE_PEDOMETER_HEIGHT, 175);
		return height;
	}
	
	public int getWeight(){
		int weight = (int) SharedPreferenceUtil.get(getActivity(), Constant.SHARE_PEDOMETER_WEIGHT, 65);
		return weight;
	}
	protected void showToast(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
	protected AbstractSimpleBlueService getSimpleBlueService(){
		BaseApp app = (BaseApp) getActivity().getApplication();
		return app.getSimpleBlueService();
	}
	/**
	 * 等待框
	 * @param msg
	 * @return
	 */
	protected ProgressDialog showProgressDialog(String msg) {
		ProgressDialog pDialog;
		pDialog = new ProgressDialog(getActivity());
			pDialog.setCancelable(false);
			pDialog.setMessage(msg);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.show();
			return pDialog;
	}
	
}
