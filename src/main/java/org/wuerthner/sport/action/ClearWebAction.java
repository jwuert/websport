package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class ClearWebAction implements ClearWebActionInterface {
	
	@Override
	public String getId() {
		return "clear";
	}
	
}
