package com.mycj.mywatch.bean;


import org.litepal.crud.DataSupport;

public class SleepData extends DataSupport{
	private int id;
	private String year;
	private String month;
	private String day;
	private String sdatas;
	
	
	public SleepData(String sdatas,String year, String month, String day) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
		this.sdatas = sdatas;
	}
	public String getSdatas() {
		return sdatas;
	}
	public void setSdatas(String sdatas) {
		this.sdatas = sdatas;
	}
	public SleepData() {
		super();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	
	@Override
	public String toString() {
		
		return "@SleepData:{ year:" +year+",month:"+month+",day:"+day+",values :" +sdatas+"}";
	}
	
}
