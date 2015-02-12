package com.mryu.flune.input.tail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.mryu.flune.input.JavaTailInput;

public class RefreshWatchRunnable implements Runnable {

	public ExecutorService refreshWatchService;
	private long timeout;
	private TimeUnit unit;
	private JavaTailInput tail;
	
	
	public RefreshWatchRunnable(ExecutorService refreshWatchService, long timeout, TimeUnit unit, JavaTailInput tail) {
		this.refreshWatchService = refreshWatchService;
		this.timeout = timeout;
		this.unit = unit;
		this.tail = tail;
	}
	
	@Override
	public void run() {
		tail.refresh(false);
		try {
			refreshWatchService.awaitTermination(timeout, unit);
			refreshWatchService.submit(this);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}