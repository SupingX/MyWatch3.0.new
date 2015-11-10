package com.mycj.mywatch.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.litepal.crud.DataSupport;

import android.R.integer;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mycj.mywatch.BaseFragment;
import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.HeartRateData;
import com.mycj.mywatch.bean.SleepData;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.DateUtil;
import com.mycj.mywatch.view.HistoryView;
import com.mycj.mywatch.view.SleepCountView;
import com.mycj.mywatch.view.SleepCountView.OnScrollListener;
import com.mycj.mywatch.view.SleepCountView.OnSleepDataChangeListener;

public class HeartRateHistoryFragment extends BaseFragment implements OnClickListener{
	private final String SDF = "yyyy-MM";
	private Handler mhandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
	
				break;

			default:
				break;
			}
		};
	};
//	private AbstractSimpleBlueService mSimpleBlueService;
	private TextView tvDate;
	private TextView tvPreious;
	private TextView tvNext;
	private HistoryView hrHistoryView;
	private Date currentDate;
	private ProgressDialog showProgressDialog;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_heart_rate_history, container,false);
		initViews(view);
		setListener();
		loadData();
		return view;
	}
	@Override
	public void onStart() {
		super.onStart();
//		mSimpleBlueService = getSimpleBlueService();
	}
	
	
	public void delete(){
		DataSupport.deleteAll(HeartRateData.class);
		
		
	}
	@Override
	public void onResume() {
		
		
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
		//1.动画变化
		startAnimation(tvPreious);
			currentDate = getDateForDiff(-1);
		tvDate.setText(DateUtil.dateToString(currentDate, SDF));
	
		break;
	case R.id.tv_next:
		//1.动画变化
		startAnimation(tvPreious);
		//2。日期变化 -1
		 currentDate = getDateForDiff(1);
		tvDate.setText(DateUtil.dateToString(currentDate, SDF));
	
		break;
		default:
			break;
		}
		
		//3。查询数据库，根据日期
		loadData();
	}
	
	
	private void loadData(){
		showProgressDialog = showProgressDialog(getResources().getString(R.string.loading));
		mhandler.post(new Runnable() {
			@Override
			public void run() {
				List<Float> listNext = getData(currentDate);
				hrHistoryView.setData(listNext);
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
	
	
	private List<Float> getData(Date date) {
		List<Float> data = new ArrayList<Float>();
		//获取本月天数
		Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	Log.v("", "——————————————————————月份 ："+c.get(Calendar.MONTH));
    	int monthMaxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
    	Log.e("", "本月天数 monthMaxDay : " + monthMaxDay );
    	//遍历查找每天的数据
		for (int i = 0; i < monthMaxDay; i++) {
			c.set(Calendar.DAY_OF_MONTH, i+1);
			HeartRateData hrData = findHeartRateDateByDate(c.getTime());
			//有数据就添加
			if (hrData!=null) {
				data.add((float) hrData.getAvghr());
			}else{
				//没有数据就为0
				data.add(0f);
			}
		}
		if (showProgressDialog != null&&showProgressDialog.isShowing()) {
			showProgressDialog.dismiss();
		}
		//从数据库中获取一个月所有的天数的数据
//		List<SleepData> list = findAllSleepDateByMonth(date);
		return data;
	}
	
	/**
	 *	根据日期查找HeartRateData
	 * @param date
	 * @return
	 */
	private HeartRateData findHeartRateDateByDate(Date date){
		String sql = DateUtil.dateToString(date, "yyyyMMdd");
		List<HeartRateData>  hrData = DataSupport.where("year=? and month=? and day=?", sql.substring(0,4),sql.substring(4,6),sql.substring(6,8)).find(HeartRateData.class);
		if (hrData!=null && hrData.size()>0) {
			return hrData.get(0);
		}else {
			return null;
		}
	}
	
	
	private Date getDateForDiff(int diff){
		String dateStr = tvDate.getText().toString();
		Date date = DateUtil.stringToDate(dateStr, SDF);
		Date diffDate = DateUtil.getDateOfDiffMonth(date, diff);
		return diffDate;
	}
	

	private void initViews(View view) {
		tvDate = (TextView) view.findViewById(R.id.tv_sleep_history_date);
		tvPreious = (TextView) view.findViewById(R.id.tv_preious);
		tvNext = (TextView) view.findViewById(R.id.tv_next);
		hrHistoryView = (HistoryView) view.findViewById(R.id.history_hr);
		hrHistoryView.setTextY(new String[]{"40","80","120","160","200","240"});
		hrHistoryView.setMax(240);
		currentDate = new Date();
		tvDate.setText(DateUtil.dateToString(currentDate, SDF));//默认本月
	}
	

	private void setListener(){
		tvPreious.setOnClickListener(this);
		tvNext.setOnClickListener(this);
	}

	
	
}
