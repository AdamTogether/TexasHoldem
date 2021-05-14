package com.algorandex.model;

import lombok.Data;

@Data
public class Player {
	private String username;
	
	public Player(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}
}
