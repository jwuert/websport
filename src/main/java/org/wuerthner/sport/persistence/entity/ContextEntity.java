package org.wuerthner.sport.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GENERIC_CONTEXT")
public class ContextEntity {
	@Id
	@Column(name = "WRK_ID")
	private long workerId;
	
	@Column(name = "SELECTION")
	private String selection;
	
	public long getWorkerId() {
		return workerId;
	}
	
	public void setWorkerId(long workerId) {
		this.workerId = workerId;
	}
	
	public String getSelection() {
		return selection;
	}
	
	public void setSelection(String selection) {
		this.selection = selection;
	}
	
}
