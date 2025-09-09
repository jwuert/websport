package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class CopyWebAction implements CopyWebActionInterface {
	
	@Override
	public String getId() {
		return "copy";
	}
	
}
