package com.mryu.flune.common;

public class Filed {
	private String name;
	private int col;
	private String type;
	private String format = "yyyy-MM-dd HH:mm:ss";
	private String defaultValue;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}