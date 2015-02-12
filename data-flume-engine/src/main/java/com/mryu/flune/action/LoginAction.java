package com.mryu.flune.action;

import com.mryu.flune.core.AbstractAction;

public class LoginAction extends AbstractAction {
	private String key;
	public LoginAction() {
		// TODO Auto-generated constructor stub
	}
	
	public LoginAction(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
