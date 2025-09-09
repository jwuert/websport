package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class CutWebAction implements CutWebActionInterface {
	
	@Override
	public String getId() {
		return "cut";
	}
	
}
