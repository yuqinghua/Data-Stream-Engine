package com.mryu.flune.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mryu.flune.common.Filed;

public abstract class AbstractEvent {

	protected ContextConfigure configure;
	
	protected boolean isRunning = false;
	protected List<String> tunnelNameList = new ArrayList<>();
	
	protected String eventName;
	protected Map<String, Filed> fileds;
	protected Map<String, Filed> fileds_add = new HashMap<String, Filed>();

	public AbstractEvent(){
	}
	
	void init(ContextConfigure configure, String eventName) {
		beforeInitialize();
		this.configure = configure;
		this.eventName = eventName;
		this.fileds = initFileds("fileds");
		this.fileds_add = initFileds("fileds_add");
		afterInitialize();
	}
	
	void startup(){
		Set<AbstractEvent> inputArray;
		for(String tunnelName : ContextConfigure.tunnelInputMap.keySet()){
			inputArray = ContextConfigure.tunnelEventMap.get(tunnelName);
			if(inputArray == null || inputArray.size() == 0){
				continue;
			}
			for(AbstractEvent input : inputArray){
				if(input == this){
					tunnelNameList.add(tunnelName);
				}
			}
		}
		isRunning = true;
	}
	
	
	private Map<String, Filed> initFileds(String filedsName) {
		if(configure.reader.getValue(eventName, filedsName) == null){
			return new HashMap<String, Filed>(0);
		}
		JSONArray json = null;
		try {
			json = JSONArray.fromObject(configure.reader.getValue(eventName, filedsName));
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String, Filed>(0);
		}
		Filed filed;
		JSONObject obj;
		Map<String, Filed> fileds = new HashMap<>();
		for(int i=0; i<json.size(); i++){
			obj = json.getJSONObject(i);
			filed = initFiled(obj);
			fileds.put(filed.getName(), filed);
		}
		return fileds;
	}
	
	protected Object getValue(Filed filed, String value) {
		try {
			String type = filed.getType();
			if(value == null){
				return null;
			}
			if(type == null || type.trim().equalsIgnoreCase("string")){
				return value;
			}
			else if(type.equalsIgnoreCase("integer")){
				return Integer.parseInt(value);
			}
			else if(type.equalsIgnoreCase("long")){
				return Long.parseLong(value);
			}
			else if(type.equalsIgnoreCase("float")){
				return Float.parseFloat(value);
			}
			else if(type.equalsIgnoreCase("double")){
				return Double.parseDouble(value);
			}
			else if(type.equalsIgnoreCase("boolean")){
				return Boolean.parseBoolean(value);
			}
			else if(type.equalsIgnoreCase("date")){
				return new SimpleDateFormat(filed.getFormat()).parse(value);
			}else{
				return value;
			}
		} catch (Exception e) {
//			e.printStackTrace();
			return value;
		}
		
		
	}

	private Filed initFiled(JSONObject jsonObject) {
		return (Filed) JSONObject.toBean(jsonObject, Filed.class);
	}

	protected void beforeInitialize(){}
	
	protected void afterInitialize(){}

	public abstract boolean matches(Object line);

	public abstract JSONObject process(Object line);

	public String getName() {
		return eventName;
	}

	public void setName(String name) {
		this.eventName = name;
	}

}
