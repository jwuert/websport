package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class LogoutWebAction implements LogoutWebActionInterface {
	
	@Override
	public String getId() {
		return "logout";
	}
}
