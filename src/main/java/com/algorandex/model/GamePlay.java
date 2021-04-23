package com.algorandex.model;

import lombok.Data;

@Data
public class GamePlay {
	
	private TicTacToe type;
	private Integer coordinateX;
	private Integer coordinateY;
	private String gameId;
	
	// SETTER FUNCTIONS
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	// GETTER FUNCTIONS
	public TicTacToe getType() {
		return type;
	}

	public int getCoordinateX() {
		return coordinateX;
	}

	public int getCoordinateY() {
		return coordinateY;
	}
	
	public String getGameId() {
		return this.gameId;
	}
}
