package com.mycj.mywatch.bean;

public class ConditionWeather {
	
	private String id;
	private String code;
	private String date;
	private String temp;//当前温度
	private String text; //当时天气
	
	
	public ConditionWeather() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	


	public ConditionWeather(String code, String date, String temp) {
		super();
		this.code = code;
		this.date = date;
		this.temp = temp;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTemp() {
		return temp;
	}
	public void setTemp(String temp) {
		this.temp = temp;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
