package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class UndoWebAction implements UndoWebActionInterface {
	
	@Override
	public String getId() {
		return "undo";
	}
}
