package com.mryu.flune.core;

public class ActionBean {
	private String action;
	private AbstractAction obj;
	
	public ActionBean(String action, AbstractAction obj) {
		this.action = action;
		this.obj = obj;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public AbstractAction getObj() {
		return obj;
	}

	public void setObj(AbstractAction obj) {
		this.obj = obj;
	}
	
}
