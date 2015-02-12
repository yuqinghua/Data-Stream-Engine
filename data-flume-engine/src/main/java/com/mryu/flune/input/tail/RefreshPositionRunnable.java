package com.mryu.flune.input.tail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.mryu.flune.input.JavaTailInput;

public class RefreshPositionRunnable implements Runnable {

	private ExecutorService refreshPositionService;
	private long timeout;
	private TimeUnit unit;
	private JavaTailInput tail;
	
	public RefreshPositionRunnable(ExecutorService refreshPositionService, long timeout, TimeUnit unit, JavaTailInput tail){
		this.refreshPositionService = refreshPositionService;
		this.timeout = timeout;
		this.unit = unit;
		this.tail = tail;
	}
	
	
	@Override
	public void run() {
		// System.out.println(dateformat.format(new Date()) +
		// "\t\t\tRefresh....");
		ObjectOutputStream out = null;
		File file = new File(tail.posPath);
		try {
			if (!file.exists() || file.isDirectory()) {
				file.createNewFile();
			}
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(tail.positionMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				refreshPositionService.awaitTermination(timeout, unit);
				refreshPositionService.submit(this);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}