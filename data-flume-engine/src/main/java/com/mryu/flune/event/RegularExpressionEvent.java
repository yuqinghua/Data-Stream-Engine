package com.mryu.flune.event;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import com.mryu.flune.common.Filed;
import com.mryu.flune.core.AbstractEvent;
import com.mryu.flune.util.TagDecoder;

public class RegularExpressionEvent extends AbstractEvent {
	
	private String regexp;

	private Pattern pattern;
	
	@Override
	protected void afterInitialize() {
		Map<String, String> section = configure.reader.getItemsBySectionName(eventName);
		this.regexp = section.get("regexp");
		this.pattern = Pattern.compile(this.regexp);
	}

	public Map<String, Filed> getFileds() {
		return fileds;
	}

	public void setFileds(Map<String, Filed> fileds) {
		this.fileds = fileds;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
		this.pattern = Pattern.compile(regexp);
	}
	
	public String getRegexp() {
		return regexp;
	}

	public Map<String, Filed> getFileds_add() {
		return fileds_add;
	}

	public void setFileds_add(Map<String, Filed> fileds_add) {
		this.fileds_add = fileds_add;
	}

	@Override
	public JSONObject process(Object txt) {
		Matcher m = pattern.matcher((CharSequence) txt);
		if(!m.matches()){
			return null;
		}
		JSONObject json = new JSONObject();
		for(String key : fileds.keySet()){
			json.elementOpt(key, getValue(fileds.get(key), m.group(fileds.get(key).getCol())));
		}
		
		for(String key : fileds_add.keySet()){
			if(fileds_add.get(key).getCol() > 0){
				json.elementOpt(key, getValue(fileds_add.get(key), m.group(fileds_add.get(key).getCol())));
			}else{
				json.elementOpt(key, getValue(fileds_add.get(key), TagDecoder.decodeSystemTag(fileds_add.get(key).getDefaultValue(), configure)));
			}
		}
		return json;
	}
	
	@Override
	public boolean matches(Object value) {
		if(!(value instanceof String)){
			return false;
		}
		try {
			return pattern.matcher((String) value).matches();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
