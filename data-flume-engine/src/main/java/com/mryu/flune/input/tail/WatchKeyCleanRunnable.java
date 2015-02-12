package com.mryu.flune.input.tail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class WatchKeyCleanRunnable implements Runnable {

	private ExecutorService watchKeyCleanService;
	private long timeout;
	private TimeUnit unit;
	
	public WatchKeyCleanRunnable(ExecutorService watchKeyCleanService, long timeout, TimeUnit unit){
		this.watchKeyCleanService = watchKeyCleanService;
		this.timeout = timeout;
		this.unit = unit;
	}

	@Override
	public void run() {
		try {
			watchKeyCleanService.awaitTermination(timeout, unit);
			watchKeyCleanService.submit(this);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}