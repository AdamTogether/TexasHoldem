package com.algorandex.exception;

public class InvalidParamException extends Exception {
	
	private String message;
	
	public InvalidParamException(String message) {
		this.message = message;
	}
	
	public String getmessage() {
		return message;
	}
}
