package com.mycj.mywatch.fragment;

import java.util.Date;
import java.util.List;

import org.litepal.crud.DataSupport;

import android.bluetooth.BluetoothProfile;
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
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.bean.SleepData;
import com.mycj.mywatch.business.ParseSleepData;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.DataUtil;
import com.mycj.mywatch.util.DateUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.view.SleepCountView;
import com.mycj.mywatch.view.SleepCountView.OnScrollListener;
import com.mycj.mywatch.view.SleepCountView.OnSleepDataChangeListener;

public class SleepFragment extends BaseFragment implements OnClickListener{
	private final String SDF = "yyyy-MM-dd";
	private SleepCountView sleepCountView;
	private TextView tvDate;
	private TextView tvTotal;
	private TextView tvAwak;
	private TextView tvDeep;
	private TextView tvLight;
	private AbstractSimpleBlueService mSimpleBlueService;
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
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(SimpleBlueService.ACTION_DATA_HISTORY_SLEEP_FOR_TODAY)){
				final String sleeps = intent.getExtras().getString(SimpleBlueService.EXTRA_SLEEP);
				mhandler.post(new Runnable() {
					@Override
					public void run() {
						Log.e("SleepFragment", "当天的睡眠数据sleeps :" + sleeps);
						String[] split = sleeps.split(",");
						Log.i("", "split :" + split);
						int [] datas = new int[24];
						for (int i = 0; i < split.length; i++) {
							datas[i] = Integer.valueOf(split[i]);
						}
						sleepCountView.setSleepData(datas);
						float[] parseSleepData = ParseSleepData.parseSleepData(datas);
						float deep = parseSleepData[2];
						float light = parseSleepData[1];
						float awak = parseSleepData[0];
						tvTotal.setText((deep+light)+"");
						tvDeep.setText(deep+"");
						tvLight.setText(light+"");
//						tvAwak.setText(sleepCountView.getAwak()+"");
						awak = size - deep-light;
						if (awak<0) {
							awak=0;
						}
						tvAwak.setText((awak)+"");
						
					}
				});
			}
		}
		
	};
	private int size;

	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sleep_sleep, container,false);
		initViews(view);
		setListener();
//		sleepCountView.setSleepData(new int[]{1,2,3,4,2,2,2,2,1,5,5,5,4,4,2,5,5,2,4,4,1,2,2,5});
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();
		getActivity().registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());
		byte[] data = ProtocolForWrite.instance().getByteForSleepQualityOfToday(0);
		Log.e("", "data : " + data+"_____________________________");
		if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED) {
			mSimpleBlueService.writeCharacteristic(data);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateTextDay(new Date());
		int start = (int) SharedPreferenceUtil.get(getActivity(), Constant.SHARE_SLEEP_START_HOUR, 0);
		int end = (int) SharedPreferenceUtil.get(getActivity(), Constant.SHARE_SLEEP_END_HOUR, 23);
		if (start>end) {
			size = (24-start)+end;
		}else{
			size = end-start;
		}
		tvAwak.setText(DataUtil.format1(size));
		
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		
		default:
			break;
		}
	}
	
	private SleepData getSleepDateFromSql(String dateStr) {
		List<SleepData> sleepDatas = DataSupport.where("date=?",dateStr).find(SleepData.class);
		if (sleepDatas!=null && sleepDatas.size()>0	) {
			return sleepDatas.get(0);
		}
		return null;
	}

	private String getDateForDiff(int diff){
		
		String dateStr = tvDate.getText().toString();
		Date date = DateUtil.stringToDate(dateStr, SDF);
		Date diffDate = DateUtil.getDateOfDiffDay(date, diff);
		return DateUtil.dateToString(diffDate, SDF);
	}
	

	private void initViews(View view) {
		tvDate = (TextView) view.findViewById(R.id.tv_sleep_date);
		Log.e("","tvDate ------------"	 +tvDate);
		tvDate.setText(DateUtil.dateToString(new Date(), SDF));
		
		tvTotal = (TextView) view.findViewById(R.id.tv_sleep_total);
		tvAwak = (TextView) view.findViewById(R.id.tv_sleep_awak);
		tvDeep = (TextView) view.findViewById(R.id.tv_sleep_deep);
		tvLight = (TextView) view.findViewById(R.id.tv_sleep_light);
		
		sleepCountView = (SleepCountView) view.findViewById(R.id.sleep_count);
//		tvTotal.setText(""+sleepCountView.getsize());
	
//		tvAwak.setText(""+size);
		
	}

	private void setListener(){
		sleepCountView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void previous() {
			}
			@Override
			public void next() {
			}
		});
//		sleepCountView.setOnSleepDataChangeListener(new OnSleepDataChangeListener() {
//			@Override
//			public void onchange(int[]sleeps) {
//				tvAwak.setText((size-sleepCountView.getTotal())+"");
////				tvAwak.setText(sleepCountView.getAwak()+"");
//				tvDeep.setText(sleepCountView.getDeep()+"");
//				tvLight.setText(sleepCountView.getLight()+"");
//				tvTotal.setText(sleepCountView.getTotal()+"");
//				
//			}
//		});
	}

	private void updateTextDay(Date date){
		String dateStr = DateUtil.dateToString(date, "yyyy-MM-dd");
		boolean isSameDay = DateUtil.isSameDayOfMillis(new Date().getTime(), date.getTime());
		if (isSameDay) {
			tvDate.setText(getString(R.string.today)+dateStr);
		}else{
			tvDate.setText(dateStr);
		}
	}
	
	
}
