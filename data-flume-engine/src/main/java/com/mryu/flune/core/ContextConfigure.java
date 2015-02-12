package com.mryu.flune.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.mryu.flune.util.IniReader;

public class ContextConfigure {
	
	private Logger logger = Logger.getLogger(ContextConfigure.class);

	protected String configurePath;
	public IniReader reader;

	private static final int DEFUALT_QUEUE_SIZE = 20000;
	/** tunnelQueueMap key:tunnelId, value: for process messages */
	protected static Map<String, ArrayBlockingQueue<JSONObject>> tunnelQueueMap = new HashMap<>();
	
	/** tunnelInputMap key: tunnelId, value: inputs of tunnel */
	protected static Map<String, Set<AbstractInput>> tunnelInputMap = new HashMap<>();
	
	/** tunnelInputMap key: tunnelId, value: outputs of tunnel */
	protected static Map<String, Set<AbstractOutput>> tunnelOutputMap = new HashMap<>();
	
	/** tunnelInputMap key: tunnelId, value: events of tunnel */
	protected static Map<String, Set<AbstractEvent>> tunnelEventMap = new HashMap<>();

	/** inputName: abstractInput */
	protected static Map<String, AbstractInput> inputMap = new HashMap<>();
	/** outputName: AbstractOutput */
	protected static Map<String, AbstractOutput> outputMap = new HashMap<>();
	/** eventName : AbstractEvent */
	protected static Map<String, AbstractEvent> eventMap = new HashMap<>();
	
	private String getDefaultConfigPath(){
		return this.getClass().getClassLoader().getResource("").getFile() + "config.ini";	
	}
	
	public ContextConfigure(String configurePath) {
		this.configurePath = configurePath;
		if(configurePath == null || configurePath.trim().equals("")){
			this.configurePath = getDefaultConfigPath();
		}
		File configFile = new File(configurePath);
		if(!configFile.exists() || !configFile.isFile()){
			this.configurePath = getDefaultConfigPath();
		}
		logger.info("config path: " + this.configurePath);
		
		// validate whether the configure section exists
		validateConfigureFile();
		
		// initialize tunnels
		initTunnels();
		
	}
	
	public void startup(){
		for(AbstractEvent event : eventMap.values()){
			event.startup();
		}
		for(AbstractInput input : inputMap.values()){
			input.startup();
		}
		for(AbstractOutput output : outputMap.values()){
			output.startup();
		}
	}
	
	private void initTunnels() {
		Map<String, String> config = reader.getItemsBySectionName("config");
		List<String> keys = new ArrayList<>();
		keys.addAll(config.keySet());
		String key;
		while(keys.size() > 0){
			key = keys.get(0);
			if(key.endsWith(".input") || key.endsWith(".events") || key.endsWith(".output")){
				createTunnel(keys, key.substring(0, key.lastIndexOf('.')));
			}else{
				logger.info("config:[" + key +"] is not a valid configure!");
				keys.remove(0);
			}
		}
	}

