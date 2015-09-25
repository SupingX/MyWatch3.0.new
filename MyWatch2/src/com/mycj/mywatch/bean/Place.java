package com.mycj.mywatch.bean;

public class Place {
	private int id;
	private String ptIso;
	private String woeid;
	private String lon; //经度
	private String lat;//纬度
	private String province;//深
	private String country;
	private String countryWoeid;
	private String pName;
	private String name;
	
	
	public Place(String woeid,String pName){
		this.woeid = woeid;
		this.pName = pName;
	}
	public Place(String ptIso, String woeid, String lon, String lat, String province, String country, String countryWoeid, String pName, String name) {
		super();
		this.ptIso = ptIso;
		this.woeid = woeid;
		this.lon = lon;
		this.lat = lat;
		this.province = province;
		this.country = country;
		this.countryWoeid = countryWoeid;
		this.pName = pName;
		this.name = name;
	}
	
	public Place() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Place(String [] values) {
		this(values[0],values[1],values[2],values[3],values[4],values[5],values[6],values[7],values[8]); 
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPtIso() {
		return ptIso;
	}
	public void setPtIso(String ptIso) {
		this.ptIso = ptIso;
	}
	public String getWoeid() {
		return woeid;
	}
	public void setWoeid(String woeid) {
		this.woeid = woeid;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCountryWoeid() {
		return countryWoeid;
	}
	public void setCountryWoeid(String countryWoeid) {
		this.countryWoeid = countryWoeid;
	}
	public String getpName() {
		return pName;
	}
	public void setpName(String pName) {
		this.pName = pName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	
	
}
