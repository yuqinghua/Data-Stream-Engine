package com.mryu.flune.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

public abstract class AbstractOutput {
	
	private final static int DEFUALT_WORKERS = 1;
	
//	private static int cnt = 0;
	
	protected ContextConfigure configure;
	protected String outputName;

	private ExecutorService service;
	
	private boolean isRunning;
	
	private List<ArrayBlockingQueue<JSONObject>> queueList;
	
	protected void beforeInitialize(){}
	
	protected void afterInitialize(){}
	
	void init(ContextConfigure configure, String outputName){
		beforeInitialize();
		this.configure = configure;
		this.outputName = outputName;
		afterInitialize();
	}
	
	void startup(){
		if(queueList != null){
			queueList.clear();
		}else{
			queueList = new ArrayList<>(5);
		}
		Set<AbstractOutput> outputs;
		ArrayBlockingQueue<JSONObject> queue;
		for(String tunnelName : ContextConfigure.tunnelOutputMap.keySet()){
			outputs = ContextConfigure.tunnelOutputMap.get(tunnelName);
			if(outputs == null || outputs.size() == 0){
				continue;
			}else{
				for(AbstractOutput output : outputs){
					if(output == this){
						queue = ContextConfigure.tunnelQueueMap.get(tunnelName);
						if(queue != null){
							queueList.add(queue);
						}
					}
				}
			}
		}
		isRunning = true;
		String workerStr = configure.reader.getValue(outputName, "workers");
		int workers = DEFUALT_WORKERS;
		try {
			workers = workerStr == null ? DEFUALT_WORKERS : Integer.parseInt(workerStr);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initWorkers(workers);
	}
	
	public void initWorkers(int workers){
		workers = workers <= 0 ? DEFUALT_WORKERS : workers;
		service = Executors.newFixedThreadPool(workers);
		for(int i=0; i<workers; i++){
			service.submit(new Worker("[" + outputName + "|" + this.getClass().getName() + "|= " + i + "]"));
		}
	}
	
	public void close(){
		if(service == null){
			return;
		}
		boolean isClosed = false;
		isRunning = false;
		try {
			isClosed = service.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(!isClosed){
			try {
				service.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public abstract void process(JSONObject json);
	
	private class Worker implements Runnable{
		private JSONObject object;
		private String name;
		
		public Worker(String name) {
			this.name = name;
		}
		@Override
		public void run() {
			if(queueList == null || queueList.size() == 0){
				return;
			}
			while(isRunning){
				for(ArrayBlockingQueue<JSONObject> queue : queueList){
					if((object = queue.poll()) != null){
						break;
					}
				}
				if(object == null){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						System.err.println("ERROR: thread is interrupted. Thread[" + this.name + "]");
						e.printStackTrace();
					}
					continue;
				}
//				cnt ++;
//				System.out.println("CNT: " + cnt);
				AbstractOutput.this.process(object);
			}
			
		}
	}
	
}
