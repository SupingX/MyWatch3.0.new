package com.mycj.mywatch.fragment;

import java.util.Random;

import com.mycj.mywatch.BaseFragment;
import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.DataUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.util.WatchDataUtil;
import com.mycj.mywatch.view.StepArcView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PedoFragment extends BaseFragment{
	private StepArcView stepCircleView;
	private Handler mHandler = new Handler(){
		
	};
	private TextView tvComplete;
	private TextView tvCompleteGre;
	private TextView tvPedoMax;
	private TextView tvCal;
	private TextView tvDistance;
	private TextView tvTime;
	private int max;
	private int completeStep;
	private AbstractSimpleBlueService mSimpleBlueService;
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AbstractSimpleBlueService.ACTION_SERVICE_DISCOVERED_WRITE_DEVICE)) {
				
			}else if (action.equals(SimpleBlueService.ACTION_DATA_STEP)) {	
				final int[] datas = intent.getIntArrayExtra(SimpleBlueService.EXTRA_STEP);
				if (datas!=null) {
					completeStep = datas[0];
					getActivity().runOnUiThread(new  Runnable() {
						public void run() {
							setCompleteStepValue();
							setCompleteCal(datas[0], datas[3], datas[4], datas[5]);
//							setCompleteCal(datas[1]);
							setCompleteDistance(datas[0]);
							setCompleteTime(datas[3], datas[4], datas[5]);
						}
					});
				}
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		View view = inflater.inflate(R.layout.fragment_pedo_pedo, container,false);
		stepCircleView = (StepArcView) view.findViewById(R.id.scv);
		stepCircleView.setMax(500);
		stepCircleView.setProgress(0);
		
		tvComplete = (TextView) view.findViewById(R.id.tv_pedo_complete);
		tvCompleteGre = (TextView) view.findViewById(R.id.tv_pedo_complete_gre);
		tvPedoMax = (TextView) view.findViewById(R.id.tv_pedo_right);
		tvCal = (TextView) view.findViewById(R.id.tv_pedo_cal);
		tvDistance = (TextView) view.findViewById(R.id.tv_pedo_distance);
		tvTime = (TextView) view.findViewById(R.id.tv_pedo_time);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
	@Override
	public void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();
		
		getActivity().registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());
		completeStep = getActivity().getIntent().getIntExtra("step", 0); 
//		int cal = getActivity().getIntent().getIntExtra("cal", 0); 
//		int distance = getActivity().getIntent().getIntExtra("distance", 0); 
		int hour = getActivity().getIntent().getIntExtra("hour", 0); 
		int minute = getActivity().getIntent().getIntExtra("minute", 0); 
		int second = getActivity().getIntent().getIntExtra("second", 0); 
		setCompleteCal(completeStep, hour, minute, second);
		setCompleteDistance(completeStep);
		setCompleteTime(hour, minute, second);
	}
	
	
	@Override
	public void onResume() {


	
		
		setMaxStepValue();
		setCompleteStepValue();
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(mReceiver);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

	}
	
	private void setMaxStepValue(){
		//最大步数
		max = (int) SharedPreferenceUtil.get(getActivity(), Constant.SHARE_PEDOMETER_TARGET, 500);
		tvPedoMax.setText(max+"");
		stepCircleView.setMax(max);
	}
	
	private void setCompleteStepValue() {
		//完成步数
		tvComplete.setText(completeStep+"");
		Log.e("", getSimpleBlueService().getString(R.string.completion_rate)+ DataUtil.format((float)completeStep*100/(float)(max))+"%");
		
		tvCompleteGre.setText(DataUtil.format((float)completeStep*100/(float)(max))+"%");
		stepCircleView.setProgress(completeStep);
	}
	
	
	private void setCompleteCal(int step,int hour,int min,int second) {
		float cal  = WatchDataUtil.getKcal(step, getHeight(), getWeight(), (hour*60f+min +second/60f));
		tvCal.setText(DataUtil.format(cal/1000f));
	}
	private void setCompleteDistance(int step) {
		float distance = WatchDataUtil.getDistance(step, getHeight());
		Log.e("", "distance" + distance + " getHeight() : "+ getHeight()  );
		tvDistance.setText(DataUtil.format((float)distance/(1000f))+"");
	}
	private void setCompleteTime(int hour,int min,int second) {
		String hourStr = String.valueOf(hour);
		String minStr = String.valueOf(min);
		String secondStr = String.valueOf(second);
		if (hourStr.length()==1) {
			hourStr="0"+hourStr;
		}
		if (minStr.length()==1) {
			minStr="0"+minStr;
		}
		if (secondStr.length()==1) {
			secondStr="0"+secondStr;
		}
		tvTime.setText(hourStr+":"+ minStr+":"+secondStr);
	}
}
