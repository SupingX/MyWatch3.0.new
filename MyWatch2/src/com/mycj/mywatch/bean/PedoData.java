package com.mycj.mywatch.bean;

import org.litepal.crud.DataSupport;

/**
 * 计步器数据
 * 日期：年
 * 日期：月
 * 日期：日
 * 步数
 * 距离
 * 卡路里
 * 运动：时
 * 运动：分
 * 运动：秒
 * 
 * 
 * @author Administrator
 *
 */
public class PedoData extends DataSupport {
	private String year; //yyyyMMdd
	private String month;
	private String day;
	private int id;
	private int step;
	private int distance;
	private int cal;
	private int hour;
	private int minute;
	private int second;
	
	public PedoData() {
		super();
	}
	
	
	public PedoData(int step, int distance, int cal, int hour, int minute, int second) {
		super();
		this.step = step;
		this.distance = distance;
		this.cal = cal;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	public int getCal() {
		return cal;
	}
	public void setCal(int cal) {
		this.cal = cal;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	public int getSecond() {
		return second;
	}
	public void setSecond(int second) {
		this.second = second;
	}
	
	@Override
	public String toString() {
//		return "[ step : "+ this.step 
//				+ ", distance : " + this.distance
//				+ ", cal : " + this.cal
//				+ ", hour : " + this.hour
//				+ ", mimute : " + this.minute
//				+ ", second : " + this.second
//				+ " ]";
		return 			this.year
				+"" + this.month
				+"" + this.day
				+ "" +this.step 
				+ "" + this.distance
				+ ""+ this.cal
				+ ""+ this.hour
				+ ""+ this.minute
				+ ""+this.second
				;
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
