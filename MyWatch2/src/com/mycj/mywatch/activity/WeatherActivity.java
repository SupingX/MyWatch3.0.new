package com.mycj.mywatch.activity;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.CodeDB;
import com.mycj.mywatch.bean.ConditionWeather;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.business.LoadWeatherJsonTask;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.business.LoadWeatherJsonTask.OnProgressChangeListener;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.util.YahooUtil;

public class WeatherActivity extends BaseActivity implements OnClickListener, OnProgressChangeListener {

	private final static int MSG_PLACE = 0x100004;
	private final static int MSG_FORECAST = 0x10005;
	private ImageView imgTodayIcon;
	private Handler mHandler;
	private FrameLayout flHome;
	private String lastJson;
	private RelativeLayout rlSetting;
	private RelativeLayout rlTemp;
	private TextView tvCity;
	private TextView tvWeahter;
	private TextView tvTempValue;
	private TextView tvTempUnit;
	private TextView tvUpdateDate;
	private String city;
	private String unit;
	private String woeid;
	private ProgressDialog pDialog;
	private AbstractSimpleBlueService mSimpleBlueService;
	private final String UNIT_C = "℃";
	private final String UNIT_F = "℉";
	private Runnable runHttp  = new Runnable() {
		@Override
		public void run() {
			// 这里下载数据
			HttpURLConnection conn = null;
			InputStream inputStream = null;
			InputStreamReader isr = null;
			BufferedReader bufferReader = null;
			StringBuffer json = new StringBuffer();

			try {
				URL url = new URL(YahooUtil.getConditionUrl(woeid, unit));
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
					// lenght = inputStream.available();
					isr = new InputStreamReader(inputStream);
					bufferReader = new BufferedReader(isr);
					String inputLine = "";
					while ((inputLine = bufferReader.readLine()) != null) {
						json.append(inputLine);
					}
				}
			} catch (Exception e1) {
			} finally{
				if (isr!=null) {
					try {
						isr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (bufferReader!=null) {
					try {
						bufferReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
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
			Log.i("", "json : " + json);
			mHandler.obtainMessage(1,json.toString()).sendToTarget();
		}
	};
	private ProgressDialog showProgressDialog;
	private Thread threadLoadCondition;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_weather);

//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		initViews();
		setListener();
		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 1:
					Log.v("", "<!-- handler处理  之 MSG_CONDITION ");
					String json6 = (String) msg.obj;
					Log.v("", "<!--json5" + json6);
					showProgressDialog.dismiss();
					try {
						ConditionWeather parseConditionWeatherFromJson = YahooUtil.parseConditionWeatherFromJson(json6);
						String date = parseConditionWeatherFromJson.getDate();
						String temp = parseConditionWeatherFromJson.getTemp();
						String code = parseConditionWeatherFromJson.getCode();
						CodeDB codedB = YahooUtil.getWeatherCode(Integer.valueOf(code), getApplicationContext());
						tvTempValue.setText(temp);
						tvUpdateDate.setText(date);
						tvWeahter.setText(codedB.getText());
						int unitInt = 0;
						int  tempInt = Integer.valueOf(temp);
						Log.e("", "tempInt : " + tempInt);
						if (tempInt < 0) {//零度以下
							if (unit.equals(UNIT_C)) {
								unitInt = 0x40;
							} else {
								unitInt = 0x00;
							}
						} else {//零上
							if (unit.equals(UNIT_C)) {
								unitInt = 0xC0;
							} else {
								unitInt = 0x80;
							}
						}
						
						if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED) {
							mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForWeather(codedB.getProtol(), unitInt, Math.abs(tempInt)));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
					
				case 3:
					break;
				case MSG_FORECAST:
					break;
				case LoadWeatherJsonTask.MSG_CONDITION:
	
					Log.v("", "<!-- handler处理  之 MSG_CONDITION ");
					String json5 = msg.getData().getString("json");
					Log.v("", "<!--json5" + json5);
					
					try {
						ConditionWeather parseConditionWeatherFromJson = YahooUtil.parseConditionWeatherFromJson(json5);
						String date = parseConditionWeatherFromJson.getDate();
						String temp = parseConditionWeatherFromJson.getTemp();
						String code = parseConditionWeatherFromJson.getCode();
						CodeDB codedB = YahooUtil.getWeatherCode(Integer.valueOf(code), getApplicationContext());
						tvTempValue.setText(temp);
						tvUpdateDate.setText(date);
						tvWeahter.setText(codedB.getText());
						int unitInt = 0;
						int  tempInt = Integer.valueOf(temp);
						Log.e("", "tempInt : " + tempInt);
						if (tempInt<0 ){
							if (unit.equals(UNIT_C)) {
								unitInt=0x40;
							}else{
								unitInt=0xC0;
							}
						}else{
							if (unit.equals(UNIT_C)) {
								unitInt=0x00;
							}else{
								unitInt=0x80;
							}
						}
						
						if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED) {
							mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForWeather(codedB.getProtol(), unitInt, Math.abs(tempInt)));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;

				default:
					break;
				}
			}
		};

	}

	@Override
	protected void onResume() {
		update();
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				new LoadWeatherJsonTask(LoadWeatherJsonTask.MSG_CONDITION, WeatherActivity.this, mHandler).execute(YahooUtil.getConditionUrl(woeid, unit));
//			}
//		}).start();
		
		if (!woeid.equals("")&&woeid!=null) {
			showProgressDialog = showProgressDialog(getString(R.string.in_weathering),true);
			if (threadLoadCondition!=null) {
				mHandler.removeCallbacks(threadLoadCondition);
				threadLoadCondition = null;
			}
			threadLoadCondition = new Thread(runHttp);
			threadLoadCondition.start();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(threadLoadCondition);
		if (showProgressDialog!=null&&showProgressDialog.isShowing()) {
			showProgressDialog.dismiss();
		}
		super.onPause();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}

	private void update() {
		city = (String) SharedPreferenceUtil.get(this, Constant.SHARE_PLACE_NAME, "--");
		tvCity.setText("" + city);
		unit = (String) SharedPreferenceUtil.get(this, Constant.SHARE_PLACE_UNIT, "℃");
		tvTempUnit.setText(unit);
		woeid = (String) SharedPreferenceUtil.get(this, Constant.SHARE_PLACE_WOEID, "");

		Log.e("", "city-->" + city);
		Log.e("", "unit-->" + unit);
		Log.e("", "woeid-->" + woeid);

	}

	

		
		
	// /**
	// * 解析地址，并根据地址提交查询天气json请求
	// *
	// * @param json5
	// */
	// protected void parsePlaceJsonAndUpdateView(String json5) {
	// try {
	// List<Place> list = YahooUtil.parseWoeidFromJson(json5);
	// if (list != null) {
	// if (list.size() >= 1) {
	// Place place = list.get(0);
	// tvAddress.setText(place.getName());
	// SharedPreferenceUtil.put(WeatherActivity.this, Constant.SHARE_PLACE,
	// place.getName());// 存贮搜索地址
	// LoadWeatherJsonTask task = new LoadWeatherJsonTask(MSG_FORECAST,
	// WeatherActivity.this, mHandler);
	// task.execute(YahooUtil.getForecastUrl(place.getName()));
	// }else{
	// showShortToast("输入正确的地址");
	// }
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// /**
	// * 解析天气并更新视图
	// *
	// * @param json
	// */
	// private void parseForecastJsonAndUpdateView(String json) {
	// if (json == null) {
	// return;
	// }
	// if (json.trim().equals("")) {
	// return;
	// }
	// try {
	// List<Forecast> forecastsFromJson =
	// YahooUtil.parseForecastsFromJson(json);
	// forecastAdapter = new ForecastAdapter(WeatherActivity.this,
	// forecastsFromJson, R.layout.item_forecast);
	// lvForecast.setAdapter(forecastAdapter);
	// ConditionWeather weather = YahooUtil.parseConditionWeatherFromJson(json);
	// tvCurrentTemperature.setText(weather.getTemp());
	// tvDate.setText(weather.getDate());
	// tvText.setText(""+getTextFromConditonWeather(weather.getText()));
	// imgTodayIcon.setImageResource(YahooUtil.getIcon(weather.getText()));
	// int unit = getUnitFromTemp(weather.getTemp());
	// mLiteBlueService.writeCharacticsUseConnectListener(ProtocolForWrite.instance().getByteForWeather(weahterCode,
	// unit, Integer.valueOf(weather.getTemp())));
	// // 存贮JSON
	// lastJson = json;
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// };
	//
	// private int getUnitFromTemp(String temp) {
	// return 0;
	// }
	//
	// private LiteBlueService mLiteBlueService;
	// private int weahterCode ;
	// private CharSequence weatherText = null ;
	//
	// private CharSequence getTextFromConditonWeather(String text) {
	//
	// if (text.equals(Weaher.SUNNY.getText().toString())) {
	// weahterCode = Weaher.SUNNY.getValue();
	// weatherText = Weaher.SUNNY.getText();
	// }
	//
	// return weatherText;
	// }
	//
	// private void firstEnter() {
	// LoadWeatherJsonTask taskFirst = new LoadWeatherJsonTask(MSG_FORECAST,
	// this, mHandler);
	// taskFirst.setOnProgressChangeListener(this);
	// String city = (String) SharedPreferenceUtil.get(this,
	// Constant.SHARE_PLACE, "beijing");
	// if (!city.equals("")) {
	// taskFirst.execute(YahooUtil.getForecastUrl(city));
	// tvAddress.setText(city);
	// } else {
	//
	// }
	//
	// }
	//
	// /**
	// * 获取当前网络类型
	// *
	// * @param context
	// * @return
	// */
	// private String NetType(Context context) {
	// try {
	// ConnectivityManager cm = (ConnectivityManager)
	// context.getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo info = cm.getActiveNetworkInfo();
	// String typeName = info.getTypeName().toLowerCase(); // WIFI/MOBILE
	// if (typeName.equalsIgnoreCase("wifi")) {
	//
	// } else {
	// typeName = info.getExtraInfo().toLowerCase();
	// // 3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap
	// }
	// return typeName;
	// } catch (Exception e) {
	// return null;
	// }
	// }
	//
	// /**
	// * 隐藏软件盘
	// */
	// private void hideInput(View v) {
	// InputMethodManager imm = (InputMethodManager)
	// getSystemService(INPUT_METHOD_SERVICE);
	// imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	// imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	//
	// }
	//
	// /**
	// * 显示
	// *
	// * @param v
	// */
	// private void showInput(View v) {
	// InputMethodManager imm = (InputMethodManager)
	// this.getSystemService(Context.INPUT_METHOD_SERVICE);
	// imm.showSoftInput(v, 0);
	// }
	//
	// /**
	// * 查询按钮
	// *
	// * @param v
	// */
	// public void select(View v) {
	// try {
	// v.requestFocusFromTouch();
	// String net = NetType(this);
	// Log.e("", "当前网络类型net : " + net);
	//
	// // loadCityWoeid(city);
	// // String url =
	// //
	// "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22shenzhen%2Cch%2C%20%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
	// // String url = YahooUtil.getForecastUrl("Hangzhou");
	// // String url =
	// //
	// "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22Hangzhou%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
	// // new LoadJsonTask(MSG_FORECAST).execute(url);
	// hideInput(v);
	// String city = edAddress.getText().toString();
	// if (city != null && !city.trim().equals("")) {
	// // new
	// // LoadJsonTask(MSG_PLACE).execute(YahooUtil.getPlaceUrl(city.trim()));
	// LoadWeatherJsonTask task = new LoadWeatherJsonTask(MSG_PLACE, this,
	// mHandler);
	// task.setOnProgressChangeListener(this);
	// task.execute(YahooUtil.getPlaceUrl(URLEncoder.encode(city.trim(),
	// "utf-8")));
	// }
	// } catch (UnsupportedEncodingException e) {
	// e.printStackTrace();
	// }
	//
	// // 获取当天天气图片
	// // ImageLoader.load();
	//
	// // 1。从本地加载inputStream
	// // try {
	// // InputStream in = getAssets().open("forecast.xml");
	// // showForecast(in);
	// // } catch (IOException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	//
	// // 2。网络上加载inputStream 用 Handler + Thread
	// // showProgressDialog();
	// // loadWeather(urlString);
	//
	// // 3。异步加载inputStream
	// // new LoadTask().execute(urlString);
	//
	// // 4.异步加载 返回json
	// // new LoadJsonTask(4).execute(urlString);
	// }
	//
	@Override
	public void initViews() {
		rlSetting = (RelativeLayout) findViewById(R.id.rl_weather_setting);
		rlTemp = (RelativeLayout) findViewById(R.id.rl_weather_temp);
		tvCity = (TextView) findViewById(R.id.tv_city);
		tvWeahter = (TextView) findViewById(R.id.tv_weather_value);
		tvTempValue = (TextView) findViewById(R.id.tv_temp_value);
		tvTempUnit = (TextView) findViewById(R.id.tv_temp_unit);
		tvUpdateDate = (TextView) findViewById(R.id.tv_update_date);
		flHome = (FrameLayout) findViewById(R.id.fl_home);

	}

	@Override
	public void setListener() {
		flHome.setOnClickListener(this);
		rlSetting.setOnClickListener(this);
		rlTemp.setOnClickListener(this);
		// edAddress.setOnFocusChangeListener(new OnFocusChangeListener() {
		// @Override
		// public void onFocusChange(View v, final boolean hasFocus) {
		// // (new Handler()).postDelayed(new Runnable() {
		// // public void run() {
		// // InputMethodManager imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// // if (hasFocus) {
		// // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		// // } else {
		// // imm.hideSoftInputFromWindow(edAddress.getWindowToken(), 0);
		// // }
		// // }
		// // }, 500);
		//
		// //如果获得焦点，则弹出键盘
		// if (hasFocus) {
		// getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		// }
		// }
		// });
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_home:
			finish();
			break;
		case R.id.rl_weather_setting:
			Intent intentCity = new Intent(this, WeatherCitySetActivity.class);
			startActivity(intentCity);
			break;
		case R.id.rl_weather_temp:
			Intent intentTempUnit = new Intent(this, WeatherTempUnitSetActivity.class);
			startActivity(intentTempUnit);
			break;

		default:
			break;
		}
	}

	//
	// /**
	// * 加载天气前的准备
	// */
	// private void showProgressDialogs(String msg) {
	// pDialog = new ProgressDialog(this);
	// // pDialog.setCancelable(false);
	// pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	// pDialog.setMessage(msg);
	// pDialog.show();
	// }
	//
	// private void showProgressDialog(int id) {
	// switch (id) {
	// case MSG_FORECAST:
	// showProgressDialogs(" 正在加载天气信息...");
	// break;
	// case MSG_PLACE:
	// showProgressDialogs(" 正在确认地址...");
	// break;
	//
	// default:
	// break;
	// }
	// }
	//
	// /*
	// * private class LoadTask extends AsyncTask<String, Integer, InputStream>
	// {
	// *
	// * public LoadTask(String urlString) { super(); this.urlString =
	// urlString;
	// * }
	// *
	// *
	// * @Override protected InputStream doInBackground(String... params) {
	// *
	// * File pictureFile = MyFileUtil.getOutputMediaFile(1); Log.d("", "save:"
	// +
	// * pictureFile); if (pictureFile == null) { return null; } // 这里下载数据
	// * OutputStream outputStream = null; HttpURLConnection conn = null;
	// * InputStream inputStream = null; try { URL url = new URL(params[0]);
	// conn
	// * = (HttpURLConnection) url.openConnection();
	// * conn.setRequestMethod("POST"); // conn.setConnectTimeout(10000);//
	// * 设置请求时间为10ms // conn.setReadTimeout(5000);// 设置读取时间为5ms
	// * conn.setDoInput(true); // conn.setUseCaches(false); // 不使用缓冲
	// * conn.connect(); int responseCode = conn.getResponseCode();//
	// * 获取网络访问地址的回传码，如果为200则表示成功 if (responseCode == 200) { inputStream =
	// * conn.getInputStream();// 获得网络地址返回的二进制流数据 } outputStream = new
	// * FileOutputStream(pictureFile); // lenght = inputStream.available();
	// *
	// * byte[] buff = new byte[512]; int rc = 0; while ((rc =
	// inputStream.read())
	// * > 0) { outputStream.write(buff, 0, rc); // this.publishProgress(rc);//
	// * 提醒设置进度 } outputStream.flush(); } catch (MalformedURLException e1) {
	// * e1.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
	// * finally { // if (conn != null) { // conn.disconnect(); // } try { // if
	// * (inputStream != null) { // inputStream.close(); // } if (outputStream
	// !=
	// * null) { outputStream.close(); } } catch (Exception e) {
	// * e.printStackTrace(); } } try { Log.e("",
	// * "------------>  inputStream.available() : " + inputStream.available());
	// }
	// * catch (IOException e) { // TODO Auto-generated catch block
	// * e.printStackTrace(); } return inputStream; }
	// *
	// * @Override protected void onPostExecute(InputStream result) { // Message
	// * msg = mHandler.obtainMessage(); // msg.what = 1; // msg.obj = result;
	// //
	// * mHandler.sendMessage(msg); try { Log.e("",
	// * "------异步下载结束------>  InputStream : " + result); Log.e("",
	// * "------异步下载结束------>  InputStream : " + result.available()); } catch
	// * (IOException e) { // TODO Auto-generated catch block
	// e.printStackTrace();
	// * } if (result != null) { mHandler.sendEmptyMessage(3); }
	// *
	// * // if (isOver) { // showForecast(result); // }
	// * super.onPostExecute(result); }
	// *
	// * @Override protected void onPreExecute() {
	// *
	// * // showProgressDialog(); super.onPreExecute(); }
	// */
	//
	// // @Override
	// // protected void onProgressUpdate(Integer... values) {
	// // super.onProgressUpdate(values);
	// // // pDialog.setProgress(100*values[0]/lenght);//设置进度
	// // pDialog.setProgress(values[0]);// 设置进度
	// // }
	// // }
	//
	// /**
	// *
	// * 从yahoo加载json数据
	// *
	// * @author Administrator
	// *
	// */
	// /*
	// * private class LoadJsonTask extends AsyncTask<String, Integer, String> {
	// * // 用于区分每次加载的地址 private int id;
	// *
	// * public LoadJsonTask(int id) { this.id = id; }
	// *
	// *
	// * public LoadTask(String urlString) { super(); this.urlString =
	// urlString;
	// * }
	// *
	// * @Override protected String doInBackground(String... params) { // 这里下载数据
	// * HttpURLConnection conn = null; InputStream inputStream = null;
	// * StringBuffer json = new StringBuffer(); try { URL url = new
	// * URL(params[0]); conn = (HttpURLConnection) url.openConnection();
	// * conn.setRequestMethod("POST"); // conn.setConnectTimeout(10000);//
	// * 设置请求时间为10ms // conn.setReadTimeout(5000);// 设置读取时间为5ms
	// * conn.setDoInput(true); // conn.setUseCaches(false); // 不使用缓冲
	// * conn.connect(); int responseCode = conn.getResponseCode();//
	// * 获取网络访问地址的回传码，如果为200则表示成功 if (responseCode == 200) { inputStream =
	// * conn.getInputStream();// 获得网络地址返回的二进制流数据 } // lenght =
	// * inputStream.available(); InputStreamReader isr = new
	// * InputStreamReader(inputStream); BufferedReader bufferReader = new
	// * BufferedReader(isr); String inputLine = ""; while ((inputLine =
	// * bufferReader.readLine()) != null) { json.append(inputLine); } } catch
	// * (MalformedURLException e1) { e1.printStackTrace(); } catch (IOException
	// * e) { e.printStackTrace(); } finally {
	// *
	// * try { if (inputStream != null) { inputStream.close(); } } catch
	// * (Exception e) { e.printStackTrace(); } if (conn != null) {
	// * conn.disconnect(); } } Log.e("", "加载好的json------------>  json : " +
	// * json.toString()); return json.toString(); }
	// *
	// * @Override protected void onPostExecute(String result) { Log.e("",
	// * "------异步下载结束------>  InputStream : " + result); pDialog.dismiss(); if
	// * (result != null) {
	// *
	// * switch (id) { case MSG_FORECAST: Log.v("",
	// * "< ------ 获取forecastjson完毕 ------ > "); Message msg4 =
	// * mHandler.obtainMessage(); Bundle b4 = new Bundle();
	// b4.putString("json",
	// * result); msg4.setData(b4); msg4.what = MSG_FORECAST;
	// * mHandler.sendMessage(msg4); break; case MSG_PLACE: Log.v("",
	// * "< ------ 获取城市json完毕 ------ > "); Message msg5 =
	// * mHandler.obtainMessage(); Bundle b5 = new Bundle();
	// b5.putString("json",
	// * result); msg5.setData(b5); msg5.what = MSG_PLACE;
	// * mHandler.sendMessage(msg5); break;
	// *
	// * default: break; } } super.onPostExecute(result); }
	// *
	// * @Override protected void onPreExecute() { showProgressDialog(id);
	// * super.onPreExecute(); } }
	// */
	//
	// private class ForecastAdapter extends CommonAdapter<Forecast> {
	// public ForecastAdapter(Context context, List<Forecast> mDatas, int
	// layoutId) {
	// super(context, mDatas, layoutId);
	// }
	//
	// @Override
	// public void convert(com.mycj.mywatch.util.CommonAdapter.ViewHolder
	// holder, Forecast item) {
	// ((TextView) holder.getView(R.id.tv_forecast_day)).setText(item.getDay());
	// ((TextView)
	// holder.getView(R.id.tv_forecast_high)).setText(item.getHigh());
	// ((TextView) holder.getView(R.id.tv_forecast_low)).setText(item.getLow());
	// ((TextView)
	// holder.getView(R.id.tv_forecast_text)).setText(item.getText());
	// ((ImageView)
	// holder.getView(R.id.img_forecast_icon)).setImageResource(YahooUtil.getIcon(item.getText()));
	//
	// }
	//
	// }
	//
	// @Override
	// public void onPreExecute(int id) {
	// showProgressDialog(id);
	// }
	//
	// @Override
	// public void onPostExecute(int id) {
	// if (pDialog != null) {
	// pDialog.dismiss();
	// }
	// }
	//
	// @Override
	// public void onError(int id) {
	// showShortToast("加载异常，请检查网络设置");
	// Log.v("", "加载异常，请检查网络设置");
	// loadLastWeather();
	// }
	//
	// /**
	// * 记载上次信息
	// */
	// private void loadLastWeather() {
	// String lastJson = (String) SharedPreferenceUtil.get(this,
	// Constant.SHARE_JSON_FORECAST, "");
	// parseForecastJsonAndUpdateView(lastJson);
	// }
	//
	// private void beforeFinish() {
	// SharedPreferenceUtil.put(WeatherActivity.this,
	// Constant.SHARE_JSON_FORECAST, lastJson);// 存贮搜索地址
	// }

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
		if (pDialog != null && pDialog.isShowing()) {
			pDialog.dismiss();
			finish();

		}
	}

	@Override
	public void onError(int id) {
		showShortToast("加载异常，请检查网络设置");
	}
}
