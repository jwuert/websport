package org.wuerthner.sport.util;

public class Logger {
	private final String name;
	
	public Logger(String name) {
		this.name = name;
	}
	
	public static Logger getLogger(Class<?> clasz) {
		return new Logger(clasz.getName());
	}
	
	public void error(String msg) {
		System.err.println("ERROR: " + name + ": " + msg);
	}
	
	public void error(Exception e) {
		System.err.println("ERROR: " + name + ": " + e.getMessage());
		e.printStackTrace();
	}
	
	public void info(String msg) {
		System.err.println("INFO: " + name + ": " + msg);
	}
	
	public void debug(String msg) {
		System.err.println("DEBUG: " + name + ": " + msg);
	}
}
