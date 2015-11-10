package com.mycj.mywatch.bean;

import org.litepal.crud.DataSupport;

public class HeartRateData extends DataSupport{
	
	private String year;
	private String month;
	private String day;
	private String hour;
	private String min;
	private int id;
	private int hr;
	private int avghr;
	private int maxHr;
	private int minHr;
	/**
	 * 每一次测试心率的记录集合（实际：每次3分钟）
	 * 格式为用，隔开，例如
	 * 80,12,151,123,13,2,131122
	 */
	private String hrDatas;
	
	public HeartRateData(String year, String month, String day,String hour,String min,String hrDatas) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour ;
		this.min = min;
		this.hrDatas = hrDatas;
	}
	public HeartRateData(String year,String month,String day,int hr, int avghr, int maxHr, int minHr) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
		this.hr = hr;
		this.avghr = avghr;
		this.maxHr = maxHr;
		this.minHr = minHr;
	}
	public HeartRateData(int hr, int avghr, int maxHr, int minHr) {
		super();
		this.hr = hr;
		this.avghr = avghr;
		this.maxHr = maxHr;
		this.minHr = minHr;
	}
	public HeartRateData( String year,String month,String day,int avghr, int maxHr, int minHr) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
		this.avghr = avghr;
		this.maxHr = maxHr;
		this.minHr = minHr;
	}
	public HeartRateData( int avghr, int maxHr, int minHr) {
		super();
		this.avghr = avghr;
		this.maxHr = maxHr;
		this.minHr = minHr;
	}
	public HeartRateData() {
		super();
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getHr() {
		return hr;
	}
	public void setHr(int hr) {
		this.hr = hr;
	}
	public int getAvghr() {
		return avghr;
	}
	public void setAvghr(int avghr) {
		this.avghr = avghr;
	}
	public int getMaxHr() {
		return maxHr;
	}
	public void setMaxHr(int maxHr) {
		this.maxHr = maxHr;
	}
	public int getMinHr() {
		return minHr;
	}
	public void setMinHr(int minHr) {
		this.minHr = minHr;
	}
	
	@Override
	public String toString() {
		return "["
				+ "日期 : " + this.year+"-"+this.month+"-"+this.day+" "+this.hour+":"+this.min
				+ "数据: " + this.hrDatas
				+ " ]";
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getHrDatas() {
		return hrDatas;
	}
	public void setHrDatas(String hrDatas) {
		this.hrDatas = hrDatas;
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getMin() {
		return min;
	}
	public void setMin(String min) {
		this.min = min;
	}
	
	
	
}
