package com.algorandex.model;

import lombok.Data;

@Data
public class GamePlay {
	
	private Player player;
	private HoldemMoveType move;
	private Integer betAmount;
	private String gameId;
	
	// SETTER FUNCTIONS
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	// GETTER FUNCTIONS
	public Player getPlayer() {
		return this.player;
	}
	
	public HoldemMoveType getMove() {
		return this.move;
	}

	public int getBetAmount() {
		return this.betAmount;
	}
	
	public String getGameId() {
		return this.gameId;
	}
}
