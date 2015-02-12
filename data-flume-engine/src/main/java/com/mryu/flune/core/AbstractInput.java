package com.mryu.flune.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractInput implements Runnable{

	protected ContextConfigure configure;
	protected String inputName;
	
	protected List<String> tunnelNameList = new ArrayList<>();
	protected boolean isRunning = false;

	void init(ContextConfigure configure, String inputName) {
		beforeInitialize();
		this.configure = configure;
		this.inputName = inputName;
		afterInitialize();
	}
	
	void startup(){
		Set<AbstractInput> inputArray;
		for(String tunnelName : ContextConfigure.tunnelInputMap.keySet()){
			inputArray = ContextConfigure.tunnelInputMap.get(tunnelName);
			if(inputArray == null || inputArray.size() == 0){
				continue;
			}
			for(AbstractInput input : inputArray){
				if(input == this){
					tunnelNameList.add(tunnelName);
				}
			}
		}
		isRunning = true;
		new Thread(this).start();
	}

	protected void beforeInitialize() {
	}

	protected void afterInitialize() {
	}
	
	

	protected void emit(Object object){
		ContextConfigure.process(this.inputName, object);
	}
}
