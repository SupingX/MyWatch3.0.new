package com.mycj.mywatch.bean;

import org.litepal.crud.DataSupport;

public class SleepData extends DataSupport{
	private int id;
	private int[] sleeps; //一天的数据集合
	private String year;
	private String month;
	private String day;
	
	public SleepData() {
		super();
	}
	public SleepData(int []sleep, String year,String month,String day) {
		super();
		this.setSleeps((sleep));
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int[] getSleeps() {
		return sleeps;
	}
	public void setSleeps(int[] sleeps) {
		this.sleeps = sleeps;
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
	
}
