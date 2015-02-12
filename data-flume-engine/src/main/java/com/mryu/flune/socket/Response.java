package com.mryu.flune.socket;

import net.sf.json.JSONObject;

public class Response {
	private int code;
	private String message;
	private JSONObject json;
	
	public Response() {
	}
	
	public Response(int code, String message, JSONObject json) {
		this.code = code;
		this.message = message;
		this.json = json;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

}
