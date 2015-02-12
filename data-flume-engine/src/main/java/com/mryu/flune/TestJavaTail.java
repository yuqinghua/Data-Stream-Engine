package com.mryu.flune;
import com.mryu.flune.core.ContextConfigure;


public class TestJavaTail {
	public static void main(String[] args) {
		
		ContextConfigure configure = new ContextConfigure("F:\\workspace\\logtail-1.1\\src\\main\\resources\\logtail-console-config.ini");
		configure.startup();
	}
}
