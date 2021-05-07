package com.algorandex.model;

import lombok.Data;

@Data
public class Player {
	private String username;
//	private String email;
//	private String password;
//	private String balance;
	
	public Player(String username) {
		// TODO Auto-generated constructor stub
		this.username = username;
	}
}
