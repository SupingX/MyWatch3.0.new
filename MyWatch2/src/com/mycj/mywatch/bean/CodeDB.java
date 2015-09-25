package com.mycj.mywatch.bean;

public class CodeDB {
	private int protol;
	private String text;
	public CodeDB(int protol, String text) {
		super();
		this.protol = protol;
		this.text = text;
	}
	public CodeDB() {
		super();
	}
	
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getProtol() {
		return protol;
	}
	public void setProtol(int protol) {
		this.protol = protol;
	}
	
}