	private void createTunnel(List<String> keys, String key) {
		String eventsStr = null;
		String inputStr =  null;
		String outputStr  = null;
		String tempStr;
		
		Map<String, String> config = reader.getItemsBySectionName("config");
		
		tempStr = key + ".events";
		if(keys.contains(tempStr)){
			eventsStr = config.get(tempStr);
			keys.remove(tempStr);
		}
		tempStr = key + ".input";
		if(keys.contains(tempStr)){
			inputStr = config.get(tempStr);
			keys.remove(tempStr);
		}
		tempStr = key + ".output";
		if(keys.contains(tempStr)){
			outputStr = config.get(tempStr);
			keys.remove(tempStr);
		}
		for(int i=0; i< keys.size(); i++){
			if(keys.get(i).startsWith(key + ".")){
				keys.remove(keys.get(i));
				i --;
			}
		}
		
		if(inputStr == null || outputStr == null || eventsStr == null 
				|| inputStr.trim().equals("") || outputStr.trim().equals("") || eventsStr.trim().equals("")){
			loggerErrorTunnelConfigure(key);
			return;
		}
		String[] events = decodeJSONArrayString(eventsStr);
		String[] inputs = decodeJSONArrayString(inputStr);
		String[] outputs = decodeJSONArrayString(outputStr);
		if(events == null || inputs == null || outputs == null){
			loggerErrorTunnelConfigure(key);
			return;
		}

		// initQueues
		{
			int queueSize = DEFUALT_QUEUE_SIZE;
			if(keys.contains(key + ".size")){
				queueSize = Integer.parseInt(config.get(key + ".size")) ;
			}
			ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<>(queueSize);
			tunnelQueueMap.put(key, queue);
		}
		
		try {
			// initEvents
			initEvents(key, events);
			
			// initInputs
			initInputs(key, inputs);
			
			//initOutputs
			initOutputs(key, outputs);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	
	

	private String[] decodeJSONArrayString(String str){
		if(str == null || str.trim().equals("")){
			return null;
		}
		str = str.trim();
		if(str.matches("^[\\[].*[\\]]")){
			try {
				JSONArray jsonArr = JSONArray.fromObject(str);
				return jsonArr.size() == 0 ? null : (String[])jsonArr.toArray(new String[jsonArr.size()]);
			} catch (Exception e) {
				return null;
			}
		}else if(str.indexOf(',') == -1){
			String[] arr = str.split(",");
			for(int i=0; i<arr.length; i++){
				arr[i] = arr[i].trim();
			}
			if(arr.length == 0){
				return null;
			}
			return arr;
		}else{
			return new String[]{str};
		}
		
	}
	
	
	private void loggerErrorTunnelConfigure(String tunnelName){
		logger.warn("TUNNEL[" + tunnelName + "] is not correctly configured.");
	}
	

	private void validateConfigureFile() {
		Map<String, String> config = null;
		try {
			reader = new IniReader(this.configurePath);
			config = reader.getItemsBySectionName("config");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error: config path of " + this.configurePath + " failed to init name, type of config section!");
			exit(-1);
		}
		if(config == null || config.keySet().size() == 0){
			logger.error("Error: config path of " + this.configurePath + " does't contains 'config' section, or config section is empty!");
			System.exit(-1);
		}
	}

	private void initEvents(String key, String[] events) throws Exception {
		String event;
		Set<AbstractEvent> eventSet = new HashSet<>(5);
		AbstractEvent obj;
		for(int i=0; i<events.length; i++){
			event = events[i];
			if(eventMap.containsKey(event)){
				eventSet.add(eventMap.get(event));
				continue;
			}
			try {
				obj = (AbstractEvent) Class.forName(reader.getValue(event, "class")).getConstructor().newInstance();
				eventSet.add(obj);
				obj.init(this, event);
				eventMap.put(event, obj);
			} catch (Exception e) {
				logger.error("Error: Event[" + events[i] + "] is configured error! Tunnel[" + key + "]");
				throw e;
			}
		}
		tunnelEventMap.put(key, eventSet);
	}
	

	private void initInputs(String key, String[] inputs) throws Exception {
		String input;
		Set<AbstractInput> inputSet = new HashSet<>();
		AbstractInput obj;
		for(int i=0; i<inputs.length; i++){
			input = inputs[i];
			if(inputMap.containsKey(input)){
				inputSet.add(inputMap.get(input));
				continue;
			}
			try {
				obj = (AbstractInput) Class.forName(reader.getValue(input, "class")).getConstructor().newInstance();
				inputSet.add(obj);
				obj.init(this, input);
				inputMap.put(input, obj);
			} catch (Exception e) {
				logger.error("Error: Input[" + inputs[i] + "] is configured error! Tunnel[" + key + "]");
				throw e;
			}
		}
		tunnelInputMap.put(key, inputSet);
	}
	

	private void initOutputs(String key, String[] outputs) throws Exception {
		String output;
		Set<AbstractOutput> outputSet = new HashSet<>();
		AbstractOutput obj;
		for(int i=0; i<outputs.length; i++){
			output = outputs[i];
			if(outputMap.containsKey(output)){
				outputSet.add(outputMap.get(output));
				continue;
			}
			try {
				obj = (AbstractOutput) Class.forName(reader.getValue(output, "class")).getConstructor().newInstance();
				outputSet.add(obj);
				obj.init(this, output);
				outputMap.put(output, obj);
			} catch (Exception e) {
				logger.error("Error: Output[" + outputs[i] + "] is configured error! Tunnel[" + key + "]");
				throw e;
			}
		}
		tunnelOutputMap.put(key, outputSet);
	}

	protected static void process(String inputName, Object object){
		Set<AbstractEvent> events = matchEvent(inputName, object);
		if(events == null || events.size() == 0){
			return;
		}
		JSONObject json;
		for(AbstractEvent event : events){
			json = event.process(object);
			if(json != null){
				json.elementOpt("_eventName", event.getName());
//				service.submit(new ServiceRunnable(json, output));
				for(String tunnelName : tunnelEventMap.keySet()){
					if(tunnelEventMap.get(tunnelName).contains(event)){
						try {
							tunnelQueueMap.get(tunnelName).put(json);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	

	private void exit(int status) {
		logger.info("系统已经退出");
		System.exit(status);
	}
	
	private static Set<AbstractEvent> matchEvent(String inputName, Object object) {
		if(!inputMap.containsKey(inputName)){
			return null;
		}
		Set<AbstractEvent> set = new HashSet<AbstractEvent>(2);
		for(String tunnelName : inputMap.get(inputName).tunnelNameList){
			for(AbstractEvent e : tunnelEventMap.get(tunnelName)){
				if(e.matches(object)){
					set.add(e);
				}
			}
		}
		return set;
	}

	public IniReader getReader() {
		return reader;
	}

	public void setReader(IniReader reader) {
		this.reader = reader;
	}
	
}
