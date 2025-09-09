package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class AboutWebAction implements AboutWebActionInterface {
	
	@Override
	public String getId() {
		return "about";
	}
}
