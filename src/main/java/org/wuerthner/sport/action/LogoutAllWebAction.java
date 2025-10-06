package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class LogoutAllWebAction implements LogoutAllWebActionInterface {
	
	@Override
	public String getId() {
		return "logoutAll";
	}
}
