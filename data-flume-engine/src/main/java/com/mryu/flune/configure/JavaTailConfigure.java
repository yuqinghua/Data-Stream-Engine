package com.mryu.flune.configure;

import org.apache.log4j.Logger;

import com.mryu.flune.core.ContextConfigure;

public class JavaTailConfigure extends ContextConfigure {

	static Logger logger = Logger.getLogger(JavaTailConfigure.class);

	public JavaTailConfigure(String configurePath) {
		super(configurePath);
	}



}
