package com.mycj.mywatch.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.adapter.CityAdapter;
import com.mycj.mywatch.bean.ConditionWeather;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.bean.Place;
import com.mycj.mywatch.business.LoadWeatherJsonTask;
import com.mycj.mywatch.business.LoadXmlTask;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.util.YahooUtil;
import com.mycj.mywatch.view.CleanEditText;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeatherCitySetActivity extends BaseActivity implements OnClickListener,LoadXmlTask.OnProgressChangeListener{

	private CleanEditText etCity;
	private RelativeLayout rlSetting;
	private TextView tvSearch;
	private ListView lvCity;
	private List<Place> places = new ArrayList<>();
	private CityAdapter mAdapter;
	private Handler mHandler = new Handler(){

		private ConditionWeather parseConditionWeatherFromJson;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				showProgressDialog.dismiss();
				byte[] data1  = (byte[]) msg.obj;
				Log.e("WeatherCitySetActivity", "______________________is : " + new String(data1));
				try {
					places.clear();
					places = YahooUtil.getPlaces(data1);
					Log.e("WeatherCitySetActivity", "______________________places : " + places);
					mAdapter = new CityAdapter(WeatherCitySetActivity.this, places, R.layout.item_city);
					lvCity.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();
					tvCount.setText(""+"AVAILABLE CITY : "+places.size());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case LoadXmlTask.MSG_PLACE:
				byte[] data  = (byte[]) msg.obj;
				Log.e("WeatherCitySetActivity", "______________________is : " + new String(data));
					
				try {
					places.clear();
					places = YahooUtil.getPlaces(data);
					Log.e("WeatherCitySetActivity", "______________________places : " + places);
					mAdapter = new CityAdapter(WeatherCitySetActivity.this, places, R.layout.item_city);
					lvCity.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();
					tvCount.setText(""+"AVAILABLE CITY : "+places.size());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case LoadWeatherJsonTask.MSG_CONDITION:
				String json = msg.getData().getString("json");
				Log.e("WeatherCitySetActivity", "______________________json : " +json);
				
				try {
					parseConditionWeatherFromJson = YahooUtil.parseConditionWeatherFromJson(json);
					
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
			
			super.handleMessage(msg);
		}
		
		
	};
	private ProgressDialog pDialog;
	private TextView tvCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_city_set);
//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		initViews();
		setListener();
	}
	
	
	@Override
	protected void onDestroy() {
		if (showProgressDialog!=null&&showProgressDialog.isShowing()) {
			showProgressDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void initViews() {
		etCity = (CleanEditText) findViewById(R.id.ed_city);
		rlSetting = (RelativeLayout) findViewById(R.id.rl_setting);
		tvSearch = (TextView) findViewById(R.id.tv_search_city);
		tvCount = (TextView) findViewById(R.id.tv_city_count);
		lvCity = (ListView) findViewById(R.id.lv_city);
	
	}

	@Override
	public void setListener() {
		rlSetting.setOnClickListener(this);
		tvSearch.setOnClickListener(this);
		lvCity.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Place place = places.get(pos);
				Log.e("", "place.getWoeid()" + place.getWoeid());
				Log.e("", "place.getName()" + place.getName());
				SharedPreferenceUtil.put(WeatherCitySetActivity.this, Constant.SHARE_PLACE_WOEID, place.getWoeid());
				SharedPreferenceUtil.put(WeatherCitySetActivity.this, Constant.SHARE_PLACE_NAME, place.getName());
				finish();
//				new LoadWeatherJsonTask(LoadWeatherJsonTask.MSG_FORECAST, WeatherCitySetActivity.this, mHandler).execute(YahooUtil.getConditionUrl(woeid, "c"));
			}
		});
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.rl_setting:
			finish();
			
			break;
		case R.id.tv_search_city:
			Log.e("", "搜索");
			String city = etCity.getText().toString();

			try {
				placeNameUrlNew = YahooUtil.getPlaceNameUrlNew(URLEncoder.encode(city.trim(), "utf-8"));//转码很重要，浏览器不能识别中文，查找会没有结果
				Log.i("", "placeNameUrlNew : " + placeNameUrlNew);
				
				
//				new LoadXmlTask(LoadXmlTask.MSG_PLACE, this, mHandler).execute(placeNameUrlNew);
				
				showProgressDialog = showProgressDialog(getString(R.string.in_citying),true);
				if (thread!=null) {
					mHandler.removeCallbacks(thread);
				}
				thread = new Thread(runHttp);
				thread.start();
				
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}
	
	private Runnable runHttp  = new Runnable() {
		@Override
		public void run() {
			// 这里下载数据
			HttpURLConnection conn = null;
			InputStream inputStream = null;
			ByteArrayOutputStream out = null ;

			try {
				URL url = new URL(placeNameUrlNew);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				// conn.setConnectTimeout(10000);// 设置请求时间为10ms
				// conn.setReadTimeout(5000);// 设置读取时间为5ms
				conn.setRequestProperty("Content-type", "text/html");
				conn.setRequestProperty("Accept-Charset", "utf-8");
				conn.setRequestProperty("contentType", "utf-8");
				conn.setDoInput(true);
				conn.setUseCaches(true); // 不使用缓冲
				conn.connect();
				int responseCode = conn.getResponseCode();// 获取网络访问地址的回传码，如果为200则表示成功
				if (responseCode == 200) {
					inputStream = conn.getInputStream();// 获得网络地址返回的二进制流数据
				}
				out= new ByteArrayOutputStream();
				int len = 0;
				byte[] buffer = new byte[200];
				while ((len = inputStream.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				data = out.toByteArray();
			} catch (Exception e1) {
				if (conn!=null) {
					conn.disconnect();
					conn = null;
				}
				if (inputStream!=null) {
					try {
						inputStream.close();
						inputStream=null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Log.v("", "异常 e1");
				e1.printStackTrace();
				return;
			}finally{
				if (conn!=null) {
					conn.disconnect();
					conn = null;
				}
				if (inputStream!=null) {
					try {
						inputStream.close();
						inputStream=null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} 
			
			Log.i("", "out : " + out);
			mHandler.obtainMessage(1,out.toByteArray()).sendToTarget();
		}
		
	};
	private String placeNameUrlNew;
	private byte[] data;
	private ProgressDialog showProgressDialog;
	private Thread thread;
	
	
	/**
	 * 加载前的准备
	 */
	private void showProgressDialogs(String msg) {
		pDialog = new ProgressDialog(this);
		// pDialog.setCancelable(false);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(msg);
		pDialog.show();
	}
	
	@Override
	public void onPreExecute(int id) {
		showProgressDialogs("查找中");
	}

	@Override
	public void onPostExecute(int id) {
		if (pDialog!=null&&pDialog.isShowing()) {
			pDialog.dismiss();
			finish();
			
		}
	}

	@Override
	public void onError(int id) {
		showShortToast("加载异常，请检查网络设置");
	}
}
