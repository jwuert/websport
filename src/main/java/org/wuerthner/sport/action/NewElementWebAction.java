package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class NewElementWebAction implements NewElementWebActionInterface {
	
	@Override
	public String getId() {
		return "newElement";
	}
}
