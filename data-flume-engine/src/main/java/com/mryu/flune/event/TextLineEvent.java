package com.mryu.flune.event;

import net.sf.json.JSONObject;

import com.mryu.flune.core.AbstractEvent;

public class TextLineEvent extends AbstractEvent {
	

	@Override
	public JSONObject process(Object txt) {
		JSONObject obj = new JSONObject();
		for(String filed : this.fileds.keySet()){
			obj.element(filed, txt);
		}
		return obj;
	}
	
	@Override
	public boolean matches(Object value) {
		return true;
	}
	
}
