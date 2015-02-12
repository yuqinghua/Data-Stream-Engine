package com.mryu.flune.output;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.mryu.flune.core.ContextConfigure;
import com.mryu.flune.core.AbstractOutput;

public class ConsoleOutput extends AbstractOutput {
	private static Logger logger = Logger.getLogger(ConsoleOutput.class);

	@Override
	public void process(JSONObject json) {
//		logger.info(json);
		System.out.println(json);
	}

}
