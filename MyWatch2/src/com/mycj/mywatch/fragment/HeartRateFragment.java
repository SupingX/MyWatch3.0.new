package com.mycj.mywatch.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.litepal.crud.DataSupport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mycj.mywatch.BaseFragment;
import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.HeartRateData;
import com.mycj.mywatch.business.HeartRateJson;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.DataUtil;
import com.mycj.mywatch.view.HeartRateView;
import com.mycj.mywatch.view.SleepCountView;

public class HeartRateFragment extends BaseFragment implements OnClickListener {
	private final String SDF = "yyyy-MM-dd";
	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
		};
	};
	private Runnable r = new Runnable() {

		@Override
		public void run() {

		}
	};
	private AbstractSimpleBlueService mSimpleBlueService;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SimpleBlueService.ACTION_DATA_HEART_RATE)) {
			final int hr = intent.getIntExtra(SimpleBlueService.EXTRA_HEART_RATE, 0);
//			final int avgHr = intent.getIntExtra(SimpleBlueService.EXTRA_HEART_RATE_AVG, 0);
//			final int maxHr = intent.getIntExtra(SimpleBlueService.EXTRA_HEART_RATE_MAX, 0);
//			final int minHr = intent.getIntExtra(SimpleBlueService.EXTRA_HEART_RATE_MIN, 0);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateUi(hr);
					}
				});
			}
		}

	};

	private TextView tvHeartRate;
	private TextView tvMax;
	private TextView tvMin;
	private TextView tvAvg;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_heart_rate, container, false);
		initViews(view);
		setListener();
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
//		mSimpleBlueService = getSimpleBlueService();
		getActivity().registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());
	}

	@Override
	public void onResume() {
		updateTextDay(new Date());
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(mReceiver);
	}

	@Override
	public void onDestroy() {
		// mhandler.removeCallbacks(run);
		listHr.clear();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		default:
			break;
		}
	}

	private List<Integer> listHr = new ArrayList<Integer>();


	private HeartRateView hrView;

	private void initViews(View view) {
		tvHeartRate = (TextView) view.findViewById(R.id.tv_heart_rate);
		tvMax = (TextView) view.findViewById(R.id.tv_hr_max);
		tvMin = (TextView) view.findViewById(R.id.tv_hr_min);
		tvAvg = (TextView) view.findViewById(R.id.tv_hr_avg);
		hrView = (HeartRateView) view.findViewById(R.id.heart_rate);
		loadJson(hrView);
	}

	private void setListener() {
	}

	private void updateTextDay(Date date) {
	}

	/**
	 * 根据手环传来的心率收据
	 * 
	 * @param f
	 */
	private void updateUi(int hr) {
		if (hr == 0) {
			// 传来的是0时，结束。
			Log.e("", "心率测试结束");
			// 清空，准备下一次保存记录
			listHr.clear();
			return;
		}
		hrView.addData((float) hr);
		listHr.add(hr);
		tvHeartRate.setText(hr + "");
		int[] data = getMaxFromList(listHr);
		tvMax.setText(data[0] + "");
		tvMin.setText(data[1] + "");
		tvAvg.setText(data[2]+"");
	}
	
	private int[] getMaxFromList(List<Integer> hrList){
		int maxs=0;
		int mins= 0;
		int total = 0;
		int size = hrList.size();
		if (size > 0) {
			maxs = hrList.get(0);
			mins = hrList.get(0);
			for (int i = 0; i < size; i++) {
				int hr = hrList.get(i);
				maxs = Math.max(maxs, hr);
				mins = Math.min(mins, hr);
				total+=hr;
			}
		}
		int avgs = (int) (total*1.0 /size);
		return new int[]{maxs,mins,avgs};
	}
	
	

	/**
	 * 保存心率
	 */
	private void saveHeartRate() {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.clear();
		c.setTime(date);
		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH) + 1);
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String min = String.valueOf(c.get(Calendar.MINUTE));
		String datas = dataToString();
		
		List<HeartRateData> heartRateDatas = DataSupport.where("year=? and month=? and day=? and hour =? and min=?", year, month, day, hour, min).find(HeartRateData.class);
		if (heartRateDatas != null && heartRateDatas.size() > 0) {
			Log.v("", "存在历史数据");
			// 说明数据库存在
			HeartRateData heartRateDataQuery = heartRateDatas.get(0);
			if (datas!=null && !datas.equals("")) {
				heartRateDataQuery.setHrDatas(dataToString());
				heartRateDataQuery.save();
				Log.v("", "更新心率成功	:" +heartRateDataQuery.toString());
			}
		} else {
			// 说明数据库不存在
			Log.v("", "存在没有数据");
			if (datas!=null && !datas.equals("")) {
				HeartRateData hrd = new HeartRateData(year, month, day, hour, min, datas);
				hrd.save();
				Log.v("", "保存心率成功 ："+hrd.toString() );
				saveHeartRateJson(hrd);
			}
		}

	}

	/**
	 * 保存心率数据json最后一次
	 * @param hrd
	 */
	private void saveHeartRateJson(HeartRateData hrd) {
		String json = HeartRateJson.objToJson(hrd);
		HeartRateJson.writeFileData("json", json, getActivity());
	}
	
	/**
	 * 初始化最后一次测心率的json数据
	 * @param view
	 */
	private void loadJson(HeartRateView view) {
		String json = HeartRateJson.readFileData("json", getActivity());
		HeartRateData jsonToObj = HeartRateJson.jsonToObj(json);
		if (jsonToObj!=null) {	
			Log.e("", "jsonToObj : " + jsonToObj);
			String hrDatas = jsonToObj.getHrDatas();
			String[] split = hrDatas.split(",");
			if (split!=null && split.length>0) {
				for (int i = 0; i < split.length; i++) {
					view.addData(Float.valueOf(split[i]));
				}
			}
			
			tvMax.setText(jsonToObj.getMaxHr()+"");
			tvMin.setText(jsonToObj.getMinHr()+"");
			tvAvg.setText(jsonToObj.getAvghr()+"");
			tvHeartRate.setText(split[split.length-1]);
		}
	}	

	private String dataToString() {
		StringBuffer sb = new StringBuffer();
		if (listHr.size() > 0) {
			for (int i = 0; i < listHr.size(); i++) {
				sb.append(String.valueOf(listHr.get(i)));
				if (i!=listHr.size()-1) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}

}
