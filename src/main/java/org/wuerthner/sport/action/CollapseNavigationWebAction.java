package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class CollapseNavigationWebAction implements CollapseNavigationWebActionInterface {
	
	@Override
	public String getId() {
		return "collapseNavigation";
	}
}
