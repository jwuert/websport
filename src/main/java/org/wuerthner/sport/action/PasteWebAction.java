package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class PasteWebAction implements PasteWebActionInterface {
	
	@Override
	public String getId() {
		return "paste";
	}
}
