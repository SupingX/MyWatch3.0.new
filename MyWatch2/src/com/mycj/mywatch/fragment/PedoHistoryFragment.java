package com.mycj.mywatch.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.litepal.crud.DataSupport;

import android.app.ProgressDialog;
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
import com.mycj.mywatch.bean.PedoData;
import com.mycj.mywatch.util.DataUtil;
import com.mycj.mywatch.util.DateUtil;
import com.mycj.mywatch.util.WatchDataUtil;
import com.mycj.mywatch.view.HistoryView;

public class PedoHistoryFragment extends BaseFragment implements OnClickListener {
	private final String SDF = "yyyy-MM";
	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:

				break;

			default:
				break;
			}
		};
	};
	private Runnable r = new Runnable() {

		@Override
		public void run() {
		}
	};
	private TextView tvDate;
	private TextView tvPreious;
	private TextView tvNext;
	private HistoryView stepHistoryView;
	private TextView tvTotalCal;
	private TextView tvTotalDistance;
	private TextView tvTotalStep;
	
	private float totalDistance;
	private float totalCal;
	private float totalStep;
	
	private Date currentDate;

	private ProgressDialog showProgressDialog;
	private float totalTime;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pedo_history, container, false);
		initViews(view);
		setListener();
		
		showProgressDialog = showProgressDialog(getResources().getString(R.string.loading));
		mhandler.post(new Runnable() {
			@Override
			public void run() {
				List<Float> listNext = getData(	getDateForDiff(0));
				stepHistoryView.setData(listNext);
				updateCountStep();
			}
		});
		mhandler.postDelayed((new Runnable() {
			@Override
			public void run() {
				if (showProgressDialog != null) {
					showProgressDialog.dismiss();
				}
			}
		}),5000);
		
		
		return view;
	}

	@Override
	public void onResume() {
		totalDistance = 0f;
		totalCal =0f;
		totalStep=0f;
		super.onResume();
	}

	@Override
	public void onPause() {
		
		super.onPause();
		if (showProgressDialog != null&&showProgressDialog.isShowing()) {
			showProgressDialog.dismiss();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
	
		
		switch (v.getId()) {
		case R.id.tv_preious:
			totalDistance = 0f;
			totalCal =0f;
			totalStep=0f;
			// 1.动画变化
			startAnimation(tvPreious);
			// 2。日期变化 -1
			currentDate = getDateForDiff(-1);
			tvDate.setText(DateUtil.dateToString(currentDate, SDF));

			// 3。查询数据库，根据日期
			// List<Float> list = getData(currentDate);
			// stepHistoryView.setData(list);
			break;
		case R.id.tv_next:
			totalDistance = 0f;
			totalCal =0f;
			totalStep=0f;
			// 1.动画变化
			startAnimation(tvPreious);
			// 2。日期变化 -1
			currentDate = getDateForDiff(1);
			tvDate.setText(DateUtil.dateToString(currentDate, SDF));
			// 3。查询数据库，根据日期
			break;
		default:
			break;
		}
		showProgressDialog = showProgressDialog(getResources().getString(R.string.loading));
		mhandler.post(new Runnable() {
			@Override
			public void run() {
				List<Float> listNext = getData(currentDate);
				stepHistoryView.setData(listNext);
				updateCountStep();
			}
		});
		mhandler.postDelayed((new Runnable() {
			@Override
			public void run() {
				if (showProgressDialog != null) {
					showProgressDialog.dismiss();
				}
			}
		}),3000);

	}

	private void updateCountStep() {
		// 总步数
		// String totalStep = String.valueOf(totalStep);
		tvTotalStep.setText(String.valueOf((int)totalStep));
		// 总距离
		float distanceTotal = WatchDataUtil.getDistance(totalStep,getHeight());
		tvTotalDistance.setText(DataUtil.format((float)distanceTotal/1000f));
		// 总卡洛里
		float kcalTotal = WatchDataUtil.getKcal(totalStep, getHeight(), getWeight(), totalTime);
		tvTotalCal.setText(DataUtil.format((float)kcalTotal/1000f));

	}

	private List<Float> getData(Date date) {
		Log.e("", "查询的日期 ：" + DateUtil.dateToString(date, "yyyy年MM月dd日"));
		List<Float> data = new ArrayList<Float>();
		// 获取本月天数
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int monthMaxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		// 遍历查找每天的数据
		for (int i = 0; i < monthMaxDay; i++) {
			Log.e("", "______本月第" + i + "天 ");
			c.set(Calendar.DAY_OF_MONTH, i + 1);
			PedoData stepData = findStepDateByDate(c.getTime());
					// 有数据就添加
					if (stepData != null) {
						Log.e("", stepData.toString());
						data.add((float) stepData.getStep());
						totalDistance += stepData.getDistance();
						totalCal += stepData.getCal();
						totalStep += stepData.getStep();
						totalTime = stepData.getHour()*60f+stepData.getMinute()+stepData.getSecond()/60f;
					} else {
						// 没有数据就为0
						data.add(0f);
					}
		}
		if (showProgressDialog != null&&showProgressDialog.isShowing()) {
			showProgressDialog.dismiss();
		}
	
		// 从数据库中获取一个月所有的天数的数据
		// List<SleepData> list = findAllSleepDateByMonth(date);

		return data;
	}

	/**
	 * 根据日期查找stepData
	 * 
	 * @param date
	 * @return
	 */
	private PedoData findStepDateByDate(Date date) {
		String sql = DateUtil.dateToString(date, "yyyyMMdd");
		Log.e("", "______________________________查询的日期 ：sql" + sql);
		List<PedoData> stepdatas = DataSupport.where("year=? and month=? and day=?", sql.substring(0,4),sql.substring(4,6),sql.substring(6,8)).find(PedoData.class);
		if (stepdatas != null && stepdatas.size() > 0) {
			return stepdatas.get(0);
		} else {
			return null;
		}
	}

	private Date getDateForDiff(int diff) {
		String dateStr = tvDate.getText().toString();
		Date date = DateUtil.stringToDate(dateStr, SDF);
		Date diffDate = DateUtil.getDateOfDiffMonth(date, diff);
		return diffDate;
	}

	private void initViews(View view) {
		tvDate = (TextView) view.findViewById(R.id.tv_step_history_date);
		tvPreious = (TextView) view.findViewById(R.id.tv_preious);
		tvNext = (TextView) view.findViewById(R.id.tv_next);

		tvTotalCal = (TextView) view.findViewById(R.id.tv_total_cal);
		tvTotalDistance = (TextView) view.findViewById(R.id.tv_total_distance);
		tvTotalStep = (TextView) view.findViewById(R.id.tv_total_step);

		stepHistoryView = (HistoryView) view.findViewById(R.id.history_step);
		
		stepHistoryView.setTextY(new String[] { "4,000", "8,000", "12,000", "16,000", "20,000", "24,000" });
		stepHistoryView.setMax(24000);
		tvDate.setText(DateUtil.dateToString(new Date(), SDF));// 默认本月

	}

	private void setListener() {
		tvPreious.setOnClickListener(this);
		tvNext.setOnClickListener(this);
	}

}
