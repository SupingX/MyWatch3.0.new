package com.mycj.mywatch.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.util.Xml;

import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.CodeDB;
import com.mycj.mywatch.bean.ConditionWeather;
import com.mycj.mywatch.bean.Forecast;
import com.mycj.mywatch.bean.Place;
import com.mycj.mywatch.bean.Wind;

public class YahooUtil {
	public final static String FORECAST = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22";
	public final static String WIND = "http://query.yahooapis.com/v1/public/yql?q=select%20wind%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20";
	

	public final static String PLACE_ID = "http://query.yahooapis.com/v1/public/yql?q=select%20*from%20geo.places%20where%20text%3D%22";
	public final static String PLACE_END = "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

	public final static String END = "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";;

	public final static String PLACE = "http://sugg.us.search.yahoo.net/gossip-gl-location/?appid=weather&output=xml&command=";
	private static Context context;
	
	
	
	/**
	 * 获取Forecast预报url
	 * 
	 * @param city
	 * @return
	 */
	public static String getForecastUrl(String city) {
		if (city != null && !city.trim().equals("")) {
			Log.v("YahooUtil", "getForecastUrl()");
			return FORECAST + city + END;

		} else {
			return null;
		}
	}

	/**
	 * 获取Wind风 url
	 * 
	 * @param city
	 * @return
	 */
	public static String getWindUrl(String city) {
		if (city != null) {
			return WIND + city + END;
		} else {
			return null;
		}
	}

	public static String getConditionUrl(String woeid,String unit) {
		if (woeid != null ) {
			if (unit.equals("℃")) {
				return "http://query.yahooapis.com/v1/public/yql?q=select%20item.condition%20from%20weather.forecast%20where%20woeid%20%3D%20"+woeid+"%20%20and%20u%3D'c'&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
			}else{
				return "http://query.yahooapis.com/v1/public/yql?q=select%20item.condition%20from%20weather.forecast%20where%20woeid%20%3D%20"+woeid+"%20%20and%20u%3D'f'&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
			}
		} else {
			return null;
		}
	}

	/**
	 * 根据城市名称从yahoo获取城市woeid
	 * 
	 * @param city
	 */
	public static String getPlaceUrl(String city) {
		if (city != null) {
			return PLACE_ID + city + PLACE_END;
		} else {
			return null;
		}
		
	}

	/**
	 * 根据城市名称从yahoo获取城市woeid
	 * 
	 * @param city
	 */
	public static String getPlaceNameUrl(String city) {
		if (city != null) {
			return PLACE_ID + city + PLACE_END;
		} else {
			return null;
		}
	}

	/**
	 * 新
	 * 
	 * @param city
	 * @return
	 */
	public static String getPlaceNameUrlNew(String city) {
		Log.e("YahooUtil", "city :" + city);
		if (city != null) {
			return PLACE + city;
		} else {
			return null;
		}
	}

