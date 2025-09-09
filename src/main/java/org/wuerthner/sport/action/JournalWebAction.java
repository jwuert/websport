package org.wuerthner.sport.action;

import jakarta.ejb.Stateless;

@Stateless
public class JournalWebAction implements JournalWebActionInterface {
	
	@Override
	public String getId() {
		return "journal";
	}
}
