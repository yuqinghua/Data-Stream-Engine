package com.mryu.flune.core;

import net.sf.json.JSONObject;


public class ServiceRunnable implements Runnable{
		private JSONObject msg;
		private AbstractOutput output;
		
		ServiceRunnable(JSONObject msg, AbstractOutput output){
			this.msg = msg;
			this.output = output;
		}

		@Override
		public void run() {
			output.process(msg);
		}
		
	}