	/**
	 * 从JSON数据中提取forcast信息
	 * 
	 * @param data
	 * @return
	 * @throws JSONException
	 */
	public static List<Forecast> parseForecastsFromJson(String data) throws JSONException {
		List<Forecast> list = new ArrayList<>();
		JSONObject json = new JSONObject(data).getJSONObject("query").getJSONObject("results").getJSONObject("channel").getJSONObject("item");
		JSONArray jsonArray = json.getJSONArray("forecast");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObj = (JSONObject) jsonArray.get(i);
			String date = jsonObj.getString("date");
			String day = jsonObj.getString("day");
			String high = jsonObj.getString("high");
			String low = jsonObj.getString("low");
			String text = jsonObj.getString("text");
			Forecast forecast = new Forecast(date, day, high, low, text);
			list.add(forecast);
		}
		return list;
	}

	/**
	 * 从JSON数据中提取wind信息
	 * 
	 * @param data
	 * @return
	 * @throws JSONException
	 */
	public static Wind parseWindFromJson(String data) throws JSONException {
		JSONObject json = new JSONObject(data).getJSONObject("query").getJSONObject("results").getJSONObject("channel").getJSONObject("wind");
		String chill = json.getString("chill");
		String direction = json.getString("direction");
		String speed = json.getString("speed");
		return new Wind(chill, direction, speed);
	}

	/**
	 * 从JSON数据中提取ConditionWeather信息
	 * 
	 * @param data
	 * @return
	 * @throws JSONException
	 */
	public static ConditionWeather parseConditionWeatherFromJson(String data) throws JSONException {
		JSONObject json = new JSONObject(data).getJSONObject("query").getJSONObject("results").getJSONObject("channel").getJSONObject("item").getJSONObject("condition");
		;
		
		String code = json.getString("code");
		String date = json.getString("date");
		String temp = json.getString("temp");
		String text = json.getString("text");
		ConditionWeather weather = new ConditionWeather(code,date, temp);
		return weather;
	}

	public static List<Place> parseWoeidFromJson(String data) throws JSONException {
		List<Place> list = new ArrayList<>();
		JSONObject json = new JSONObject(data).getJSONObject("query");
		if (!json.isNull("results")) {
			JSONObject jsonResults = json.getJSONObject("results");
			try {
				// 强行被Array
				JSONArray jsonArrayPlace = jsonResults.getJSONArray("place");
				Log.v("YahooUtil", "jsonPlace 搜索匹配地址结果的个数 : " + jsonArrayPlace.length());
				for (int i = 0; i < jsonArrayPlace.length(); i++) {
					String woeid = ((JSONObject) jsonArrayPlace.get(0)).getString("woeid");
					String name = ((JSONObject) jsonArrayPlace.get(0)).getString("name");
					list.add(new Place(woeid, name));
				}

				return list;
			} catch (Exception e) {
				// 强行失败时
				JSONObject jsonPlace = jsonResults.getJSONObject("place");

				String woeid = jsonPlace.getString("woeid");
				String name = jsonPlace.getString("name");
				Log.v("YahooUtil", "获取name : " + name);
				Log.v("YahooUtil", "获取woeid : " + woeid);
				list.add(new Place(woeid, name));
				return list;
			}

		} else {
			Log.v("YahooUtil", "获取woeid : 没有对应地址");
			return null;
		}
	}

	public static int getIcon(String text) {
		if (text.equals("Rain")) {
			return WeatherIcon.RAIN.getValue();
		} else if (text.equals("Mostly Sunny")) {
			return WeatherIcon.SUN.getValue();
		} else if (text.equals("Thunderstorms")) {
			return WeatherIcon.STORM.getValue();
		} else if (text.equals("Sunny")) {
			return WeatherIcon.SUN.getValue();
		} else if (text.equals("Mostly Cloudy")) {
			return WeatherIcon.CLOUDY.getValue();
		} else {
			return WeatherIcon.NAN.getValue();
		}

	}

	enum WeatherIcon {
		STORM(R.drawable.yahoo_weather_024), RAIN(R.drawable.yahoo_weather_014), NAN(R.drawable.yahoo_weather_011), SUN(R.drawable.yahoo_weather_006), CLOUDY(R.drawable.yahoo_weather_002);

		private int icon;

		WeatherIcon(int w) {
			this.icon = w;
		}

		public int getValue() {
			return this.icon;
		}

	}

	public static List<Place> getPlaces(byte[] in) throws Exception {
		InputStream is = getStringStream(in);
		List<Place> list = null;
		Place place;
		String q = null ;
		XmlPullParser parser = Xml.newPullParser();// 得到Pull解析器
		parser.setInput(is, "UTF-8");// 设置输入流的编码
		int eventType = parser.getEventType();
		Log.e("PullXmlUtil", "_________________________解析开始");
		while (eventType != XmlPullParser.END_DOCUMENT) {// 如果没有到文件的结尾，就不断地解析
	
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT: // 文件的开始
				list = new ArrayList<Place>();
				break;
			case XmlPullParser.START_TAG:// 文件的tag开始
				place = new Place();
	
				String tagName = parser.getName();// 获得当前标签的名称
				Log.i("PullXmlUtil", "_________________________________________________tag:" + tagName);
				if (tagName.equals("m")) {
					q= parser.getAttributeValue(0);
					Log.e("", "___________________________________________________________________q : " + q);
					String grrid = parser.getAttributeValue(1);
					String n = parser.getAttributeValue(2);
				} else if (tagName.equals("s")) { //
					
					String k = parser.getAttributeValue(0);
					String d = parser.getAttributeValue(1);
					String[] datas = d.split("&");// 解析数据
					if (datas != null && datas.length > 0) {
						// 比如："ct:iso=CN&woeid=26198346&lon=114.19&lat=22.6441&s=Guangdong&c=China&country_woeid=23424781&pn=深圳市&n=Shenzhen, Guangdong, China"
						// 得到的数组为[iso=CN,woeid=26198346,lon=.........]
						Log.e("", "datas : " + datas.toString());
//						String[] values = new String[datas.length];
//						for (int i = 0; i < datas.length; i++) {
//							String[] split = datas[i].split("=");// 解析数据对
//																	// ，split[0]-->键
//																	// split[1]-->值
//							String entrty = split[0];
//							String value = split[1];
//							values[i] = value;
//						}
						place.setWoeid(datas[1].split("=")[1]);
						place.setpName(datas[datas.length-1].split("=")[1]);
						place.setName(q);
						list.add(place);// new Plcae（）加入list；继续下次循环
					}
				}
	
				break;
			case XmlPullParser.END_TAG:// 标签的结束
				place = null;
				break;
			default:
				break;
			}
			eventType = parser.next();
		}
		return list;
	}

	public static InputStream getStringStream(byte[] sInputString) {
		ByteArrayInputStream tInputStringStream = null;
		if (sInputString != null) {
			tInputStringStream = new ByteArrayInputStream(sInputString);
		}
		return tInputStringStream;
	}
	
	

	public static  CodeDB  getWeatherCode(int code,Context context){
		switch (code) {
		case 1:
			return  new CodeDB(0x10, context.getResources().getString(R.string.weather_01));
		case 2:
			return  new CodeDB(0x13, context.getResources().getString(R.string.weather_16));
		case 3:
			return  new CodeDB(0x16, context.getResources().getString(R.string.weather_19));
		case 4:
			return  new CodeDB(0x15, context.getResources().getString(R.string.weather_22));
		case 5:
			return  new CodeDB(0x0B, context.getResources().getString(R.string.weather_11));
		case 6:
			return  new CodeDB(0x0E, context.getResources().getString(R.string.weather_14));
		case 7:
			return  new CodeDB(0x0E, context.getResources().getString(R.string.weather_14));
		case 8:
			return  new CodeDB(0x04, context.getResources().getString(R.string.weather_04));
		case 9:
			return  new CodeDB(0x04, context.getResources().getString(R.string.weather_04));
		case 10:
			return  new CodeDB(0x06, context.getResources().getString(R.string.weather_06));
		case 11:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_07));
		case 12:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_07));
		case 13:
			return  new CodeDB(0x08, context.getResources().getString(R.string.weather_08));
		case 14:
			return  new CodeDB(0x08, context.getResources().getString(R.string.weather_61));
		case 15:
			return  new CodeDB(0x09, context.getResources().getString(R.string.weather_09));
		case 16:
			return  new CodeDB(0x0D, context.getResources().getString(R.string.weather_13));
		case 17:
			return  new CodeDB(0x0B, context.getResources().getString(R.string.weather_11));
		case 18:
			return  new CodeDB(0x0B, context.getResources().getString(R.string.weather_11));
		case 19:
			return  new CodeDB(0x0F, context.getResources().getString(R.string.weather_15));
		case 20:
			return  new CodeDB(0x0C, context.getResources().getString(R.string.weather_12));
		case 21:
			return  new CodeDB(0x0C, context.getResources().getString(R.string.weather_62));
		case 22:
			return  new CodeDB(0x0C, context.getResources().getString(R.string.weather_63));
		case 23:
			return  new CodeDB(0x13, context.getResources().getString(R.string.weather_64));
		case 24:
			return  new CodeDB(0x12, context.getResources().getString(R.string.weather_65));
		case 25:
			return  new CodeDB(0x11, context.getResources().getString(R.string.weather_66));
		case 26:
			return  new CodeDB(0x03, context.getResources().getString(R.string.weather_02));
		case 27:
			return  new CodeDB(0x03, context.getResources().getString(R.string.weather_02));
		case 28:
			return  new CodeDB(0x03, context.getResources().getString(R.string.weather_02));
		case 29:
			return  new CodeDB(0x03, context.getResources().getString(R.string.weather_02));
		case 30:
			return  new CodeDB(0x03, context.getResources().getString(R.string.weather_02));
		case 31:
			return  new CodeDB(0x01, context.getResources().getString(R.string.weather_01));
		case 32:
			return  new CodeDB(0x01, context.getResources().getString(R.string.weather_01));
		case 33:
			return  new CodeDB(0x01, context.getResources().getString(R.string.weather_01));
		case 34:
			return  new CodeDB(0x01, context.getResources().getString(R.string.weather_01));
		case 35:
			return  new CodeDB(0x0E, context.getResources().getString(R.string.weather_14));
		case 36:
			return  new CodeDB(0x10, context.getResources().getString(R.string.weather_73));
		case 37:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_74));
		case 38:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_74));
		case 39:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_74));
		case 40:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_07));
		case 41:
			return  new CodeDB(0x0A, context.getResources().getString(R.string.weather_75));
		case 42:
			return  new CodeDB(0x09, context.getResources().getString(R.string.weather_76));
		case 43:
			return  new CodeDB(0x0A, context.getResources().getString(R.string.weather_75));
		case 44:
			return  new CodeDB(0x03, context.getResources().getString(R.string.weather_03));
		case 45:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_74));
		case 46:
			return  new CodeDB(0x0B, context.getResources().getString(R.string.weather_77));
		case 47:
			return  new CodeDB(0x07, context.getResources().getString(R.string.weather_74));
		case 3200:
			return  new CodeDB(0x00, context.getResources().getString(R.string.weather_00));
		default:
			break;
		}
		 return  new CodeDB(0x00, context.getResources().getString(R.string.weather_00));
	}
}
