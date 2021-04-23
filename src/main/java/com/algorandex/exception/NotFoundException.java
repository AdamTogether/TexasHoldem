package com.algorandex.exception;

public class NotFoundException extends Exception {
	
	private String message;
	
	public NotFoundException(String message) {
		this.message = message;
	}
	
	public String getmessage() {
		return message;
	}
}
