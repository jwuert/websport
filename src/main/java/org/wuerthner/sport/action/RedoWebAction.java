package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class RedoWebAction implements RedoWebActionInterface {
	
	@Override
	public String getId() {
		return "redo";
	}
}
